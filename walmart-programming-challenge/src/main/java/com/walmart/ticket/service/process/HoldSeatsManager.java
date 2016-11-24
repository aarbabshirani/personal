package com.walmart.ticket.service.process;

import java.time.Instant;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.walmart.ticket.service.seating.Seat;
import com.walmart.ticket.service.seating.SeatHold;
import com.walmart.ticket.service.seating.SeatHoldComparator;

/**
 * 
 * @author aarbabshirani
 * 
 * This class manages tickets that are to be hold
 * It removes the expired tickets by running a single scheduled thread executor
 * 
 */

public class HoldSeatsManager {

	public static final Logger LOGGER = Logger.getLogger(HoldSeatsManager.class); 
	
	// keeps heldSeats in the order of creation (FIFO), it is used to expire held tickets
	private PriorityBlockingQueue<SeatHold> holdQueue; 
	private final int initCapacity = 10;
	private Comparator<SeatHold> seatHoldcomparator;
	
	// Map of seatHoldId to SeatHold, keeps heldSeats for retrieval to reserve
	private ConcurrentHashMap<Integer, SeatHold> heldSeatsMap; 
	
	// Reference to seatingVenue, it is used to put back expired seats for a ticket
	private PriorityBlockingQueue<Seat> seatingVenue;
	private ScheduledExecutorService scheduledExecutorService;
	private long holdExpiryInSec;


	public HoldSeatsManager(PriorityBlockingQueue<Seat> seatingVenue, long holdExpiryInSec) {
		seatHoldcomparator = new SeatHoldComparator();
		holdQueue = new PriorityBlockingQueue<SeatHold>(initCapacity, seatHoldcomparator);
		heldSeatsMap = new ConcurrentHashMap<Integer, SeatHold>(initCapacity);
		this.seatingVenue = seatingVenue;
		this.holdExpiryInSec = holdExpiryInSec;
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(); 
		@SuppressWarnings("unchecked")
		ScheduledFuture scheduledFuture =
		scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
			public void run() {
				removeExpiredRecords();
			}
		}, 5L , holdExpiryInSec/2, TimeUnit.SECONDS);
	}
	
	public int size() {
		return this.heldSeatsMap.size();
	}

	public void addSeatHold(SeatHold seatHold) {
		heldSeatsMap.put(seatHold.getSeatHoldId(), seatHold);
		holdQueue.add(seatHold);
	}

	public SeatHold getAndRemoveSeatHold(int seatHoldId) {
		SeatHold sh = heldSeatsMap.get(seatHoldId);
		heldSeatsMap.remove(seatHoldId);
		return sh; 
	}
	public Enumeration<SeatHold> getAllHeldSeats() {
		return this.heldSeatsMap.elements();
	}

	public void removeExpiredRecords() {
		long now = Instant.now().getEpochSecond();
		SeatHold sh = holdQueue.peek();
		while (null != sh && (now - sh.getTimestamp()) >= holdExpiryInSec) {
			sh = holdQueue.poll();
			heldSeatsMap.remove(sh.getSeatHoldId());
			LOGGER.info("removing expired seatHold: " + sh.toString());
			List<Seat> seats = sh.getHeldSeats();
			for (Seat seat : seats) {
				if (!seat.isReserved()) {
					seat.setHold(false);
					seatingVenue.add(seat);
				}
			}
			sh = holdQueue.peek();
		}

	}

}
