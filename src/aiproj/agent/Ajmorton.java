package aiproj.agent;

// TODO
// move Scoring to correct location (find where that is)
// pathfind still needs to update capDifference, need to pass reference to
// fill out comments.txt
// do we use alpha beta?
// scoreMap for 6x6 is likely wrong

import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import aiproj.agent.board.*;
import aiproj.agent.decisionTree.*;
import aiproj.agent.decisionTree.Tree.*;
import aiproj.squatter.*;

public class Ajmorton implements Player, Piece {
	
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
		
		System.out.println("Starting game as Ajmorton: "+p);

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
		
		Miscellaneous.init(mainBoard.getBoardSize());
		
		return SUCCESS;
	}

	
	public Move makeMove() {		
		Tree<GameState> decisionTree = new Tree<GameState>(new GameState(null, null));
		
		// We need to hold on to best node so we can pass the capture data along
		DLSBuildAB(decisionTree, decisionTree.getRoot(), 
				MAX_PLY, Board.copy(mainBoard), Integer.MIN_VALUE, Integer.MAX_VALUE);
		// Can't set new root until we have made move
		
		Node<GameState> best = null;		
		
		ArrayList<Node<GameState>> i = decisionTree.getRoot().getChildren();
		for (Node<GameState> n : i) {
			if (best == null || n.getData().getScore() > best.getData().getScore()) {
				best = n;
			}
		}
				
		GameMove r = best.getData().getMove();
		
		System.out.println("New root move - x: "+r.getX()+ " - y: "+r.getY()+ " - p: "+r.getPlayer());
		System.out.println("Best score: "+best.getData().getScore());
		
		GameMove gm = best.getData().getMove();
		gm.setPlayer((byte) playerID);

		makeMove(best.getData());

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
	
	//
	public int getWinner() {
	//TODO
		/* This function when called by referee should return the winner
		 *	Return -1, 0, 1, 2, 3 for INVALID, EMPTY, WHITE, BLACK, DEAD
		 *	respectively.
		 */
		
		if (!mainBoard.isFull()) {
			return Piece.EMPTY;
		}
		int[] pieces = new int[6];
		
		for (int j = 0; j < mainBoard.getBoardSize(); j++) {
			for (int i = 0; i < mainBoard.getBoardSize(); i++) {
				int v = mainBoard.getValueAtPosition(i, j);
					if (v < 0) {
						return Piece.INVALID;
					}
				pieces[mainBoard.getValueAtPosition(i, j)]++;
			}
		}
		
		// TODO CHANGE THIS!
		// TODO use gamestate knowledge of captures to compute winner
		
		if (pieces[Cell.BLACK_CAP] > pieces[Cell.WHITE_CAP]) {
			return Piece.WHITE;
		} else if (pieces[Cell.WHITE_CAP] > pieces[Cell.BLACK_CAP]) {
			return Piece.BLACK;
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
	
	public Node<GameState> DLSBuildAB(Tree<GameState> tree, Node<GameState> node,
			int maxDepth, Board pBoard, int a, int b) {
		if (maxDepth <= 0 || 
				node.getData().getDepth() == mainBoard.getFreeSpaces()) {
			//System.out.println("testing node: ");
			//pBoard.printBoard();
			Scoring.scoreState(node, pBoard, currentPlayer);
			pBoard = null;
			if (node.getData().getScore() > 0) {
				//System.out.println("Score: "+node.getData().getScore());
			}
			return node;
		}
			
		int p;		
		if (node.getData().getMove() != null) {
			p = node.getData().getMove().getPlayer();
			p = (p == Cell.WHITE) ? Cell.BLACK : Cell.WHITE;
		} else {
			p = playerID;
		}
		
		boolean maximizingPlayer = (p == playerID);
		
		/* Set board with all parent moves */
		Node<GameState> parentNode = node;
		while (parentNode != null) {
			if (parentNode.getData() != null) {
				if (parentNode.getData().getMove() != null) {
					pBoard.setCell(parentNode.getData().getMove());
				}
			}
			parentNode = parentNode.getParent();
		}

		Board tBoard = Board.copy(pBoard);
		
		GameState bestGS = new GameState(null, null);
		Node<GameState> best = new Node<GameState>(bestGS, null);
		if (maximizingPlayer)
			best.getData().setScore(Integer.MIN_VALUE);
		else
			best.getData().setScore(Integer.MAX_VALUE);
		
		for (int k = cellProbabilities.length-1; k >= 0; k--) {
			int j = cellProbabilities[k].getY();
			int i = cellProbabilities[k].getX();
			//System.out.println("Max depth: "+maxDepth);
			//System.out.println("@@@@@@@@");
			//pBoard.printBoard();
			if (pBoard.isLegal(i, j)) {
				if (tBoard == null) {
					//System.out.println("Cloning");
					tBoard = Board.copy(pBoard);
				}
				tBoard.setCell(i, j, (byte) p);
			
				GameMove gm = new GameMove(p, i, j);
				GameState gs = new GameState(node.getData(), gm);
				Node<GameState> newNode = new Node<GameState>(gs, node);
				newNode.getData().setDepth(node.getData().getDepth()+1);
				boolean captures = tBoard.checkCaptures(gs, scoreBoard, p);
				
				//System.out.println("=========");
				//tBoard.printBoard();
			
				/* alpha beta pruning */
				if (maximizingPlayer) {
					Node<GameState> child = DLSBuildAB(tree, newNode, maxDepth-1,
							tBoard, a, b);
					best = (child.getData().getScore() > best.getData().getScore())
							? child : best;
					a = (a > best.getData().getScore()) ? a:best.getData().getScore();
					if (b <= a) {
						//System.out.println("pruned - a: "+ a + " - v: "+best.getData().getScore());
						break;
					}
				} else {
					Node<GameState> child = DLSBuildAB(tree, newNode, maxDepth-1,
							tBoard, a, b);
					best = (child.getData().getScore() < best.getData().getScore()) ? child : best;
					b = (b < best.getData().getScore()) ? b : best.getData().getScore();
					if (b <= a) {
						//System.out.println("pruned - b: "+ b + " - v: "+best.getData().getScore());
						break;
					}
				}
				
				if (captures) {
					tBoard = null;
				}				
				if (newNode.getData().getDepth() == 1) {
					tree.getRoot().getChildren().add(newNode);
				}
				
				if (tBoard != null) {
					tBoard.resetCell(i, j, (byte) p);
				}

			}
		}
		/* Reset parent board to original state */
		parentNode = node;
		while (parentNode != null) {
			if (parentNode.getData() != null) {
				if (parentNode.getData().getMove() != null) {
					pBoard.resetCell(parentNode.getData().getMove());
				}
			}
			parentNode = parentNode.getParent();
		}
		
		node.getData().setScore(best.getData().getScore());
		
		return best;
	}
	
	public void generateZobristKeys(TreeSet<Long> l, Board b) {
		for (int i = 0 ; i < 7; i++) {
			Board t = b.transform(i);
			long key = t.hashKey();
			if (!l.contains(key)) {
				l.add(key);
			}
		}
	}
	
	public void makeMove(GameState gs) {
		mainBoard.setCell(gs.getMove());
		mainBoard.checkCaptures(gs, scoreBoard, currentPlayer);
		
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
		
		/*for (int i = 0; i < cellProbabilities.length; i++) {
			System.out.print(cellProbabilities[i].getProbability() + " ");
		}*/
		

	}
	
	public Board getBoard() {
		return mainBoard;
	}

	public ScoreBoard getScoreBoard() {
		return scoreBoard;
	}
	
}
