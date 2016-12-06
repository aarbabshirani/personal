package com.walmart.ticket.service.process;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import com.walmart.ticket.service.exception.WPCConfigurationException;
import com.walmart.ticket.service.seating.SeatHold;

import org.apache.log4j.Logger;
import org.junit.Assert;

public class TicketServiceTreeImpTest {

	public static final Logger LOGGER = Logger.getLogger(TicketServiceTreeImpTest.class);
	private TicketServiceImp tested;
	private String rows = "100";
	private String cols = "1000";
	private long seatHoldExpiry = 0;
	private int total = 0;

	@Before
	public void setUp() throws Exception {
		tested = new TicketServiceImp();
		seatHoldExpiry = 4;
		Properties props = new Properties();
		props.setProperty("rows", rows);
		props.setProperty("cols", cols);
		props.setProperty("seatHoldExpiry", Long.toString(seatHoldExpiry));
		tested.init(props);
		total = tested.numSeatsAvailable();
	}

	@Test
	public void testInitSeating() {
		String rows = "10";
		String cols = "20";
		Properties props = new Properties();
		props.setProperty("rows", rows);
		props.setProperty("cols", cols);
		props.setProperty("seatHoldExpiry", "20");
		try {
			tested.init(props);
		} catch (Exception e1) {
			fail("Unexpected Exception: " + e1.getCause());
		}
		Assert.assertEquals(Math.multiplyExact(Integer.parseInt(rows), Integer.parseInt(cols)), tested.numSeatsAvailable());

		rows = "0";
		cols = "0";
		props = new Properties();
		props.setProperty("rows", rows);
		props.setProperty("cols", cols);
		props.setProperty("seatHoldExpiry", "20");
		try {
			tested.init(props);
			fail("init failed to throw WPCConfigurationException");
		} catch (WPCConfigurationException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (Exception e) {
			fail("Unexpected Exception: " + e.getCause());
		}
	}

	@Test
	public void testFindAndHoldSeats() {
		int numSeatToHold = 5;
		String email = "test@walmart.com";

		SeatHold sh = tested.findAndHoldSeats(numSeatToHold, email);
		Assert.assertEquals(sh.getCustomerEmail(), email);
		Assert.assertEquals(sh.getHeldSeats().size(), numSeatToHold);
		Assert.assertEquals(total - numSeatToHold, tested.numSeatsAvailable());

		sh = tested.findAndHoldSeats(3, "test1@walmart.com");
		Assert.assertEquals(sh.getCustomerEmail(), "test1@walmart.com");
		Assert.assertEquals(sh.getHeldSeats().size(), 3);
		Assert.assertEquals(total - numSeatToHold - 3, tested.numSeatsAvailable());
		
		sh = tested.findAndHoldSeats(total, "test2@walmart.com");
		Assert.assertNull(sh);
	}
	
	@Test
	public void testFindAndHoldSeats_expiredTicket() {
		int numSeatToHold = 5;
		String email = "test@walmart.com";
		SeatHold sh = tested.findAndHoldSeats(numSeatToHold, email);
		Assert.assertEquals(sh.getCustomerEmail(), email);
		Assert.assertEquals(sh.getHeldSeats().size(), numSeatToHold);
		Assert.assertEquals(total - numSeatToHold, tested.numSeatsAvailable());
		try {
			Thread.sleep(this.seatHoldExpiry * 1000 * 2);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		Assert.assertEquals(total, tested.numSeatsAvailable());
	}
	
	@Test
	public void testReserveSeats() {
		int numSeatToRes = 5;
		String email = "test@walmart.com";
		SeatHold sh = tested.findAndHoldSeats(numSeatToRes, email);
		String reservation = tested.reserveSeats(sh.getSeatHoldId(), email);
		Assert.assertEquals(this.total - numSeatToRes, tested.numSeatsAvailable());
		Assert.assertEquals(1, tested.reservedSeatsMap.size());
		Assert.assertEquals(0, tested.holdSeats.size());
		Assert.assertEquals(numSeatToRes, tested.reservedSeatsMap.get(reservation).getHeldSeats().size());
		try {
			Thread.sleep(this.seatHoldExpiry * 1000 * 2);
		} catch (InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		Assert.assertEquals(this.total - numSeatToRes, tested.numSeatsAvailable());
		Assert.assertEquals(1, tested.reservedSeatsMap.size());
		Assert.assertEquals(numSeatToRes, tested.reservedSeatsMap.get(reservation).getHeldSeats().size());
	}
	
	@Test
	public void test() {
		ExecutorService executor = Executors.newFixedThreadPool(1000);

		// holding 80000 seats using 1000 threads, Always 20000 seats should be left available
		IntStream.range(0, 80000)
	    .forEach(i -> executor.submit(this::findAndHoldSeats));

		stop(executor);

		Assert.assertEquals(20000, tested.numSeatsAvailable());
		System.out.println(tested.numSeatsAvailable());
	}
	
	public static void stop(ExecutorService executor) {
        try {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("task is not finished, force killing the tasks");
            }
            executor.shutdownNow();
        }
    }
	
	public void findAndHoldSeats() {
		tested.findAndHoldSeats(1, "abc@walmart.com");
	}
	

}
