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
		GameMove gm;
		Tree<GameState> decisionTree = buildTree();

		Iterator<Node<GameState>> it = decisionTree.getRoot().getChildren().iterator();
		Node<GameState> best = null;

		while (it.hasNext()) {
			Node<GameState> n = it.next();
			if (best == null || n.getData().getScore() > best.getData().getScore()) {
				best = n;
			}
		}
		
		gm = best.getData().getMove();

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
	
	public int DLSBuildAB(Tree<GameState> tree, Node<GameState> node, int maxDepth, Board pBoard, int a, int b) {		
		if (maxDepth <= 0 || node.getData().getDepth() == mainBoard.getFreeSpaces()) {
			node.getData().calculateScore(null);
			return node.getData().getScore();
		}
			
		int p;		
		if (node.getData().getMove() != null) {
			p = node.getData().getMove().getPlayer();
			p = (p == Pieces.WHITE) ? Pieces.BLACK : Pieces.WHITE;
		} else {
			p = playerID;
		}
		
		boolean maximizingPlayer = (p==Pieces.WHITE);
		
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

				if (maximizingPlayer) {
					int newV = DLSBuildAB(tree, newNode, maxDepth-1, pBoard, a, b);
					v = (newV > v) ? newV : v;
					a = (a > v) ? a:v;
					if (b <= a) {
						break;
					}
				} else {
					int newV = DLSBuildAB(tree, newNode, maxDepth-1, pBoard, a, b);
					v = (newV < v) ? newV : v;
					b = (b < v) ? b:v;
					if (b <= a) {
						break;
					}
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
		
		node.getData().setScore(v);
		
		return v;
	}
	
	public Tree<GameState> buildTree() {
		Tree<GameState> decisionTree = new Tree<GameState>(new GameState(null, null));
		DLSBuildAB(decisionTree, decisionTree.getRoot(), MAX_PLY, Board.copy(mainBoard), Integer.MIN_VALUE, Integer.MAX_VALUE);
		return decisionTree;
	}

	public void makeMove(GameMove m) {
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
