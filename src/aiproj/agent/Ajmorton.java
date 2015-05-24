package aiproj.agent;


import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

	private int playerID;
	
	private int currentPlayer;
	
	private int currentMove;
	
	private Board mainBoard;
	private ScoreBoard scoreBoard;
	
	private ArrayList<GameMove> moves;
	
	public static void main(String[] args) {
		System.out.println("test");
		Ajmorton aj = new Ajmorton();
		aj.init(5, 1);
		
		while (!aj.mainBoard.isFull()) {
			aj.makeMove();
			GameMove gm = new GameMove(aj.currentPlayer, new Point(1,2));
			while (!aj.mainBoard.isLegal(gm) && !aj.mainBoard.isFull()) {
				int x = (int) Math.round(aj.mainBoard.getBoardSize()*Math.random());
				int y = (int) Math.round(aj.mainBoard.getBoardSize()*Math.random());
				gm = new GameMove(aj.currentPlayer, new Point(x,y));
			}
			aj.opponentMove(GameMove.getMove(gm));
			
			aj.mainBoard.printBoard();
		}
		
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
		mainBoard.setCell(new GameMove(3, new Point(3, 3)));
		mainBoard.setCell(new GameMove(3, new Point(1, 4)));
		mainBoard.setCell(new GameMove(3, new Point(2, 0)));
		mainBoard.setCell(new GameMove(3, new Point(2, 4)));
		mainBoard.setCell(new GameMove(3, new Point(1, 0)));
		this.moves = new ArrayList<GameMove>();
		
		this.currentPlayer = Piece.WHITE;
		
		return SUCCESS;
	}

	
	public Move makeMove() {
		if (currentMove == 0) {
			GameMove gm = new GameMove(Piece.WHITE, new Point(mainBoard.getBoardSize()/2, mainBoard.getBoardSize()/2));
			return GameMove.getMove(gm);
		}
		Tree<GameState> decisionTree = buildTree();
		cycleUp(decisionTree);
		
		int maxDepth = currentMove+MAX_PLY;
		maxDepth = (maxDepth > mainBoard.getBoardSpaces() ? mainBoard.getBoardSpaces() : maxDepth);
		
		Iterator<Node<GameState>> it = decisionTree.getRoot().getChildren().iterator();
		Node<GameState> best = null;

		while (it.hasNext()) {
			Node<GameState> n = it.next();
			if (best == null || n.getData().getScore() > best.getData().getScore()) {
				best = n;
			}
			if (DEBUG) System.out.println("Evaluation function: "+n.getData().getScore());
		}
		GameMove gm = best.getData().getMove();
		
		makeMove(gm);
		
		currentMove++;
		currentPlayer = ((currentPlayer == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);
		
		return GameMove.getMove(gm);
	}
	
	private void cycleUp(Tree<GameState> decisionTree) {
		int maxDepth = currentMove+MAX_PLY;
		maxDepth = (maxDepth > mainBoard.getBoardSpaces() ? mainBoard.getBoardSpaces() : maxDepth);
		
		HashMap<Integer, ArrayList<Node<GameState>>> leaves = decisionTree.getLeaves();
		
		for (int d = maxDepth; d > currentMove; d--) {
			ArrayList<Node<GameState>> it = leaves.get(d);
			while (!it.isEmpty()) {
				Node<GameState> child = it.get(0);
				if (child.getParent() != null) {
					if (child.getParent().getData().getScore() > child.getData().getScore()) {
						child.getParent().getData().setScore(child.getData().getScore());
					}
					if (!leaves.get(d-1).contains(child.getParent())) {
						leaves.get(d-1).add(child.getParent());
					}
				}
				it.remove(0);
			}
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
		
		HashMap<Integer, ArrayList<Node<GameState>>> leaves = new HashMap<Integer, ArrayList<Node<GameState>>>();
		/* Generate empty leaves ArrayLists */
		for (int i = currentMove; i <= currentMove+MAX_PLY; i++) {
			ArrayList<Node<GameState>> l = new ArrayList<Node<GameState>>();
			leaves.put(i, l);
		}
		
		int p = currentPlayer;
		int depth = currentMove;
		
		Board tBoard = new Board(mainBoard.getBoard(), mainBoard.getBoardSize());
		leaves.get(depth).add(root);
			
		int maxDepth = depth + MAX_PLY;
		maxDepth = (maxDepth > mainBoard.getBoardSpaces() ? mainBoard.getBoardSpaces() : maxDepth);
		for (; depth < maxDepth; depth++) {
			while (!leaves.get(depth).isEmpty()) {
				Node<GameState> currentNode = leaves.get(depth).get(0);
				leaves.get(depth).remove(0);

				/* Set board for all parent states */
				Node<GameState> parentNode = currentNode;
				while (parentNode != null) {
					if (parentNode.getData() != null) {
						if (parentNode.getData().getMove() != null) {
							tBoard.setCell(parentNode.getData().getMove());
						}
					}
					parentNode = parentNode.getParent();
				}

				checkUniqueTransforms(currentNode, tBoard);

				for (int j = 0; j < mainBoard.getBoardSize(); j++) {
					for (int i = 0; i < mainBoard.getBoardSize(); i++) {
						//TODO Switch to new move class?
						GameMove m = new GameMove(p, new Point(i, j));

						if (tBoard.isLegal(m)) {
							tBoard.setCell(m);
							if (DEBUG) System.out.println("d: "+depth+" - j: "+j+" - i: "+i);
							if (DEBUG) tBoard.printBoard();

							GameState newGS = new GameState(root.getData(), m);
							Node<GameState> newNode = new Node<GameState>(newGS, currentNode);
							newNode.getData().calculateScore(tBoard);
							currentNode.insert(newNode);
							leaves.get(depth+1).add(newNode);
							tBoard.resetCell(m);
						}
					}
				}
				/* Reset board for all parent states */
				parentNode = currentNode;
				while (parentNode != null) {
					if (parentNode.getData() != null) {
						if (parentNode.getData().getMove() != null) {
							tBoard.resetCell(parentNode.getData().getMove());
						}
					}
					parentNode = parentNode.getParent();
				}
			}
			// END OF TURN
			p = ((p == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);
		}
		// END OF TREE GEN
		
		decisionTree.setLeaves(leaves);
		
		return decisionTree;
	}
	
	/*public void buildTree() {
		Tree<GameState> decisionTree = new Tree<GameState>(new Board(mainBoard.getBoardSize()));
		Root<GameState> root = decisionTree.getRoot();
		
		HashMap<Integer, ArrayList<Node<GameState>>> leaves = new HashMap<Integer, ArrayList<Node<GameState>>>();
		 Generate empty leaves ArrayLists 
		for (int i = 0; i <= Math.pow(mainBoard.getBoardSize(), 2); i++) {
			ArrayList<Node<GameState>> l = new ArrayList<Node<GameState>>();
			leaves.put(i, l);
		}
		
		int p = Piece.WHITE;
		int depth = 0;
		
		while(!mainBoard.isFull()) {
			Board tBoard = new Board(mainBoard.getBoard(), mainBoard.getBoardSize());
			leaves.get(depth).add(root);
			
			int maxDepth = depth + (depth*MAX_PLY)/mainBoard.getBoardSpaces() + 2;
			maxDepth = (maxDepth > mainBoard.getBoardSpaces() ? mainBoard.getBoardSpaces() : maxDepth);
			for (; depth < maxDepth; depth++) {
				System.out.println("depth: "+depth);
				while (!leaves.get(depth).isEmpty()) {
					Node<GameState> currentNode = leaves.get(depth).get(0);
					leaves.get(depth).remove(0);
					
					 Set board for all parent states 
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
								if (DEBUG) System.out.println("d: "+depth+" - j: "+j+" - i: "+i);
								if (DEBUG) tBoard.printBoard();
								
								GameState newGS = new GameState(root.getData(), m);
								Node<GameState> newNode = new Node<GameState>(newGS, currentNode);
								newNode.getData().calculateScore(tBoard);
								currentNode.insert(newNode);
								leaves.get(depth+1).add(newNode);
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
				// END OF TURN
				p = ((p == Piece.WHITE) ? Piece.BLACK : Piece.WHITE);
			}
			// END OF TREE GEN
			Iterator<Node<GameState>> it = leaves.get(depth).iterator();
			Node<GameState> best = null;
			
			while (it.hasNext()) {
				Node<GameState> n = it.next();
				if (best == null || n.getData().getScore() > best.getData().getScore()) {
					best = n;
				}
				if (DEBUG) System.out.println("Evaluation function: "+n.getData().getScore());
			}
			// No children, hence board is full!
			if (best == null) {
				break;			
			}
			makeMoves(best);
			mainBoard.printBoard();
			
			root = new Root<GameState>(new Board(mainBoard.getBoard(), mainBoard.getBoardSize()));
			root.setChildren(best.getChildren());
			leaves.get(depth).clear();
		}
		
	}*/
	
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
		mainBoard.setCell(m);
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
