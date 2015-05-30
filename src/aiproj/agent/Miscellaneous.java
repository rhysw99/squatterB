package aiproj.agent;

import java.awt.Point;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

//TODO rename from miscellaneous
/**
 * stores functions for searching adjecent cells on the board
 */
public class Miscellaneous {
	
	
	public static HashMap<Point, Point> next;
	public static long[][] zobrist;
	

	public static void init(int boardSize) {
		
		// a hashtable that describes clockwise movement around (0,0)
		// the current position passed into the table returns the next 
		// clockwise position
		next = new HashMap<Point, Point>(8);
		next.put(new Point(-1, -1), new Point(0, -1));
		next.put(new Point(0, -1), new Point(1, -1));
		next.put(new Point(1, -1), new Point(1, 0));
		next.put(new Point(1, 0), new Point(1, 1));
		next.put(new Point(1, 1), new Point(0, 1));
		next.put(new Point(0, 1), new Point(-1, 1));
		next.put(new Point(-1, 1), new Point(-1, 0));
		next.put(new Point(-1, 0), new Point(-1, -1));
		
		
		
		// creates a board of the same size as the gameBoard and populates 
		// with random long values to use for zobrist hashing
		zobrist = new long[boardSize*boardSize][Cell.number];
		
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				for (int p = 0; p < Cell.number; p++) {
					zobrist[j*boardSize + i][p] = Miscellaneous.randomLong();
				}
			}
		}
	}
	
	// method that implements uses the "next" hashtable for clockwise 
	// movement on the board
	public static Point nextCell(Point offset) {
		offset = next.get(offset);

		return offset;
	}

	public static long randomLong() {
		Random ranGen = new SecureRandom();
		return ranGen.nextLong();
	}

}
