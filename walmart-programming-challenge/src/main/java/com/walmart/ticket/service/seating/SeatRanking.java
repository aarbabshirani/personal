package com.walmart.ticket.service.seating;

/**
 * 
 * @author aarbabshirani
 * 
 * This class determines the 
 */

public class SeatRanking {
  
  private int rows;
  private int cols;
  private int totalSeats;
  
  public SeatRanking(int rows, int cols) {
    this.rows = rows;
    this.cols = cols;
    this.totalSeats = Math.multiplyExact(rows, cols);
  }


  public enum RankingPolicy {
    CLOSEST_TO_STAGE;
  }
  
  
  public Integer getSeatRanking(RankingPolicy policy, Seat seat) {
    if (policy == RankingPolicy.CLOSEST_TO_STAGE) {
      return closetToStageRanking(seat);
    }
    return null;
  }
  
  private int closetToStageRanking(Seat seat) {
    return Math.multiplyExact(seat.getRow(), 1000) + seat.getCol();
  }

}
