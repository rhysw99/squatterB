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

	private int player;
	
	private static Board mainBoard;
	private static ScoreBoard scoreBoard;
	
	public static void main(String[] args) {
		System.out.println("test");
		Ajmorton aj = new Ajmorton();
		aj.init(7, 1);
	}
	
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
		this.mainBoard = new Board((byte) n);
		this.scoreBoard = new ScoreBoard((byte) n);
		
		System.out.println("test2");
		
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
		
		if(!mainBoard.isLegal(GameMove.getGameMove(m))) {
			return FAILURE;
		}
			
		mainBoard.updateBoard(GameMove.getGameMove(m));
		
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
		Tree<GameState> decisionTree = new Tree<GameState>(new Board(mainBoard.getBoardSize()));
		Root<GameState> root = decisionTree.getRoot();
		
		int p = Piece.BLACK;
		int depth = 1;
		
		for (int d = depth; d < depth + 3; d++) {
			for (int j = 0; j < mainBoard.getBoardSize(); j++) {
				for (int i = 0; i < mainBoard.getBoardSize(); i++) {
					//TODO Switch to new move class?
					GameMove m = new GameMove(p, new Point(i, j));
					if (mainBoard.isLegal(m)) {
						root.insert(new GameState(root.getData(), m));
					}
				}
			}
		}
		
	}
	
	public static Board getBoard() {
		return mainBoard;
	}

	public static ScoreBoard getScoreBoard() {
		return scoreBoard;
	}
	
}
