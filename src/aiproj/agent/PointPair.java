package aiproj.agent;

import java.awt.Point;

/** 
 * Data type that stores two Points
 */
public class PointPair {

	private Point newCell;
	private Point prevCell;
	
	/* CONSTRUCTOR */
	public PointPair(Point newCell, Point prevCell){
		this.newCell  = newCell;
		this.prevCell = prevCell;
	}
	
	/* GETTERS */
	public Point getNewCell()	{return newCell;}
	public Point getPrevCell()	{return prevCell;}
	
	/* SETTERS */
	public void setNewCell(Point newCell) {this.newCell = newCell;}
	public void setPrevCell(Point prevCell) {this.prevCell = prevCell;}
}
