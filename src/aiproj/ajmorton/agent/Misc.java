package aiproj.ajmorton.agent;

/** 
2  * COMP30024 Artificial Intelligence 
3  * Project B
4  * ajmorton Andrew Morton 522139  
5  * rhysw    Rhys Williams 661561 
6  */ 

import java.awt.Point;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class Misc {
	
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
		
		zobrist = new long[boardSize*boardSize][Cell.number];
		
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				for (int p = 0; p < Cell.number; p++) {
					zobrist[j*boardSize + i][p] = randomLong();
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
	
	// http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
	public static <K, V extends Comparable<? super V>>
		Map<K, V> sortByValue( Map<K, V> map ) {
		List<Map.Entry<K, V>> list = new LinkedList<>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
				{
			@Override
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o1.getValue()).compareTo( o2.getValue() );
			}
				} );

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

}
