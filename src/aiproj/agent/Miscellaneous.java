package aiproj.agent;

import java.awt.Point;
import java.util.HashMap;

public class Miscellaneous {
	
	public static HashMap<Point, Point> next;
	
	public static void init() {
		next = new HashMap<Point, Point>(8);
		next.put(new Point(-1, -1), new Point(0, -1));
		next.put(new Point(0, -1), new Point(1, -1));
		next.put(new Point(1, -1), new Point(1, 0));
		next.put(new Point(1, 0), new Point(1, 1));
		next.put(new Point(1, 1), new Point(0, 1));
		next.put(new Point(0, 1), new Point(-1, 1));
		next.put(new Point(-1, 1), new Point(-1, 0));
		next.put(new Point(-1, 0), new Point(-1, -1));
	}
	
	public static Point nextCell(Point offset) {
		offset = next.get(offset);

		return offset;
	}

}
