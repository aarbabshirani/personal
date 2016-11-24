package com.walmart.ticket.service.seating;

import java.util.List;

/**
 * 
 * @author aarbabshirani
 * 
 */

public class SeatHold {
  
  private List<Seat> heldSeats;
  private int seatHoldId;
  private String customerEmail;
  private long timestamp;
  
  
  public SeatHold(List<Seat> heldSeats, int seatHoldId, String customerEmail, long timestamp) {
    this.heldSeats = heldSeats;
    this.seatHoldId = seatHoldId;
    this.customerEmail = customerEmail;
    this.timestamp = timestamp;
  }

  public List<Seat> getHeldSeats() {
    return heldSeats;
  }
  
  public void setHeldSeats(List<Seat> heldSeats) {
    this.heldSeats = heldSeats;
  }
  
  public int getSeatHoldId() {
    return seatHoldId;
  }
  
  public void setSeatHoldId(int seatHoldId) {
    this.seatHoldId = seatHoldId;
  }
  
  public String getCustomerEmail() {
    return customerEmail;
  }
  
  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }
  
  public long getTimestamp() {
	return timestamp;
}

public void setTimestamp(long timestamp) {
	this.timestamp = timestamp;
}

public String toString() {
    return "Seats held: " + this.heldSeats.toString() + "\n" + 
           "Customer Email: " + this.customerEmail + "\n" +
           "Seat Hold Id: " + this.seatHoldId;
  }

}
