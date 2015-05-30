package aiproj.agent.board;

import java.awt.Point;

/**
 * a board that describes the A* value of cells in the gameboard
 * used for pathfinding to find captures on the board
 */
public class ScoreBoard extends Board {
	
	private int maxScore;		// max score of the score map function

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
		// TODO cell scores for the even board should all be -1, 
		//      but it still works as is
		byte maxValue, i, j;
		float vertScore, horiScore;

		// find max value of score map function
		// score is essentially manhattan distance from center of board
		maxValue = (byte) (boardSize - 2);
		
		// fill the cells with their A* scores, scores are determined by 
		// their manhattan distance from the center. 
		// If they are edge cells in they are given the max score
		for(i = 0; i < boardSize; i++) {
			for(j = 0; j < boardSize; j++) {
				// If cell on the outside
				if(j == 0 || i == 0 || i == boardSize - 1 ||
						j == boardSize - 1) {
					board[i][j] = maxValue;								
				} else {
					vertScore = Math.abs((float)(boardSize - 1)/2 - i);
					horiScore = Math.abs((float)(boardSize - 1)/2 - j);
					board[i][j] = (byte)(vertScore + horiScore); 
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
