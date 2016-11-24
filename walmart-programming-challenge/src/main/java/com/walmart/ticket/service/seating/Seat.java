package com.walmart.ticket.service.seating;

/**
 * 
 * @author aarbabshirani
 * 
 */

public class Seat {
  
  private boolean reserved;
  private boolean hold;
  private int row;
  private int col;
  private int rank;
  
  
  public Seat(boolean reserved, boolean hold, int row, int col) {
    this.reserved = reserved;
    this.hold = hold;
    this.row = row;
    this.col = col;
  }
  
  public boolean isAvailable() {
    return !(reserved | hold);
  }
  
  public boolean isReserved() {
    return reserved;
  }
  
  public void setReserved(boolean reserved) {
    this.reserved = reserved;
  }
  
  public boolean isHold() {
    return hold;
  }
  
  public void setHold(boolean hold) {
    this.hold = hold;
  }
  
  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }
  
  public String toString() {
	  return "[r=" + this.row + ", c=" + this.col + ", res=" + this.reserved + 
			  ", hold=" + this.hold + ", rank=" + this.rank + "]";
  }
  
}
