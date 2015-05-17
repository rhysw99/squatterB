/**
 * COMP30024 Artificial Intelligence
 * Project A - Checking Win States
 * ajmorton Andrew Morton 522139 
 * rhysw    Rhys Williams 661561
 */
// TODO fix for projB
package aiproj.agent;

import java.awt.Point;

public class Cell {
	
	private char owner;					// the owner of the cell ('B', 'W', '+', '-')
	private Point p;					// the x and y coordinates of the cell (as a Point)
	
	
	/* CONSTRUCTER */
	public Cell(Point p, char owner) {
		this.p = p;
		this.owner = owner;
	}
	
	/* SETTERS */
	public void setOwner(char owner) {
		this.owner = owner;
	}	
	
	/* GETTERS */
	public char getOwner() {
		return this.owner;
	}
	
	public Point getPosition() {
		return p;
	}
}
