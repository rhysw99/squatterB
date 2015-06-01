package aiproj.ajmorton.agent;

/** 
2  * COMP30024 Artificial Intelligence 
3  * Project B
4  * ajmorton Andrew Morton 522139  
5  * rhysw    Rhys Williams 661561 
6  */ 

/**
 *Defines the constants associated with each possible cell state on the board.
 */
public class Cell {
	
	//TODO why include number?
	public static int number = 6;
	
	public static final int EMPTY     =  0,		// id empty cell
							WHITE     =  1, 	// id white cell
            				BLACK     =  2,		// id black cell
            				WHITE_CAP =  3,		// id captured white cell
            				BLACK_CAP =  4,		// id captured black cell
            				CAP       =  5;		// id uncaptured cell
	
	/**
	 * takes in the cell id's defined above and returns the respective
	 * char characters for readability
	 * @param cellID	the integer id of the cell passed in
	 * @return the character corresponding to the cell is
	 */
	public static char toChar(int cellID){
		
		switch(cellID){
		case 0:	 return '-';
		case 1:  return 'W';
		case 2:  return 'B';
		case 3:  return 'w';
		case 4:  return 'b';
		case 5:  return '+';
		default: return ' ';
		}
	}
	
}
