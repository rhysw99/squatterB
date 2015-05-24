package aiproj.agent;


import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

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
	
	private int currentPlayer;
	
	private int currentMove;
	
	private Board mainBoard;
	private ScoreBoard scoreBoard;
	
	private ArrayList<GameMove> moves;
	
	public static void main(String[] args) {
		System.out.println("test");
		/*
		Ajmorton aj = new Ajmorton();
		aj.init(5, 1);
		
		while (!aj.mainBoard.isFull()) {
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
			System.out.println("=================");
		}
		aj.mainBoard.printBoard();
		*/
		
		Ajmorton aj = new Ajmorton();
		aj.init(5, 1);
		System.out.println("    \n");
		aj.mainBoard.printBoard();
		
		aj.mainBoard.updateBoard(new GameMove(Piece.BLACK, new Point(1, 1)), aj.scoreBoard);
		System.out.println("\nafter    \n");
		aj.mainBoard.printBoard();

		
		
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
		
		this.playerID = p;
		this.mainBoard = new Board((byte) n);
		this.scoreBoard = new ScoreBoard((byte) n);
		mainBoard.setCell(new GameMove(Piece.BLACK, new Point(3, 2)));
		mainBoard.setCell(new GameMove(Piece.BLACK, new Point(3, 3)));
		mainBoard.setCell(new GameMove(Piece.BLACK, new Point(0, 3)));
		mainBoard.setCell(new GameMove(Piece.BLACK, new Point(2, 1)));
		mainBoard.setCell(new GameMove(Piece.BLACK, new Point(2, 4)));
		mainBoard.setCell(new GameMove(Piece.BLACK, new Point(0, 2)));

		this.moves = new ArrayList<GameMove>();
		
		this.currentPlayer = Piece.WHITE;
		
		return SUCCESS;
	}

	
	public Move makeMove() {
		GameMove gm;
		if (currentMove == 0) {
			gm = new GameMove(Piece.WHITE, new Point(mainBoard.getBoardSize()/2, mainBoard.getBoardSize()/2));
		} else {
			Tree<GameState> decisionTree = buildTree();
			
			int maxDepth = currentMove+MAX_PLY;
			maxDepth = (maxDepth > mainBoard.getBoardSpaces() ? mainBoard.getBoardSpaces() : maxDepth);
			
			Iterator<Node<GameState>> it = decisionTree.getRoot().getChildren().iterator();
			Node<GameState> best = null;
	
			while (it.hasNext()) {
				Node<GameState> n = it.next();
				if (best == null || n.getData().getScore() > best.getData().getScore()) {
					best = n;
				}
			}
			gm = best.getData().getMove();
		}
		
		makeMove(gm);
		
		currentMove++;
		currentPlayer = ((currentPlayer == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);
		
		return GameMove.getMove(gm);
	}
	
	private void cycleUp(Node<GameState> node) {
		int depth = node.getData().getDepth();
		while (depth > currentMove) {
			Node<GameState> parent = node.getParent();
			if (node.getData().getScore() < parent.getData().getScore()) {
				parent.getData().setScore(node.getData().getScore());
			}
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
		
		System.out.println("OPPONENT MOVE");
		mainBoard.updateBoard(gm, scoreBoard);
		
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
		
		return 0;
	}
	
	//
	public void printBoard(PrintStream output) {
	//TODO

		
	}
	
	public Tree<GameState> buildTree() {
		Tree<GameState> decisionTree = new Tree<GameState>(new GameState(null, null), new Board(mainBoard.getBoardSize()));
		Root<GameState> root = decisionTree.getRoot();

		int p = currentPlayer;
		int depth = currentMove;

		root.getData().setDepth(depth);
		Stack<Node<GameState>> nodes = new Stack<Node<GameState>>();
		nodes.push(root);
		
		Board tBoard = new Board(mainBoard.getBoard(), mainBoard.getBoardSize());

		int maxDepth = depth + MAX_PLY;
		maxDepth = (maxDepth > mainBoard.getBoardSpaces() ? mainBoard.getBoardSpaces() : maxDepth);
		while (!nodes.isEmpty()) {
			Node<GameState> currentNode = nodes.pop();
			
			//System.out.println("=======");
			//System.out.println("max depth: "+maxDepth);
			//mainBoard.printBoard();
			
			/* Set board for all parent states */
			Node<GameState> parentNode = currentNode.getParent();
			while (parentNode != null) {
				if (parentNode.getData() != null) {
					if (parentNode.getData().getMove() != null) {
						//System.out.println("Parent depth: "+parentNode.getData().getDepth());
						//tBoard.setCell(parentNode.getData().getMove());
						/*try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
					}
				}
				parentNode = parentNode.getParent();
			}

			
			if (currentNode.getData().getDepth() >= maxDepth) {
				cycleUp(currentNode);
				continue;
			}
			
			for (int j = 0; j < mainBoard.getBoardSize(); j++) {
				for (int i = 0; i < mainBoard.getBoardSize(); i++) {
					//TODO Switch to new move class?
					GameMove m = new GameMove(p, new Point(i, j));

					if (tBoard.isLegal(m)) {
						//tBoard.setCell(m);
						if (DEBUG) System.out.println("d: "+depth+" - j: "+j+" - i: "+i);
						if (DEBUG) tBoard.printBoard();

						GameState newGS = new GameState(root.getData(), m);
						Node<GameState> newNode = new Node<GameState>(newGS, currentNode);
						newNode.getData().calculateScore(tBoard);
						//System.out.println("Create node: "+currentNode.getData().getDepth());
						//System.out.println("Max depth: "+maxDepth);
						newNode.getData().setDepth(currentNode.getData().getDepth() + 1);
						if (currentNode.getData().getDepth() == maxDepth) {
							System.out.println("DEPTH ERROR");
						}
						currentNode.insert(newNode);
						nodes.add(newNode);
						//tBoard.resetCell(m);
					}
				}
			}
			/* Reset board for all parent states */
			parentNode = currentNode.getParent();
			while (parentNode != null) {
				if (parentNode.getData() != null) {
					if (parentNode.getData().getMove() != null) {
						//tBoard.resetCell(parentNode.getData().getMove());
					}
				}
				parentNode = parentNode.getParent();
			}
			// END OF TURN
			p = ((p == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);
			
			// Clean up child nodes
			if (currentNode.getData().getDepth() > currentMove) {
				currentNode.setChildren(null);
			}
		}

		return decisionTree;
	}
	
	private void generateChild(Node<GameState> n) {
		
	}

	
	private void checkUniqueTransforms(Node<GameState> currentNode, Board tBoard) {
		GameState gs = currentNode.getData();
		boolean[] transformFlags = gs.getTransformFlags();
		for (int i = 0; i < transformFlags.length; i++) {
			if (transformFlags[i]) {
				Board transBoard = tBoard.transform(i);
				if (Board.checkUniqueStates(tBoard, transBoard)) {
					System.out.println("Unique state");
					//TODO CHECK THIS
					transformFlags[i] = false;
				}
			}
		}
	}
	

	public void makeMove(GameMove m) {
		System.out.println("Move: X:"+m.getLocation().x+" - y: "+m.getLocation().y+" - player: "+m.getPlayer());
		mainBoard.updateBoard(m, scoreBoard);
		moves.add(m);
	}
	
	public void printMoves() {
		for (GameMove m : moves)
			System.out.println("Player: "+m.getPlayer() + " - x: "+m.getLocation().x + " - y: "+m.getLocation().y);
	}
	
	public Board getBoard() {
		return mainBoard;
	}

	public ScoreBoard getScoreBoard() {
		return scoreBoard;
	}
	
}
