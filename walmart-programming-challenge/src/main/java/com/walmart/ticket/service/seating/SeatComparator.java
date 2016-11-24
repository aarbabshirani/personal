package com.walmart.ticket.service.seating;

import java.util.Comparator;

/**
 * 
 * @author aarbabshirani
 * 
 */

public class SeatComparator implements Comparator<Seat>{

  public int compare(Seat s1, Seat s2) {
    if (s1.getRank() < s2.getRank()) {
      return -1;
    }
    if (s1.getRank() > s2.getRank()) {
      return 1;
    }
    return 0;
  }

}
