package aiproj.agent;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

import aiproj.agent.board.Board;
import aiproj.agent.board.ScoreBoard;
import aiproj.agent.decisionTree.GameMove;
import aiproj.agent.decisionTree.GameState;
import aiproj.agent.decisionTree.Tree;
import aiproj.agent.decisionTree.Tree.Node;
import aiproj.agent.decisionTree.Tree.Root;
import aiproj.squatter.*;

public class Dummy implements Player, Piece {
	
	public static final int FAILURE = -1;
	public static final int SUCCESS = 0;
	
	public static final int MAX_PLY = 4;
	
	public static final boolean DEBUG = false;

	private int playerID;
	private int opponentID;
	
	private int currentPlayer;
	
	private int currentMove;
	
	private Board mainBoard;
	private ScoreBoard scoreBoard;
	
	private ArrayList<GameMove> moves;
	
	// done
	public int init(int n, int p) {
		/* This function is called by the referee to initialise the player.
		 *  Return 0 for successful initialization and -1 for failed one.
		 */
		
				
		boolean playerCorrect = ((p == Pieces.BLACK) || (p == Pieces.WHITE));
		boolean sizeCorrect   = ((n == 6) || (n == 7)); // Is this only cases?			

		if (!playerCorrect || !sizeCorrect) {
			return FAILURE;
		}
		
		this.playerID = p;
		this.opponentID = (p == Pieces.WHITE) ? Pieces.BLACK : Pieces.WHITE;
		this.mainBoard = new Board((byte) n);
		this.scoreBoard = new ScoreBoard((byte) n);

		this.moves = new ArrayList<GameMove>();
		
		this.currentPlayer = Pieces.WHITE;
		
		Miscellaneous.init(mainBoard.getBoardSize());
		
		return SUCCESS;
	}

	
	public Move makeMove() {
		GameMove gm = null;
		
		while (gm == null) {
			int x = (int) Math.round(Math.random()*mainBoard.getBoardSize());
			int y = (int) Math.round(Math.random()*mainBoard.getBoardSize());
			int p = currentPlayer;
			if (mainBoard.isLegal(x, y, p)) {
				gm = new GameMove(p, x, y);
			}
		}

		makeMove(gm);

		currentMove++;
		currentPlayer = ((currentPlayer == Pieces.WHITE) ? Pieces.BLACK : Pieces.WHITE);

		return GameMove.getMove(gm);
	}
	
	
	//done 
	public int opponentMove(Move m) {
		
		/* Function called by referee to inform the player about the opponent's move
		 *  Return -1 if the move is illegal otherwise return 0
		 */
		
		GameMove gm = GameMove.getGameMove(m);
		
		if(!mainBoard.isLegal(gm)) {
			return FAILURE;
		}

		mainBoard.setCell(gm);
		mainBoard.checkCaptures(gm, scoreBoard);
		
		currentMove++;
		currentPlayer = ((currentPlayer == Pieces.WHITE) ? Pieces.BLACK : Pieces.WHITE);
		
		return SUCCESS;
	}
	
	//
	public int getWinner() {
	//TODO
		/* This function when called by referee should return the winner
		 *	Return -1, 0, 1, 2, 3 for INVALID, EMPTY, WHITE, BLACK, DEAD respectively
		 */
		
		if (mainBoard.isFull()) {
			return 3;
		}
		
		return 0;
	}
	
	//
	public void printBoard(PrintStream output) {
	//TODO
		byte[][] b = mainBoard.getBoard();
		for (int j = 0; j < mainBoard.getBoardSize(); j++) {
			for (int i = 0; i < mainBoard.getBoardSize(); i++) {
				output.print(b[j][i]+" ");
			}
			output.println();			
		}
		
	}
	
	int nodes = 0;
	int duplicates = 0;

	public void makeMove(GameMove m) {
		//System.out.println("Move: X:"+m.getLocation().x+" - y: "+m.getLocation().y+" - player: "+m.getPlayer());
		mainBoard.setCell(m);
		mainBoard.checkCaptures(m, scoreBoard);
		moves.add(m);
	}
	
	public Board getBoard() {
		return mainBoard;
	}

	public ScoreBoard getScoreBoard() {
		return scoreBoard;
	}
	
}
