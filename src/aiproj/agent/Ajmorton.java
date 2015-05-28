package aiproj.agent;

// TODO
// move Scoring to correct location (find where that is)
// pathfind still needs to update capDifference, need to pass reference to
// fill out comments.txt
// do we use alpha beta?
// scoreMap for 6x6 is likely wrong

import java.io.PrintStream;
import java.util.ArrayList;
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
	
	private ArrayList<GameMove> moves;
	
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

		this.playerID = p;
		this.opponentID = (p == Cell.WHITE) ? Cell.BLACK : Cell.WHITE;
		this.mainBoard = new Board((byte) n);
		this.scoreBoard = new ScoreBoard((byte) n);

		this.moves = new ArrayList<GameMove>();
		
		this.currentPlayer = Cell.WHITE;
		
		Miscellaneous.init(mainBoard.getBoardSize());
		
		return SUCCESS;
	}

	
	public Move makeMove() {
		GameMove gm;

		Tree<GameState> decisionTree = buildTree();

		Iterator<Node<GameState>> it = decisionTree.getRoot().getChildren().
				iterator();
		Node<GameState> best = null;

		while (it.hasNext()) {
			Node<GameState> n = it.next();
			if (best == null ||
					n.getData().getScore() > best.getData().getScore()) {
				System.out.println("Node score: "+n.getData().getScore());
				best = n;
			}
		}
		
		gm = best.getData().getMove();

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

		mainBoard.setCell(gm);
		mainBoard.checkCaptures(gs, scoreBoard, currentPlayer);
		
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
	
	public int DLSBuildAB(Tree<GameState> tree, Node<GameState> node,
			int maxDepth, Board pBoard, int a, int b) {
		if (maxDepth <= 0 ||
				node.getData().getDepth() == mainBoard.getFreeSpaces()) {
			pBoard.checkCaptures(node.getData(), scoreBoard, currentPlayer);
			Scoring.scoreState(node, pBoard, currentPlayer);
			pBoard = null;
			return node.getData().getScore();
		}
			
		int p;		
		if (node.getData().getMove() != null) {
			p = node.getData().getMove().getPlayer();
			p = (p == Cell.WHITE) ? Cell.BLACK : Cell.WHITE;
		} else {
			p = playerID;
		}
		
		boolean maximizingPlayer = (p == Cell.WHITE);
		
		TreeSet<Long> previousNodes = new TreeSet<Long>();
		
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
		
		int v = (maximizingPlayer) ? Integer.MIN_VALUE:Integer.MAX_VALUE;

		for (int k = 0; k < pBoard.getBoardSpaces(); k++) {
			int j = k / pBoard.getBoardSize();
			int i = k - j*pBoard.getBoardSize();
			if (pBoard.isLegal(i, j, p)) {
				if (tBoard == null) {
					tBoard = Board.copy(pBoard);
				}
				tBoard.setCell(i, j, (byte) p);
			
				GameMove gm = new GameMove(p, i, j);
				GameState gs = new GameState(node.getData(), gm);
				Node<GameState> newNode = new Node<GameState>(gs, node);
				newNode.getData().setDepth(node.getData().getDepth()+1);
				boolean captures = tBoard.checkCaptures(gs, scoreBoard, currentPlayer);
			
				/* alpha beta pruning */
				if (maximizingPlayer) {
					int newV = DLSBuildAB(tree, newNode, maxDepth-1,
							tBoard, a, b);
					v = (newV > v) ? newV : v;
					a = (a > v) ? a:v;
					if (b <= a) {
						break;
					}
				} else {
					int newV = DLSBuildAB(tree, newNode, maxDepth-1,
							tBoard, a, b);
					v = (newV < v) ? newV : v;
					b = (b < v) ? b:v;
					if (b <= a) {
						break;
					}
				}
				
				if (captures) {
					tBoard = null;
				}
				
				//generateZobristKeys(previousNodes, tBoard);
				
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
		
		node.getData().setScore(v);
		
		return v;
	}
	
	public Tree<GameState> buildTree() {
		GameState gs = new GameState(null, null);
		Tree<GameState> decisionTree = new Tree<GameState>(gs);
		DLSBuildAB(decisionTree, decisionTree.getRoot(), MAX_PLY,
				Board.copy(mainBoard), Integer.MIN_VALUE, Integer.MAX_VALUE);
		return decisionTree;
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
		//moves.add(m);
	}
	
	public Board getBoard() {
		return mainBoard;
	}

	public ScoreBoard getScoreBoard() {
		return scoreBoard;
	}
	
}
