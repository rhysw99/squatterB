package aiproj.agent.board;

import java.awt.Point;

public class ScoreBoard extends Board {
	
	private int maxScore;		// max score of the score map function

	public ScoreBoard(byte boardSize) {
		super(boardSize);
		
		this.maxScore = generateMetadata();
	}
	
	private byte generateMetadata() {
		// populates the score map
		// TODO check the maths on this one
		byte maxValue, i, j;
		float vertScore, horiScore;

		//find max value of score map function
		// score is essentially manhattan distance from center of board
		maxValue = (byte) (boardSize - 2);
		
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
				if (board[i][j] <= 0) {
					board[i][j] = 1;
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
