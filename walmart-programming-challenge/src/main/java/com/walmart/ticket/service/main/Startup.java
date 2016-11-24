package com.walmart.ticket.service.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.walmart.ticket.service.process.TicketServiceImp;
import com.walmart.ticket.service.seating.SeatHold;


/**
 * aarbabshirani
 *
 */
public class Startup 
{
	public static final Logger LOGGER = Logger.getLogger(Startup.class); 
	public final static String configFileName = "conf.properties";

	public static void main( String[] args )
	{
		Properties props = new Properties();

		InputStream is = Startup.class.getClassLoader()
				.getResourceAsStream(configFileName);
		System.out.println("--- config file: " + configFileName);
		if (null != is) {
			try {
				props.load(is);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		} else {
			System.out.println("No config file was found");
		}
		LOGGER.info(props.toString());
		TicketServiceImp ticketService = new TicketServiceImp();
		try {
			ticketService.init(props);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return;
		}

		// Display menu
		System.out.println("=============================================================================================");
		System.out.println("|                                  Ticket Service MENU                                      |");
		System.out.println("=============================================================================================");
		System.out.println("| Options:                                                                                  |");
		System.out.println("|    1. Enter 1 to print the seating venue                                                  |");
		System.out.println("|    2. Enter 2 for number of available seats                                               |");
		System.out.println("|    3. Enter \"hold num Email\" to hold tickets (e.g hold 5 test@walmart.com)                |");
		System.out.println("|    4. Enter \"res seatHoldId Email\" to reserve the tickets (e.g res 5 test@walmart.com)    |");
		System.out.println("|    5. Enter 5 to Exit                                                                     |");
		System.out.println("============================================================================================");

		String choice = "";
		do {
			Scanner scanner = new Scanner(System.in);
			choice = scanner.nextLine();

			if (choice.equals("1")) {
				System.out.println("Seating Venue");
				ticketService.printSeating();
			} else if (choice.equals("2")) {
				System.out.println("Available Seats: " + ticketService.numSeatsAvailable());
			} else if (choice.split("\\s+").length == 3) {
				String[] s = choice.split("\\s+");
				if (s[0].equals("hold")) {
					int numOfSeats = Integer.parseInt(s[1].trim());
					String email = s[2].trim();
					SeatHold sh = ticketService.findAndHoldSeats(numOfSeats, email);
					if (null != sh) {
						System.out.println(sh);
					}
				} else if (s[0].equals("res")) {
					int numOfSeats = Integer.parseInt(s[1].trim());
					String email = s[2].trim();
					System.out.println("Reservation Confirmation Code: " + ticketService.reserveSeats(numOfSeats, email));
				}
			} 

		} while (!choice.trim().equalsIgnoreCase("5"));
		System.out.println("Exiting the system");
		System.exit(-1);

	}
}
