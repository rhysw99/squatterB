package aiproj.agent;


import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import aiproj.agent.board.Board;
import aiproj.agent.board.ScoreBoard;
import aiproj.agent.board.Update;
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

	private int player;
	
	private static Board mainBoard;
	private static ScoreBoard scoreBoard;
	
	public static void main(String[] args) {
		System.out.println("test");
		Ajmorton aj = new Ajmorton();
		aj.init(5, 1);
		
		aj.buildTree();
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
		
		HashMap<Integer, ArrayList<Node<GameState>>> leaves = new HashMap<Integer, ArrayList<Node<GameState>>>();
		/* Generate empty leaves ArrayLists */
		for (int i = 0; i < Math.pow(mainBoard.getBoardSize(), 2); i++) {
			ArrayList<Node<GameState>> l = new ArrayList<Node<GameState>>();
			leaves.put(i, l);
		}
		
		Board tBoard = new Board(mainBoard.getBoard(), mainBoard.getBoardSize());
		
		int p = Piece.BLACK;
		int depth = 0;
		leaves.get(depth).add(root);
		
		for (int d = depth; d < depth + MAX_PLY; d++) {
			while (!leaves.get(d).isEmpty()) {
				Node<GameState> currentNode = leaves.get(d).get(0);
				leaves.get(d).remove(0);
				
				Node<GameState> parentNode = currentNode;
				while (parentNode != null) {
					if (parentNode.getData() != null) {
						tBoard.setCell(parentNode.getData().getMove());
					}
					parentNode = parentNode.getParent();
				}
				for (int j = 0; j < mainBoard.getBoardSize(); j++) {
					for (int i = 0; i < mainBoard.getBoardSize(); i++) {
						//TODO Switch to new move class?
						GameMove m = new GameMove(p, new Point(i, j));
						
						if (mainBoard.isLegal(m)) {
							tBoard.setCell(m);
							if (DEBUG) System.out.println("d: "+d+" - j: "+j+" - i: "+i);
							if (DEBUG) tBoard.printBoard();
							
							GameState newGS = new GameState(root.getData(), m);
							Node<GameState> newNode = new Node<GameState>(newGS, currentNode);
							currentNode.insert(newNode);
							leaves.get(d+1).add(newNode);
							tBoard.resetCell(m);
						}
						try {
							if (DEBUG) Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				parentNode = currentNode;
				while (parentNode != null) {
					if (parentNode.getData() != null) {
						tBoard.resetCell(parentNode.getData().getMove());
					}
					parentNode = parentNode.getParent();
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
