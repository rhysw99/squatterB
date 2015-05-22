package aiproj.agent;


import java.awt.Point;
import java.io.PrintStream;

import aiproj.agent.board.Board;
import aiproj.agent.board.ScoreBoard;
import aiproj.agent.board.Update;
import aiproj.agent.decisionTree.GameMove;
import aiproj.agent.decisionTree.GameState;
import aiproj.agent.decisionTree.Tree;
import aiproj.agent.decisionTree.Tree.Root;
import aiproj.squatter.*;

public class Ajmorton implements Player, Piece {
	
	public static final int FAILURE = -1;
	public static final int SUCCESS = 0;

	public int player;
	
	public Board board;
	public ScoreBoard scoreBoard;
	
	// done
	public int init(int n, int p) {
		/* This function is called by the referee to initialise the player.
		 *  Return 0 for successful initialization and -1 for failed one.
		 */	
		
		boolean playerCorrect = ((p == BLACK) || (p == WHITE));
		boolean sizeCorrect   = ((n == 5) || (n == 7)); // Is this only cases?
			

		if (!playerCorrect || !sizeCorrect) {
			return FAILURE;
		}
		
		this.player = p;
		this.board		  = new Board((byte) p);
		
		return SUCCESS;
	}

	
	public Move makeMove() {
		//TODO
		/* Function called by referee to request a move by the player.
		 *  Return object of class Move
		 */
		
		return null;
	}
	
	//done 
	public int opponentMove(Move m) {
		
		/* Function called by referee to inform the player about the opponent's move
		 *  Return -1 if the move is illegal otherwise return 0
		 */
		
		if(!board.isLegal(m)){
			return FAILURE;
		}
			
		Update.updateBoard(m, board);
		
		return SUCCESS;
	}
	
	//
	public int getWinner() {
	//TODO
		/* This function when called by referee should return the winner
		 *	Return -1, 0, 1, 2, 3 for INVALID, EMPTY, WHITE, BLACK, DEAD respectively
		 */
		
		return 0;
	}
	
	public boolean validMove(byte[][] board, Move m) {
		
		
		return true;
	}
	
	//
	public void printBoard(PrintStream output) {
	//TODO

		
	}
	
	public void buildTree() {
		byte[][] d_board = new byte[5][5];
		Tree<GameState> decisionTree = new Tree<GameState>(d_board);
		Root<GameState> root = decisionTree.getRoot();
		
		int p = Piece.BLACK;
		int depth = 1;
		
		for (int d = depth; d < depth + 3; d++) {
			for (int j = 0; j < board.getBoardSize(); j++) {
				for (int i = 0; i < board.getBoardSize(); i++) {
					//TODO Switch to new move class?
					GameMove m = new GameMove(p, new Point(i, j));
					if (board.isLegal(m)) {
						root.insert(new GameState(root.getData(), m));
					}
				}
			}
		}
	
		
		
		
	}

	
	
}
