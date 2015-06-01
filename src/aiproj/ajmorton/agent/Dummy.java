package aiproj.ajmorton.agent;

/** 
2  * COMP30024 Artificial Intelligence 
3  * Project B
4  * ajmorton Andrew Morton 522139  
5  * rhysw    Rhys Williams 661561 
6  */ 

import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import aiproj.ajmorton.agent.board.Board;
import aiproj.ajmorton.agent.board.ScoreBoard;
import aiproj.ajmorton.agent.decisionTree.GameMove;
import aiproj.ajmorton.agent.decisionTree.GameState;
import aiproj.ajmorton.agent.decisionTree.Tree.*;
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
	
	private ProbabilityCell[] cellProbabilities;
	
	// done
	public int init(int n, int p) {
		/* This function is called by the referee to initialise the player.
		 *  Return 0 for successful initialization and -1 for failed one.
		 */
		
		if (p != Cell.BLACK && p!= Cell.WHITE) {
			System.err.println("Invalid player piece id ("+p+"). "
					+ "Program terminating!");
			return FAILURE;
		}
		if (n < 4 || n > 9) {
			System.err.println("Invalid board size ("+n+").  "
					+ "Program terminating!");
			return FAILURE;
		}
		
		System.out.println("Starting game as Dummy: "+p);

		this.playerID = p;
		this.opponentID = (p == Cell.WHITE) ? Cell.BLACK : Cell.WHITE;
		this.mainBoard = new Board((byte) n);
		this.scoreBoard = new ScoreBoard((byte) n);
		
		cellProbabilities = new ProbabilityCell[n*n];
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < n; i++) {
				cellProbabilities[j*n + i] = new ProbabilityCell(i, j);
			}
		}
		
		this.currentPlayer = Cell.WHITE;
		
		Misc.init(mainBoard.getBoardSize());
		
		return SUCCESS;
	}

	
	public Move makeMove() {
		GameMove gm = null;
		while (gm == null) {
			int x = (int) Math.round(Math.random()*mainBoard.getBoardSize());
			int y = (int) Math.round(Math.random()*mainBoard.getBoardSize());
			if (mainBoard.isLegal(x, y)) {
				gm = new GameMove(playerID, x, y);	
			}
		}
		
		GameState gs = new GameState(null, gm);
		
		makeMove(gs);

		currentMove++;
		currentPlayer = ((currentPlayer == Cell.WHITE) ? Cell.BLACK :
														 Cell.WHITE);
		
		return GameMove.getMove(gm);
	}
	
	//done 
	public int opponentMove(Move m) {
		
		/* Function called by referee to inform the player about the
		 * opponent's move.
		 *  Return -1 if the move is illegal otherwise return 0
		 */
		
		GameMove gm = GameMove.getGameMove(m);
		
		if(!mainBoard.isLegal(gm)) {
			mainBoard.setCell(gm.getX(), gm.getY(), (byte) -1);
			return FAILURE;
		}
		
		GameState gs = new GameState(null, gm);

		makeMove(gs);
		
		currentMove++;
		currentPlayer = ((currentPlayer == Cell.WHITE) ? Cell.BLACK :
														 Cell.WHITE);
		return SUCCESS;
	}
	
	public int getWinner() {
		if (!mainBoard.isFull()) {
			return Piece.EMPTY;
		}

		for (int j = 0; j < mainBoard.getBoardSize(); j++) {
			for (int i = 0; i < mainBoard.getBoardSize(); i++) {
				int v = mainBoard.getValueAtPosition(i, j);
				if (v < 0) {
					return Piece.INVALID;
				}
			}
		}

		byte[] captures = mainBoard.getCaptures();

		if (captures[Cell.BLACK] > captures[Cell.WHITE]) {
			return Piece.BLACK;
		} else if (captures[Cell.WHITE] > captures[Cell.BLACK]) {
			return Piece.WHITE;
		} else {
			return Piece.DEAD;
		}
	}
	
	//
	public void printBoard(PrintStream output) {
	//TODO
	// change from int output to char, build the Cell.interface alternative
		byte[][] b = mainBoard.getBoard();
		for (int j = 0; j < mainBoard.getBoardSize(); j++) {
			for (int i = 0; i < mainBoard.getBoardSize(); i++) {
				output.print(Cell.toChar(b[j][i]) + " ");
			}
			output.println();			
		}
		
	}
	
	
	public void makeMove(GameState gs) {
		mainBoard.setCell(gs.getMove());
		mainBoard.checkCaptures(gs, scoreBoard, currentPlayer, true);
		
		GameMove m = gs.getMove();
		int x = m.getX();
		int y = m.getY();
		int size = mainBoard.getBoardSize();
		for (int j = -2; j <= 2; j++) {
			for (int i = -2; i <= 2; i++) {
				int nX = x + i;
				int nY = y + j;
				
				if (nX < 0 || nY < 0 || nX >= size || nY >= size) {
					continue;
				}
				
				if (i == 0 && j == 0) {
					cellProbabilities[nY*size + nX].setProbability(0);
				} else if (i == 1 || j == 1) {
					cellProbabilities[nY*size + nX].incrementProbability(15);
				} else {
					cellProbabilities[nY*size + nX].incrementProbability(5);
				}
			}
		}
		
		Arrays.sort(cellProbabilities);
		

	}
	
	public Board getBoard() {
		return mainBoard;
	}

	public ScoreBoard getScoreBoard() {
		return scoreBoard;
	}
	
}
