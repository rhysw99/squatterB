package aiproj.agent;


import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
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
	
	public static final int MAX_PLY = 32;
	
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
				
		boolean playerCorrect = ((p == BLACK) || (p == WHITE));
		boolean sizeCorrect   = ((n == 6) || (n == 7)); // Is this only cases?			

		if (!playerCorrect || !sizeCorrect) {
			return FAILURE;
		}
		
		this.playerID = p;
		this.opponentID = (p == Piece.WHITE) ? Piece.BLACK : Piece.WHITE;
		this.mainBoard = new Board((byte) n);
		this.scoreBoard = new ScoreBoard((byte) n);

		this.moves = new ArrayList<GameMove>();
		
		this.currentPlayer = Piece.WHITE;
		
		Miscellaneous.init();
		
		return SUCCESS;
	}

	
	public Move makeMove() {
		GameMove gm;
		Tree<GameState> decisionTree = buildTree();

		int maxDepth = currentMove+MAX_PLY;
		maxDepth = (maxDepth > mainBoard.getBoardSpaces() ? mainBoard.getBoardSpaces() : maxDepth);

		Iterator<Node<GameState>> it = decisionTree.getRoot().getChildren().iterator();
		Node<GameState> best = null;

		while (it.hasNext()) {
			Node<GameState> n = it.next();
			//System.out.println("Current score: "+n.getData().getScore());
			if (best == null || n.getData().getScore() > best.getData().getScore()) {
				best = n;
			}
		}

		//System.out.println("Best score: "+best.getData().getScore());
		gm = best.getData().getMove();

		makeMove(gm);

		currentMove++;
		currentPlayer = ((currentPlayer == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);

		return GameMove.getMove(gm);
	}
	
	private void cycleUp(Node<GameState> node) {
		int depth = node.getData().getDepth();
		//System.out.println("Player: "+playerID);
		while (depth > currentMove+1) {
			Node<GameState> parent = node.getParent();
			//System.out.println("Move: "+parent.getData().getMove().getPlayer());
			//System.out.println("PA Before: "+parent.getData().getScore());
			boolean ourTurn = (parent.getData().getMove().getPlayer() != playerID);
			if (ourTurn) {
				//System.out.println("MAX");
				if (node.getData().getScore() > parent.getData().getScore()) {
					parent.getData().setScore(node.getData().getScore());
				}
			} else {
				//System.out.println("MIN");
				if (parent.getData().getScore() == Integer.MIN_VALUE || node.getData().getScore() < parent.getData().getScore()) {
					parent.getData().setScore(node.getData().getScore());
				}
			}
			//System.out.println("PA After: "+parent.getData().getScore());
			//System.out.println("=================");
			node = parent;
			depth--;
		}
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
		currentPlayer = ((currentPlayer == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);
		
		
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
	
	public Tree<GameState> buildTree() {
		Tree<GameState> decisionTree = new Tree<GameState>(new GameState(null, null));
		Root<GameState> root = decisionTree.getRoot();
		
		//System.out.println("CLIENT: "+playerID);
		//System.out.println("Current: "+currentPlayer);

		int p = currentPlayer;
		int depth = currentMove;
		
		TreeSet<Integer> previousNodes = new TreeSet<Integer>();

		int leafNodes = 0;

		root.getData().setDepth(depth);
		
		Stack<Node<GameState>> nodes = new Stack<Node<GameState>>();
		nodes.push(root);
		
		int totalNodes = 0;
		int reducedNodes = 0;
		
		
		int maxDepth = depth + MAX_PLY;
		maxDepth = (maxDepth > mainBoard.getBoardSpaces() ? mainBoard.getBoardSpaces() : maxDepth);
		//System.out.println("Max depth: "+maxDepth);
		int[] leaves = new int[maxDepth];
		
		int[] bestScore = new int[maxDepth];
		for (int i = 0; i < bestScore.length; i++) {
			bestScore[i] = Integer.MIN_VALUE;
		}
		
		Board parentBoard = Board.copy(mainBoard);
		
		while (!nodes.isEmpty()) {
			Node<GameState> currentNode = nodes.pop();

			if (currentNode.getData().getDepth() == maxDepth) {
				cycleUp(currentNode);
				continue;
			}
			
			/* Set board with all parent moves */
			Node<GameState> parentNode = currentNode.getParent();
			while (parentNode != null) {
				if (parentNode.getData() != null) {
					if (parentNode.getData().getMove() != null) {
						parentBoard.setCell(parentNode.getData().getMove());
					}
				}
				parentNode = parentNode.getParent();
			}
			
			
			Board tBoard = null;
			
			for (int j = 0; j < mainBoard.getBoardSize(); j++) {
				for (int i = 0; i < mainBoard.getBoardSize(); i++) {
					GameMove m = new GameMove(p, new Point(i, j));

					if (parentBoard.isLegal(m)) {
						if (tBoard == null) {
							tBoard = Board.copy(mainBoard);
						}
						
						tBoard.setCell(m);
						
						totalNodes++;
						
						int code = tBoard.hashCodeString(currentNode.getData().getDepth());
											
						if (previousNodes.contains(code)) {
							//System.out.println("Duplicate node");
							continue;
						}
						
						reducedNodes++;
						
						leaves[currentNode.getData().getDepth()]++;
						
						if (currentNode.getData().getDepth() == (maxDepth-1)) {
							leafNodes++;
							generateHashCodes(previousNodes, tBoard, currentNode.getData().getDepth());
						}
												
						GameState newGS = new GameState(root.getData(), m);
						Node<GameState> newNode = new Node<GameState>(newGS, currentNode);
						newNode.getData().setDepth(currentNode.getData().getDepth() + 1);
						
					
						boolean boardChanged = tBoard.checkCaptures(m, scoreBoard);
						newNode.getData().calculateScore(tBoard);
						//System.out.println("Score: "+newNode.getData().getScore()+ " - Depth: "+newNode.getData().getDepth());
						if (newNode.getData().getScore() > bestScore[currentNode.getData().getDepth()]) {
							bestScore[currentNode.getData().getDepth()] = newNode.getData().getScore();
							currentNode.insert(newNode);
							nodes.add(newNode);
						}
						
						if (boardChanged) {
							tBoard = null; // If we captured any cells we need to reset the board to the parent state
						}
													
						// If the board exists then we didn't make any captures on the last node and therefore can simply reset
						// the cell
						if (tBoard != null) {
							tBoard.resetCell(m);
						}
					}
				}
			}
			// END OF TURN
			p = ((p == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);
			
			// Clean up child nodes
			if (currentNode.getData().getDepth() > currentMove) {
				currentNode.setChildren(null);
			}
			
			parentNode = currentNode.getParent();
			// Reset parent board back to original state
			while (parentNode != null) {
				if (parentNode.getData() != null) {
					if (parentNode.getData().getMove() != null) {
						parentBoard.resetCell(parentNode.getData().getMove());
					}
				}
				parentNode = parentNode.getParent();
			}
		}
		
		for (int i = 0; i < leaves.length; i++) {
			if (leaves[i] > 0) {
				//System.out.println("Depth: "+i+ " - Leaves: "+leaves[i]);
			}
		}
		
		System.out.println("Total nodes: "+totalNodes);
		System.out.println("Reduced nodes: "+reducedNodes);
		System.out.println("Leaf nodes: "+leafNodes);

		return decisionTree;
	}
	
	private void generateHashCodes(TreeSet<Integer> l, Board b, int depth) {
		int code = b.hashCodeString(depth);
		if (!l.contains(code)) {
			l.add(code);
		}
		
		//System.out.println("List size:" +l.size());
	}	

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
