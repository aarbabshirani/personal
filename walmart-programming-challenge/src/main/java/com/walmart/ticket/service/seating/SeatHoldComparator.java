package com.walmart.ticket.service.seating;

import java.util.Comparator;

/**
 * 
 * @author aarbabshirani
 * 
 */

public class SeatHoldComparator implements Comparator<SeatHold>{

  public int compare(SeatHold s1, SeatHold s2) {
    if (s1.getTimestamp() < s2.getTimestamp()) {
      return -1;
    }
    if (s1.getTimestamp() > s2.getTimestamp()) {
      return 1;
    }
    return 0;
  }

}
