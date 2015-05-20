package aiproj.agent.decisionTree;

import java.awt.Point;

/* ADT to store move history */
public class Move {
	
	/* Maybe use a byte instead and use hashTable or something to get the char from the byte and vice versa as in project A. */
	private char player;
	/* Using point is better java design, however it has much more overhead than just two bytes */
	private Point location;
	
	public Move(char player, Point location) {
		this.player = player;
		this.location = location;
	}

	public char getPlayer() {
		return player;
	}

	public Point getLocation() {
		return location;
	}
}
