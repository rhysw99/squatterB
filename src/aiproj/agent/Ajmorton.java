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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Stream;

import aiproj.agent.board.*;
import aiproj.agent.decisionTree.*;
import aiproj.agent.decisionTree.Tree.*;
import aiproj.squatter.*;

/**
 * The player AI
 * interfaces with the referee to run the game
 *
 */
public class Ajmorton implements Player, Piece {
	
	public static final int FAILURE = -1;			// error code returned to referee after initialising
	public static final int SUCCESS = 0;			// success code returned to referee after initialising
	public static final int MAX_PLY = 4;			// the depth the decision tree can explore to 
	
	public static final boolean DEBUG = false;

	private int playerID;							// the id assigned to the player (black or white)
	private int opponentID;							// the id assigned to the opponent
	
	private int currentPlayer;						// whose move it currently is
	private int currentMove;
	
	private Board mainBoard;						// the board the pieces are placed on
	private ScoreBoard scoreBoard;					// a scoremap of the board used for pathfinding
	
	private ProbabilityCell[] cellProbabilities;
	private ProbabilityCell[] permanent;
	
	// TODO remove?
	/*
	public static void main(String[] args) {		
		Ajmorton aj = new Ajmorton();
		aj.init(6, 1);
		aj.scoreBoard.printBoard();
		
		aj.mainBoard.setCell(5, 6, (byte)Cell.BLACK);
 
		

		/*
		 * 	1 2 1 2 1 2 
			1 2 1 2 1 2 
			1 2 1 2 1 2 
			1 2 1 2 5 2 
			1 2 1 2 5 2 
			1 2 1 2 2 1 */
		 
		
		
		/*
		Point placed = new Point(2,3);
		
		aj.mainBoard.updateBoard(new GameMove(Cell.BLACK, placed, Cell.WHITE));
		
		
		aj.mainBoard.checkCaptures(new GameMove(Cell.BLACK, placed, Cell.WHITE), aj.scoreBoard);
		
		System.out.println("\n");
		aj.printBoard(System.out);
		
		*/
		
		/*
		// build a root and a node and a board size 6
		GameMove recentMove = new GameMove(Cell.BLACK, new Point(3,3), Cell.BLACK);
		GameState newGs = new GameState(null, recentMove);
		Root<GameState> root = new Root<GameState>(newGs); 
		Node<GameState> node = new Node<GameState>(newGs, root);
		Board currentBoard = new Board(7);
		*/
		
		/*
		 * 0 0 0 0 0 0
		 * 0 0 0 0 0 0 
		 * 0 0 0 0 0 0 
		 * 0 0 0 0 0 0 
		 * 0 0 0 0 0 0
		 * 0 0 0 0 0 0
		 
		
		//set Board
		currentBoard.setCell(2, 2, (byte)Cell.BLACK);
		currentBoard.setCell(3, 3, (byte)Cell.BLACK);
		currentBoard.setCell(1, 1, (byte)Cell.BLACK);
		currentBoard.setCell(3, 1, (byte)Cell.WHITE);

		/*
		 * 0 0 0 0 0 0
		 * 0 2 0 1 0 0 
		 * 0 0 2 0 0 0 
		 * 0 0 0 2 0 0 
		 * 0 0 0 0 0 0
		 * 0 0 0 0 0 0
		 */

		// score the node
		//Scoring.scoreState(node, root, currentBoard.getBoard());
		
		//System.out.println("boardScore is: " + node.getScore() + "\n");
		
		
		
		/*while (!aj.mainBoard.isFull()) {
			//System.out.println("New cycle");
			aj.makeMove();
			GameMove gm = new GameMove(aj.currentPlayer, new Point(0,0));
			while (!aj.mainBoard.isLegal(gm) && !aj.mainBoard.isFull()) {
				int x = (int) Math.round(aj.mainBoard.getBoardSize()*Math.random());
				int y = (int) Math.round(aj.mainBoard.getBoardSize()*Math.random());
				gm = new GameMove(aj.currentPlayer, new Point(x,y));
			}
			aj.opponentMove(GameMove.getMove(gm));
			
			aj.mainBoard.printBoard();
		}
		
		
		
	}
	*/
	
	/** This function is called by the referee to initialise the player.
	 *  Return 0 for successful initialization and -1 for failed one.
	 */
	public int init(int n, int p) {
		
		// is the id assigned to player valid?
		if (p != Cell.BLACK && p != Cell.WHITE) {
			System.err.println("Invalid player piece id ("+p+"). "
					+ "Program terminating!");
			return FAILURE;
		}
		
		// is the board withing reasonable expectations?
		if (n < 4 || n > 9) {
			System.err.println("Invalid board size ("+n+").  "
					+ "Program terminating!");
			return FAILURE;
		}
		
		// System.out.println("Starting game as Ajmorton: "+p);

		this.playerID = p;
		this.opponentID = (p == Cell.WHITE) ? Cell.BLACK : Cell.WHITE;
		this.mainBoard = new Board((byte) n);
		this.scoreBoard = new ScoreBoard((byte) n);
		
		this.cellProbabilities = new ProbabilityCell[n*n];
		
		this.permanent = new ProbabilityCell[n*n];
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < n; i++) {
				permanent[j*n + i] = new ProbabilityCell(i, j);
			}
		}
		
		permanent[n*n/2].setProbability(5);
		
		System.arraycopy(permanent, 0, cellProbabilities, 0, permanent.length);
		
		Arrays.sort(cellProbabilities);
		
		this.currentPlayer = Cell.WHITE;
		
		Misc.init(mainBoard.getBoardSize());
		// create the main board
		
		return SUCCESS;
	}

	/**
	 * makes a move when requested by the referee
	 * returns a Move object with the relevant information
	 */
	// TODO add comments on operation
	public Move makeMove() {		
		nodes = 0;
		Tree<GameState> decisionTree = new Tree<GameState>(new GameState(null, null));
		
		// We need to hold on to best node so we can pass the capture data along
		DLSBuildAB(decisionTree, decisionTree.getRoot(), 
				MAX_PLY, Board.copy(mainBoard), Integer.MIN_VALUE, Integer.MAX_VALUE);
		// Can't set new root until we have made move
		
		Node<GameState> best = null;
		
		System.out.println("nodes: "+nodes);
		
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
	
	/** 
	 * Function called by referee to inform the player about the
	 * opponent's move and update the gameboard accordingly.
	 * Return -1 if the move is illegal otherwise return 0
	 */
	public int opponentMove(Move m) {
		
		// convert the object of type move to type GameMove for internal usage
		GameMove gm = GameMove.getGameMove(m);
		
		// checks if the move is legally possible, if not return FAILURE
		if(!mainBoard.isLegal(gm)) {
			mainBoard.setCell(gm.getX(), gm.getY(), (byte) -1);
			return FAILURE;
		}
		
		// creates object containing pertinent information about the move
		GameState gs = new GameState(null, gm);

		// updates the board with this information
		makeMove(gs);
		
		//updates the turn counter and whose turn it is
		currentMove++;
		currentPlayer = ((currentPlayer == Cell.WHITE) ? Cell.BLACK :
														 Cell.WHITE);
		return SUCCESS;
	}
	
	/** This function when called by referee should return the winner
	 *	Return -1, 0, 1, 2, 3 for INVALID, EMPTY, WHITE, BLACK, DEAD
	 *	respectively.
	 */
	public int getWinner() {
		
		//check if more moves can be made/ if the game is over
		if (!mainBoard.isFull()) {
			return Piece.EMPTY;
		}
		
		int[] pieces = new int[6];
		
		for (int j = 0; j < mainBoard.getBoardSize(); j++) {
			for (int i = 0; i < mainBoard.getBoardSize(); i++) {
				int v = mainBoard.getValueAtPosition(i, j);
					if (v < 0) {
						//TODO is this possible to reach, setting a cell to invalid and continuing shouldn't happen?
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
	
	/**
	 * prints the board state to the ouput, pieces are rpreresented by B,W,b,w,+ or -
	 */
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
	
	public int nodes = 0;
	

	/**
	 * builds the decision tree for finding the next best move to make
	 * @param tree		the decision tree
	 * @param node		a node in the decision tree
	 * @param maxDepth	the depth to expand the tree to
	 * @param pBoard	the probability board decribing the best next cell to consider
	 * @param a			
	 * @param b
	 * @returns the best move to make from the tree exploration
	 */
	public Node<GameState> DLSBuildAB(Tree<GameState> tree, Node<GameState> node,
			int maxDepth, Board pBoard, int a, int b) {
		nodes++;
		
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
	
	/**
	 * generates zobrist keys for the hashtable
	 * @param l
	 * @param b the board state to be hashed
	 */
	public void generateZobristKeys(TreeSet<Long> l, Board b) {
		for (int i = 0 ; i < 7; i++) {
			Board t = b.transform(i);
			long key = t.hashKey();
			if (!l.contains(key)) {
				l.add(key);
			}
		}
	}
	
	public void updateProbabilities(Point p, float weight, int player, Point offset, int pieces, ArrayList<Point> explored) {
		int x = p.x;
		int y = p.y;
		int size = mainBoard.getBoardSize();
		
		boolean ourMove = (player == playerID) ? true : false;
		
		float multiplier = (ourMove) ? 1.0f:0.9f;
		
		System.out.println("Point: "+p.x+","+p.y+" - weight: "+weight);
		
		HashMap<Point, Float> next = new HashMap<Point, Float>();
			
		for (int j = -1; j <= 1; j++) {
			for (int i = -1; i <= 1; i++) {
				int nX = x+i;
				int nY = y+j;
				if (i == 0 && j == 0) {
					continue;
				} else if (Math.abs(i) == 1 && Math.abs(j) == 1) { // Diagonals
					if (mainBoard.getValueAtPosition(nX, nY) == player) { // Two Diagonal
						if (mainBoard.getValueAtPosition(nX+i, nX+i) == player) { // Three diagonals in row!!!
							updateProbability(nX-i, nY+j, 40*multiplier);
							updateProbability(nX+i, nY-j, 40*multiplier);
						} else {
							updateProbability(nX+i, nY+j, 20*multiplier);
						}
					} else { //Only cell
						updateProbability(x+i, y+j, 5*multiplier);
					}
				} else {
					updateProbability(x+i, y+j, 3*multiplier);		
				}
			}
		}
		
		/*if (next.size() <= 0 || mainBoard.getValueAtPosition(x, y) != player) {
			for (int j = -1; j <= 1; j++) {
				for (int i = -1; i <= 1; i++) {
					if (i == 0 && j == 0) {
						continue;
					}
					int nX = x+i;
					int nY = y+i;
					if (mainBoard.getValueAtPosition(nX, nY) == Cell.EMPTY) {
						permanent[nY*size + nX].setProbability(weight);
					}
				}
			}
			return;
		}
		*/
		explored.add(p);
		
		LinkedHashMap<Point, Float> sorted = (LinkedHashMap<Point, Float>) Misc.sortByValue(next);
		ListIterator<Map.Entry<Point, Float>> iterator = new ArrayList<Map.Entry<Point, Float>>(sorted.entrySet()).listIterator(sorted.size());
		while (iterator.hasPrevious()) {
			Map.Entry<Point, Float> entry = iterator.previous();
			Point a = entry.getKey();
			float v = entry.getValue();
			if (!explored.contains(a)) {
				Point o = new Point(a.x - x, a.y - y);
				updateProbabilities(a, v, player, o, pieces+1, explored);
			}
		}
	}
	
	public void updateProbability(int x, int y, float value) {
		if (mainBoard.onBoard(x,y) && mainBoard.getValueAtPosition(x, y) == Cell.EMPTY) {
			int size = mainBoard.getBoardSize();
			permanent[y*size + x].incrementProbability(value);
		}
	}
	
	/**
	 * makes a move on the board and updates the probability board for next best cell to check
	 * @param gs the relevant information to make the move
	 */
	public void makeMove(GameState gs) {
		// makes the move and updates the board for captures
		mainBoard.setCell(gs.getMove());
		mainBoard.checkCaptures(gs, scoreBoard, currentPlayer);
		
		permanent[gs.getMove().getY()*mainBoard.getBoardSize() + gs.getMove().getX()].setProbability(0);
		
		ArrayList<Point> explored = new ArrayList<Point>();
		
		updateProbabilities(new Point(gs.getMove().getX(), gs.getMove().getY()), 5, gs.getMove().getPlayer(), new Point(0, 0), 0, explored);
		
		System.arraycopy(permanent, 0, cellProbabilities, 0, permanent.length);
		
		Arrays.sort(cellProbabilities);
		
		int size = mainBoard.getBoardSize();
		
		for(int j = 0; j < size; j++) {
			for (int i = 0; i < size; i++) {
				if (permanent[j*size + i].getProbability() <= 0.0) {
					System.out.print(Cell.toChar(mainBoard.getValueAtPosition(i, j)) + "\t");
				} else {
					System.out.print(permanent[j*size + i].getProbability() + "\t");
				}
			}
			System.out.println();
		}
		
		/*GameMove m = gs.getMove();
=======
		//Updates the cell utility scores (how likely they are to lead to a beneficial state)
		// only changes local cells
		GameMove m = gs.getMove();
>>>>>>> branch 'master' of https://github.com/rhysw99/squatterB.git
		int x = m.getX();
		int y = m.getY();
		int size = mainBoard.getBoardSize();
		for (int j = -1; j <= 1; j++) {
			for (int i = -1; i <= 1; i++) {
				int nX = x + i;
				int nY = y + j;
				
				if (nX < 0 || nY < 0 || nX >= size || nY >= size) {
					continue;
				}
				
				if (mainBoard.isOccupied(nX, nY)) {
					continue;
				}
				float multiplier = 1.0f;
				if (mainBoard.getValueAtPosition(x, y) == opponentID) {
					multiplier = 1.0f;
				}
				if (i == 0 && j == 0) {
					permanent[nY*size + nX].setProbability(0);
				} else if (Math.abs(i) == 1 && Math.abs(j) == 1) {
					permanent[nY*size + nX].incrementProbability(20*multiplier);
				} else if (Math.abs(i) == 1 || Math.abs(j) == 1){
					permanent[nY*size + nX].incrementProbability(5*multiplier);
				}
			}
		}
		
<<<<<<< HEAD
		for(int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				System.out.print(permanent[i*size + j].getProbability() + "   ");
			}
			System.out.println();
		}
		
		System.arraycopy(permanent, 0, cellProbabilities, 0, permanent.length);
		
		Arrays.sort(cellProbabilities);*/
		
		/*for (int i = 0; i < cellProbabilities.length; i++) {
			System.out.print(cellProbabilities[i].getProbability() + " ");
		}*/

	}
	
	/**
	 * returns the mainBoard
	 * @return
	 */
	// TODO is this used?
	public Board getBoard() {
		return mainBoard;
	}
}
