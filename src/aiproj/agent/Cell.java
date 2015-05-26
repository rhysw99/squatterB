package aiproj.agent;

public class Cell {
	
	public static final int EMPTY     =  0,
							WHITE     =  1, 
            				BLACK     =  2,
            				WHITE_CAP =  3,
            				BLACK_CAP =  4,
            				CAP       =  5;
	
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
