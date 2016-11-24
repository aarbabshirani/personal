package com.walmart.ticket.service.process;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.walmart.ticket.service.exception.WPCConfigurationException;
import com.walmart.ticket.service.seating.Seat;
import com.walmart.ticket.service.seating.SeatComparator;
import com.walmart.ticket.service.seating.SeatHold;
import com.walmart.ticket.service.seating.SeatRanking;
import com.walmart.ticket.service.seating.SeatRanking.RankingPolicy;


/**
 * 
 * @author aarbabshirani
 * This class implements TicketService interface
 * It uses a PriorityBlockingQueue to simulate the seating venue
 * 
 */

public class TicketServiceImp implements TicketService{

	public static final Logger LOGGER = Logger.getLogger(TicketServiceImp.class); 
	
	// Available seats ordered by their rank
	private PriorityBlockingQueue<Seat> seatingVenue;
	
	// Held Seats ordered by EPOCH timestamp
	protected HoldSeatsManager holdSeats;
	
	// Map of reservation confirmation code to reserved seats
	protected ConcurrentHashMap<String, SeatHold> reservedSeatsMap;

	private SeatRanking seatRanking;
	private AtomicInteger seatHoldId;
	private int rows;
	private int cols;


	public void init(Properties props) throws Exception {
		int rows = Integer.parseInt((String) props.get("rows"));
		int cols = Integer.parseInt((String) props.get("cols"));
		this.rows = rows;
		this.cols = cols;
		validate(rows, cols);
		long seatHoldExpiry = Long.parseLong((String) props.get("seatHoldExpiry"));
		String seatingPolicy = props.getProperty("seatingPolicy", RankingPolicy.CLOSEST_TO_STAGE.toString());
		int totalSeats = Math.multiplyExact(rows, cols);

		Comparator<Seat> comparator = new SeatComparator();
		seatingVenue = new PriorityBlockingQueue<Seat>(totalSeats, comparator);
		holdSeats = new HoldSeatsManager(seatingVenue, seatHoldExpiry);
		reservedSeatsMap = new ConcurrentHashMap<>();
		seatHoldId = new AtomicInteger(0);
		seatRanking = new SeatRanking(rows, cols);
		initSeatings(rows, cols, RankingPolicy.valueOf(seatingPolicy));
	}

	private boolean validate(int rows, int cols) {
		if (rows == 0 || cols == 0) {
			throw new WPCConfigurationException("rows or columns can not be zero");
		}
		return true;
	}

	
	/**
	 * 
	 * @param rows
	 * @param cols
	 * @param rankingPolicy
	 * 
	 * Initials the seating venue based on the ranking policy
	 */
	protected void initSeatings(int rows, int cols, RankingPolicy rankingPolicy) {
		for (int r = 1; r <= rows; r++) {
			for (int c = 1; c <= cols; c++) {
				Seat s = new Seat(false, false, r, c);
				int ranking = seatRanking.getSeatRanking(rankingPolicy, s);
				s.setRank(ranking);
				seatingVenue.add(s);
			}

		}
	}

	public int numSeatsAvailable() {
		return seatingVenue.size();
	}

	/**
	 * seatingVenue object is threadsafe, but this method should be synchronized because:
	 * During the polling, it is possible that another thread polls from the seatingVenue
	 * and it runs out of available seats, and both hold requests get rejected, however, with
	 * synchronized, possibly one of requests can go through depending on the num of seats 
	 * 
	 */
	public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
		if (numSeats > numSeatsAvailable()) {
			System.out.println("Not enough seats available");
			return null;
		}
		List<Seat> seatList = new ArrayList<Seat>();
		for (int i = 0; i < numSeats; i++) {
			Seat s = seatingVenue.poll();
			if (s != null) {
				s.setHold(true);
				seatList.add(s);
			}
		}
		SeatHold sh = new SeatHold(seatList, seatHoldId.getAndIncrement(), customerEmail, Instant.now().getEpochSecond());
		holdSeats.addSeatHold(sh);
		return sh;
	}

	public String reserveSeats(int seatHoldId, String customerEmail) {
		SeatHold seatHold = holdSeats.getAndRemoveSeatHold(seatHoldId);
		if (null == seatHold) {
			LOGGER.info("Held seats are no longer available");
			return null;
		}
		for (Seat seat : seatHold.getHeldSeats()) {
			seat.setReserved(true);
		}
		String reservationId = UUID.randomUUID().toString();
		reservedSeatsMap.put(reservationId, seatHold);
		return reservationId;
	}

	public void printSeating() {
		String[][] seating = new String[rows+1][cols+1];
		Enumeration<SeatHold> enumration = holdSeats.getAllHeldSeats();
		while (enumration.hasMoreElements()) {
			SeatHold sh = enumration.nextElement();
			for (Seat s : sh.getHeldSeats()) {
				seating[s.getRow()][s.getCol()] = "H";
			}
		}

		Collection<SeatHold> c = reservedSeatsMap.values();
		Iterator<SeatHold> it = c.iterator();
		while (it.hasNext()) {
			SeatHold sh = it.next();
			for (Seat s : sh.getHeldSeats()) {
				seating[s.getRow()][s.getCol()] = "R";
			}
		}
		
		for (int i = 0; i <= this.rows; i++) {
			
			for (int j = 0; j <= this.cols; j++) {
				if (i == 0 && j == 0) {
					System.out.print("  ");
					continue;
				}
				if (i == 0) {
					System.out.print(j + " "); 
					continue;
				}
				if (j == 0) {
					System.out.print(i + " ");
					continue;
				}
				if ( null == seating[i][j]) {
					System.out.print("-" + " "); 
				} else {
					System.out.print(seating[i][j] + " "); 
				}
			}
			System.out.println();
		}
	}

}
