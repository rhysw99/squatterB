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
		currentPlayer = ((currentPlayer == Pieces.WHITE) ? Pieces.BLACK : Pieces.WHITE);

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
	
	public Tree<GameState> buildTree() {
		Tree<GameState> decisionTree = new Tree<GameState>(new GameState(null, null));
		Root<GameState> root = decisionTree.getRoot();
		
		
		//System.out.println("CLIENT: "+playerID);
		//System.out.println("Current: "+currentPlayer);

		int p = currentPlayer;
		int depth = currentMove;
		
		TreeSet<Long> previousNodes = new TreeSet<Long>();
		TreeSet<Long> previousUselessNodes = new TreeSet<Long>();

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
		
		Board parentBoard = Board.copy(mainBoard);
		
		while (!nodes.isEmpty()) {
			Node<GameState> currentNode = nodes.pop();

			if (currentNode.getData().getDepth() == maxDepth) {
				cycleUp(currentNode);
				continue;
			}
			
			///System.out.println("@@@@@@@@@@@@@@@@@@");
			
			//parentBoard.printBoard();
			
			/* Set board with all parent moves */
			Node<GameState> parentNode = currentNode.getParent();
			while (parentNode != null) {
				if (parentNode.getData() != null) {
					if (parentNode.getData().getMove() != null) {
						//System.out.println("Added Depth: "+parentNode.getData().getDepth());
						//System.out.println("i: "+parentNode.getData().getMove().getX() + " - j: "+parentNode.getData().getMove().getY() + " - p: "+parentNode.getData().getMove().getPlayer());
						parentBoard.setCell(parentNode.getData().getMove());
					} else {
						//System.out.println("Depth: "+parentNode.getData().getDepth());
						//System.out.println("Stopped");
					}
				}
				parentNode = parentNode.getParent();
			}
			
			//parentBoard.printBoard();
			
			long parentCode = parentBoard.hashKey();
			
			Board tBoard = Board.copy(parentBoard);
			
			//System.out.println("=======================");
			
			for (int j = 0; j < mainBoard.getBoardSize(); j++) {
				for (int i = 0; i < mainBoard.getBoardSize(); i++) {
					GameMove m = new GameMove(p, i, j);
					if (tBoard == null) {
						tBoard = Board.copy(parentBoard);
					}
					if (tBoard.isLegal(m)) {
						long childCode = parentCode;
						
						//System.out.println("--------");
						//tBoard.printBoard();
						
						tBoard.setCell(m);
				
						
						totalNodes++;
						
						childCode = tBoard.hashKey(); // Append move to code
						
						//System.out.println(childCode+ " - Depth: "+currentNode.getData().getDepth()+ " - i: "+i+" - j: "+j+ " - p:"+p);
						
						//tBoard.printBoard();
											
						if (previousNodes.contains(childCode)) {
							//System.out.println("duplicateA");
							//tBoard.printBoard();
							continue;
						} else if (previousUselessNodes.contains(childCode)) {
							//System.out.println("duplicateB");
							//tBoard.printBoard();
							previousUselessNodes.remove(childCode);
							previousNodes.add(childCode);
							continue;
						}
						
						reducedNodes++;
						
						leaves[currentNode.getData().getDepth()]++;
						
						generateHashCodes(previousUselessNodes, tBoard, currentNode.getData().getDepth());
						
						if (currentNode.getData().getDepth() == (maxDepth-1)) {
							leafNodes++;
						}
												
						GameState newGS = new GameState(root.getData(), m);
						Node<GameState> newNode = new Node<GameState>(newGS, currentNode);
						newNode.getData().setDepth(currentNode.getData().getDepth() + 1);
						
						if (currentNode.getData().getDepth() == (maxDepth-1)) {
							boolean boardChanged = tBoard.checkCaptures(m, scoreBoard);
							newNode.getData().calculateScore(tBoard);
							//System.out.println("Score: "+newNode.getData().getScore()+ " - Depth: "+newNode.getData().getDepth());
							if (boardChanged) {
								tBoard = null; // If we captured any cells we need to reset the board to the parent state
							}
						}
						currentNode.insert(newNode);
						nodes.add(newNode);

						
						// If the board exists then we didn't make any captures on the last node and therefore can simply reset
						// the cell
						if (tBoard != null) {
							tBoard.resetCell(m);
						}
					} else {
						//System.out.println("DECLINEDDDDDD");
					}
				}
			}
			// END OF TURN
			p = ((p == Pieces.WHITE) ? Pieces.BLACK : Pieces.WHITE);
			
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
		
/*
		Collection<Integer> sf = previousNodes.values();
		ArrayList<Integer> sort = new ArrayList<Integer>();		
		for (Integer i : sf) {
			sort.add(i);
		}
		Collections.sort(sort);
		Collections.reverse(sort);
		int zero = 0;
		int nonzero = 0;
		
		for (Integer i : sort) {
			if (i > 0) {
				nonzero++;
				System.out.println(i);
			} else {
				zero++;
			}
		}
		
		System.out.println("Zero: "+zero);
		System.out.println("Non zero: "+nonzero);
		
		for (int i = 0; i < leaves.length; i++) {
			if (leaves[i] > 0) {
				//System.out.println("Depth: "+i+ " - Leaves: "+leaves[i]);
			}
		}
		*/
		System.out.println("Total nodes: "+totalNodes);
		System.out.println("Reduced nodes: "+reducedNodes);
		System.out.println("Leaf nodes: "+leafNodes);

		return decisionTree;
	}
	
	private void generateHashCodes(TreeSet<Long> l, Board b, int depth) {
		long code = b.hashKey();
		if (!l.contains(code)) {
			l.add(code);
		}
		
		/*for (int i = 0; i < 7; i++) {
			Board tBoard = b.transform(i);
			code = tBoard.hashKey();
			if (!l.contains(code)) {
				l.add(code);
			}
		}*/
		
		if (l.size() > 100000) {
			trimNodeMap(l);
		}
	}
	
	private void trimNodeMap(TreeSet<Long> l) {
		l.clear();
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
