package Agent;


import java.io.PrintStream;

import aiproj.squatter.*;

public class Ajmorton implements Player, Piece{

	public int boardSize;				// should have getters and setters
	public int playerColour;
	
	public Board board;
	
	public int init(int n, int p) {
		/* This function is called by the referee to initialise the player.
		 *  Return 0 for successful initialization and -1 for failed one.
		 */	
		
		boolean playerCorrect = ((p == BLACK) || (p == WHITE));	
		boolean sizeCorrect   = ((n == 5) || (n == 7));							// Is this only cases?
			
		
		if(playerCorrect && sizeCorrect){
			this.boardSize    = n;
			this.playerColour = p;
			board             = new Board(p);
			return 0;
		
		} else {
			return -1;
		}
		
		
	}

	public Move makeMove() {
		//TODO
		/* Function called by referee to request a move by the player.
		 *  Return object of class Move
		 */
		
		return null;
	}
	
	public int opponentMove(Move m) {
		
		/* Function called by referee to inform the player about the opponent's move
		 *  Return -1 if the move is illegal otherwise return 0
		 */
		
		if(board.isLegal(m, boardSize) == 0){
			Update.updateBoard(m, board);
			return 0;
		}
		
		return -1;
	}
	
	public int getWinner() {
	//TODO
		/* This function when called by referee should return the winner
		 *	Return -1, 0, 1, 2, 3 for INVALID, EMPTY, WHITE, BLACK, DEAD respectively
		 */
		
		return 0;
	}
	
	public void printBoard(PrintStream output) {
	//TODO
		/* Function called by referee to get the board configuration in String format
		 * 
		 */

		
		
	}

	
	
}
