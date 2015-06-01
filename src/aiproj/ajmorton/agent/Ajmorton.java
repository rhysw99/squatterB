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
import java.util.Iterator;
import java.util.TreeSet;

import aiproj.ajmorton.agent.board.Board;
import aiproj.ajmorton.agent.board.ScoreBoard;
import aiproj.ajmorton.agent.decisionTree.GameMove;
import aiproj.ajmorton.agent.decisionTree.GameState;
import aiproj.ajmorton.agent.decisionTree.Tree;
import aiproj.ajmorton.agent.decisionTree.Tree.*;
import aiproj.squatter.*;

/**
 * The player AI
 * interfaces with the referee to run the game
 *
 */
public class Ajmorton implements Player, Piece {
	
	public static final int FAILURE = -1;
	public static final int SUCCESS = 0;
	public static int MAX_PLY = 4;//the depth the decision tree can explore to
	
	public static final boolean DEBUG = false;

	private int playerID;// the id assigned to the player (black or white)
	private int opponentID;// the id assigned to the opponent
	
	private int currentPlayer; // whose move it currently is
	private int currentMove;
	
	private Board mainBoard; // the board the pieces are placed on
	// a scoremap of the board used for pathfinding
	private ScoreBoard scoreBoard;
	
	private ProbabilityCell[] cellProbabilities;
	private ProbabilityCell[] permanent;
		
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
		
		// is the board within reasonable expectations?
		if (n < 4 || n > 9) {
			System.err.println("Invalid board size ("+n+").  "
					+ "Program terminating!");
			return FAILURE;
		}
		
		if (n <= 6) {
			// 6 ply can be achieved most of the time, but just to be safe
			// we are limiting it to 4
			MAX_PLY = 4;
		} else {
			MAX_PLY = 4;
		}
		
		this.playerID = p;
		this.opponentID = (p == Cell.WHITE) ? Cell.BLACK : Cell.WHITE;
		this.mainBoard = new Board((byte) n);
		this.scoreBoard = new ScoreBoard((byte) n);
		
		scoreBoard.printBoard();
		
		this.cellProbabilities = new ProbabilityCell[n*n];
		
		this.permanent = new ProbabilityCell[n*n];
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < n; i++) {
				permanent[j*n + i] = new ProbabilityCell(i, j);
			}
		}
		for (int j = 1; j < n-1; j++) {
			for (int i = 1; i < n-1; i++) {
				permanent[j*n + i].setProbability(4);
			}
		}
		
		System.arraycopy(permanent, 0, cellProbabilities, 0,permanent.length);
		
		Arrays.sort(cellProbabilities);
		
		this.currentPlayer = Cell.WHITE;
		
		Misc.init(mainBoard.getBoardSize());
		
		return SUCCESS;
	}

	/**
	 * makes a move when requested by the referee
	 * returns a Move object with the relevant information
	 */
	// TODO add comments on operation
	public Move makeMove() {		
		Tree<GameState> decisionTree =
				new Tree<GameState>(new GameState(null, null));
	
		TreeSet<Long> previousNodes = new TreeSet<Long>();
		DLSBuildAB(decisionTree, decisionTree.getRoot(), previousNodes,
				MAX_PLY, Board.copy(mainBoard), Integer.MIN_VALUE,
				Integer.MAX_VALUE);
		
		Node<GameState> best = null;

		ArrayList<Node<GameState>> i = decisionTree.getRoot().getChildren();
		for (Node<GameState> n : i) {
			if (best == null || n.getData().getScore() >
					best.getData().getScore()) {
				best = n;
			}
		}
		
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

	/**
	 * prints the board state to the ouput, pieces are repreresented by B,W,b,w,+ or -
	 */
	public void printBoard(PrintStream output) {
		byte[][] b = mainBoard.getBoard();
		for (int j = 0; j < mainBoard.getBoardSize(); j++) {
			for (int i = 0; i < mainBoard.getBoardSize(); i++) {
				output.print(Cell.toChar(b[j][i]) + " ");
			}
			output.println();			
		}
		
	}
	
	/**
	 * Calculates the score of a given node
	 * @param node Node to evaluate
	 * @param player Player who is making the next move
	 */
	public void calculateScore(Node<GameState> node, int player) {
		int[] captures = node.getData().getCaptures();
		
		int opponent = (playerID == Cell.WHITE) ? Cell.BLACK : Cell.WHITE;
		int ourCaptures = captures[player];
		int enemyCaptures = captures[opponent];
		
		node.getData().setScore((ourCaptures-enemyCaptures) * 100);
	}
	

	/**
	 * Builds the decision tree for finding the next best move to make
	 * @param tree		the decision tree
	 * @param node		a node in the decision tree
	 * @param maxDepth	the depth to expand the tree to
	 * @param pBoard	the probability board describing the best next
	 *  cell to consider
	 * @param a			
	 * @param b
	 * @returns the best move to make from the tree exploration
	 */
	public Node<GameState> DLSBuildAB(Tree<GameState> tree, 
			Node<GameState> node, TreeSet<Long> previousNodes, int maxDepth, Board pBoard, int a, int b) {
		
		if (maxDepth <= 0 || 
				node.getData().getDepth() == mainBoard.getFreeSpaces()) {
			calculateScore(node, currentPlayer);
			pBoard = null;
			return node;
		}
		
		/* Determine which player is making the next move */
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
		
		/* Loop through the highest 'value' cell */
		for (int k = cellProbabilities.length-1; k >= 0; k--) {
			int j = cellProbabilities[k].getY();
			int i = cellProbabilities[k].getX();
			if (pBoard.isLegal(i, j)) {
				if (tBoard == null) {
					tBoard = Board.copy(pBoard);
				}
				tBoard.setCell(i, j, (byte) p);
				
				/*long key = tBoard.hashKey();
				if (previousNodes.contains(key)) {
					continue;
				} else {
					previousNodes.add(key);
					generateZobristKeys(previousNodes, tBoard);
				}*/
			
				GameMove gm = new GameMove(p, i, j);
				GameState gs = new GameState(node.getData(), gm);
				Node<GameState> newNode = new Node<GameState>(gs, node);
				newNode.getData().setDepth(node.getData().getDepth()+1);
				boolean captures = tBoard.checkCaptures(gs, scoreBoard, p,
						false);
				
				
				/* alpha beta pruning */
				if (maximizingPlayer) {
					Node<GameState> child = DLSBuildAB(tree, newNode,
							previousNodes, maxDepth-1, tBoard, a, b);
					best = (child.getData().getScore() >
						best.getData().getScore()) ? child : best;
					a = (a > best.getData().getScore()) ?
							a:best.getData().getScore();
					if (b <= a) {
						break;
					}
				} else {
					Node<GameState> child = DLSBuildAB(tree, newNode, 
							previousNodes, maxDepth-1, tBoard, a, b);
					best = (child.getData().getScore() <
							best.getData().getScore()) ? child : best;
					b = (b < best.getData().getScore()) ?
							b : best.getData().getScore();
					if (b <= a) {
						break;
					}
				}
				
				if (captures) {
					tBoard = null;
				}
				if (newNode.getData().getDepth() == 1) {
					tree.getRoot().getChildren().add(newNode);
				}
				// If we haven't modified the board via captures
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
	 * Generates Zobrist keys for the transposition table
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
	
	/** 
	 * Every time a move is made the probability board is updated to reflect
	 * the next possible best moves so that our alpha beta algorithm is
	 * as effective as possible.
	 * @param p Position of last move made
	 * @param player Player who made last move
	 * @param explored ArrayList containing all cells that have been updated
	 */
	public void updateProbabilities(Point p, int player,
			ArrayList<Point> explored) {
		int x = p.x;
		int y = p.y;
		
		boolean ourMove = (player == playerID) ? true : false;
		
		float multiplier = (ourMove) ? 1.5f:1.0f;
		
		ArrayList<Point> emptyCells = new ArrayList<Point>();

		for (int j = -1; j <= 1; j++) {
			for (int i = -1; i <= 1; i++) {
				int nX = x+i;
				int nY = y+j;
				if (i == 0 && j == 0) {
					continue;
				} else if (Math.abs(i) == 1 && Math.abs(j) == 1) {//Diagonals
					// Two Diagonal
					if (mainBoard.getValueAtPosition(nX, nY) == player) {
						if (mainBoard.getValueAtPosition(nX+i, nX+i)
								== player) { // Three diagonals in row!!!
							updateProbability(nX-i, nY+j, 40*multiplier);
							updateProbability(nX+i, nY-j, 40*multiplier);
						} else if (mainBoard.getValueAtPosition(nX, nY)
								== Cell.EMPTY) {
							updateProbability(nX+i, nY+i, 20*multiplier);
						}
					} else if (mainBoard.getValueAtPosition(nX+i, nY+j)
							== Cell.EMPTY) { //Only cell
						updateProbability(nX, nY, 6*multiplier);
					} else {
						emptyCells.add(new Point(nX, nY));
						updateProbability(nX, nY, 5*multiplier);
					}
				} else {
					emptyCells.add(new Point(nX, nY));
					updateProbability(nX, nY, 1*multiplier);		
				}
			}
		}
		
		ArrayList<Point> playerPieces = new ArrayList<Point>();
		Iterator<Point> it = emptyCells.iterator();
		while (it.hasNext()) {
			Point e = it.next();
			if (!mainBoard.onBoard(e)) {
				continue;
			}
			for (int j = -1; j <= 1; j++) {
				for (int i = -1; i <= 1; i++) {
					if (emptyCells.contains(new Point(e.x+i, e.y+j))) {
						continue;						
					}
					if (mainBoard.getValueAtPosition(e.x+i, e.y+j) == EMPTY) {
						for (int b = -1; b <= 1; b++) {
							for (int a = -1; a <= 1; a++) {
								if (mainBoard.getValueAtPosition
										(e.x+i+a, e.y+j+b) == player) {
									if (e.x+i+a == p.x && e.y+j+b == p.y) {
										continue;
									}
									Point pl = new Point(e.x+i+a, e.y+j+b);
									// Two empty cells sharing adjacent
									// player cell
									if (playerPieces.contains(pl)) {
										updateProbability(e.x, e.y,
												20/multiplier);
										playerPieces.remove(pl);
									} else {
										playerPieces.add(pl);
									}
								}
							}
						}
					}
				}
			}
		}
	
	}
	
	/**
	 * Increase the probability of a given cell
	 * @param x X coordinate to modify
	 * @param y Y coordinate to modify
	 * @param value Value to increase by
	 */
	public void updateProbability(int x, int y, float value) {
		if (mainBoard.onBoard(x,y) &&
				mainBoard.getValueAtPosition(x, y) == Cell.EMPTY) {
			int size = mainBoard.getBoardSize();
			if (permanent[y*size + x].getProbability() != 4.0) {
				permanent[y*size + x].incrementProbability(value);
			} else {
				setProbability(x, y, value);
			}
		}
	}
	
	/**
	 * Sets the probability of a given cell
	 * @param x X coordinate to modify
	 * @param y Y coordinate to modify
	 * @param value Value to set
	 */
	public void setProbability(int x, int y, float value) {
		if (mainBoard.onBoard(x,y) &&
				mainBoard.getValueAtPosition(x, y) == Cell.EMPTY) {
			int size = mainBoard.getBoardSize();
			permanent[y*size + x].setProbability(value);
		}
	}
	
	/**
	 * makes a move on the board and updates the probability board for next
	 * best cell to check
	 * @param gs the relevant information to make the move
	 */
	public void makeMove(GameState gs) {
		// makes the move and updates the board for captures
		mainBoard.setCell(gs.getMove());
		mainBoard.checkCaptures(gs, scoreBoard, currentPlayer, true);
		
		permanent[gs.getMove().getY()*mainBoard.getBoardSize()
		          + gs.getMove().getX()].setProbability(0);
		
		ArrayList<Point> explored = new ArrayList<Point>();
		
		updateProbabilities(new Point(gs.getMove().getX(), 
				gs.getMove().getY()), gs.getMove().getPlayer(), explored);
		
		System.arraycopy(permanent, 0, cellProbabilities,0,permanent.length);
		
		Arrays.sort(cellProbabilities);
		
	}

}
