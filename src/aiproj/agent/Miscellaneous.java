package aiproj.agent;

import java.awt.Point;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Random;

public class Miscellaneous {
	
	public static HashMap<Point, Point> next;
	public static long[][] zobrist;
	
	public static void init(int boardSize) {
		next = new HashMap<Point, Point>(8);
		next.put(new Point(-1, -1), new Point(0, -1));
		next.put(new Point(0, -1), new Point(1, -1));
		next.put(new Point(1, -1), new Point(1, 0));
		next.put(new Point(1, 0), new Point(1, 1));
		next.put(new Point(1, 1), new Point(0, 1));
		next.put(new Point(0, 1), new Point(-1, 1));
		next.put(new Point(-1, 1), new Point(-1, 0));
		next.put(new Point(-1, 0), new Point(-1, -1));
		
		zobrist = new long[boardSize*boardSize][Pieces.number];
		
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				for (int p = 0; p < Pieces.number; p++) {
					zobrist[j*boardSize + i][p] = Miscellaneous.randomLong();
				}
			}
		}
		
	}
	
	public static Point nextCell(Point offset) {
		offset = next.get(offset);

		return offset;
	}

	public static long randomLong() {
		Random ranGen = new SecureRandom();
		return ranGen.nextLong();
	}

}
