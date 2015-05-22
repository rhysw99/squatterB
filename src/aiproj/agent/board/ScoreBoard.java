package aiproj.agent.board;

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
		byte isEven;
		float vertScore, horiScore;

		//find max value of score map function
		// score is essentially manhattan distance from center of board
		if(boardSize%2 == 0) { 	//if even
			maxValue = (byte) (boardSize - 3);
			isEven = 1;
		} else {
			maxValue = (byte) (boardSize - 2);
			isEven = 0;
		}
		
		
		for(i = 0; i < boardSize; i++){
			for(j = 0; j < boardSize; j++){
				if( j == 0 || i == 0 || i == boardSize - 1 || j == boardSize - 1){
					board[i][j] = maxValue;											
				} else {
					vertScore = Math.abs((float)(boardSize - 1)/2 - i);
					horiScore = Math.abs((float)(boardSize - 1)/2 - j);
					board[i][j] = (byte)(vertScore + horiScore - isEven); 
				}
			}
		}
		return maxValue;
	}

	public int getMaxScore() {
		return maxScore;
	}

}
