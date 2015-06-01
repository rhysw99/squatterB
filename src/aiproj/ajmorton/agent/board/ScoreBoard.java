package aiproj.ajmorton.agent.board;

/** 
2  * COMP30024 Artificial Intelligence 
3  * Project B
4  * ajmorton Andrew Morton 522139  
5  * rhysw    Rhys Williams 661561 
6  */ 

import java.awt.Point;

/**
 * a board that describes the A* value of cells in the gameboard
 * used for pathfinding to find captures on the board
 */
public class ScoreBoard extends Board {
	
	private int maxScore; // max score of the score map function

	public ScoreBoard(byte boardSize) {
		super(boardSize);
		
		this.maxScore = generateMetadata();
	}
	
	/**
	 * loads the scoreBoard cells with values for the pathfinding algorithm
	 * scores are calculated as their manhattan distance from the center of 
	 * the board
	 * @return either the value of the largest scored cell or -1 for error
	 */
	private byte generateMetadata() {
		byte maxValue, i, j;

		// find max value of score map function
		// score is essentially manhattan distance from center of board
		maxValue = (byte) (boardSize - 2);
		
		// fill the cells with their A* scores, scores are determined by 
		// their manhattan distance from the center. 
		// If they are edge cells in they are given the max score
		for (i = 0; i < boardSize; i++) {
			for (j = 0; j < boardSize; j++) {
				// If cell on the outside
				for (int k = 0; k < boardSize/2; k++) {
					if (board[i][j] == 0) {
						if (j == k || i == k || i == boardSize - 1 - k || j == boardSize - 1 - k) {
							board[i][j] = (byte) (maxValue - k);								
						}
					}
				}
			}
		}
		return maxValue;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public int getValue(Point point) {
		if (onBoard(point)) {
			return board[point.y][point.x];
		}
		return -1;
	}

}
