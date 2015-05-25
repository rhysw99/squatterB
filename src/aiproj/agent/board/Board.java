package aiproj.agent.board;

import aiproj.agent.Ajmorton;
import aiproj.agent.PointPair;
import aiproj.agent.decisionTree.GameMove;
import aiproj.agent.decisionTree.GameState;
import aiproj.agent.decisionTree.Tree.Node;
import aiproj.squatter.Move;
import aiproj.squatter.Piece;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Board {	
	protected byte[][] board;			//the game board

	protected int boardSize;

	/* CONSTRUCTOR */
	public Board(int boardSize){
		this(new byte[boardSize][boardSize], boardSize);
	}

	public Board(byte[][] board, int boardSize) {
		this.board = board;
		this.boardSize = boardSize;
	}

	/* SETTER */
	public void setCell(GameMove move) {
		board[move.getLocation().y][move.getLocation().x] = (byte) move.getPlayer();
	}

	public void resetCell(GameMove move) {
		board[move.getLocation().y][move.getLocation().x] = Piece.EMPTY;
	}

	/* GETTER */
	public int getCell(Point point)	{
		return this.board[point.y][point.x];
	}

	public int getBoardSize() {
		return boardSize;
	}

	public int getBoardSpaces() {
		return boardSize*boardSize;
	}

	public byte[][] getBoard() {
		return board;
	}

	public boolean isLegal(GameMove move){
		if (!onBoard(move.getLocation())) {
			return false;
		} else if (isOccupied(move.getLocation())) {
			return false;
		}

		return true;
	}

	public void updateBoard(GameMove move) {		
		board[move.getLocation().y][move.getLocation().x] = move.getPlayer();
	}

	public boolean onBoard(Point p) {
		if (p.x >= 0 && p.y >= 0 && p.x < boardSize && p.y < boardSize) {
			return true;
		}
		return false;
	}

	public boolean isOccupied(Point p) {
		return (board[p.y][p.x] != Piece.EMPTY);
	}

	public boolean onEdge(Point p) {
		if (p.x == 0 || p.y == 0 || p.x == boardSize-1 || p.y == boardSize-1) {
			return true;						
		}
		return false;
	}
	
	public static Board copy(Board b) {
		int size = b.getBoardSize();
		byte[][] board = b.getBoard();
		byte[][] newBoard = new byte[size][size];
		
		for (int j = 0; j < size; j++) {
			for (int i = 0; i < size; i++) {
				newBoard[j][i] = board[j][i];
			}
		}
		
		return new Board(newBoard, size);
	}

	// TODO CHANGE THIS FUNCTION
	public boolean isFull() {
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				if (board[j][i] == Piece.EMPTY) {
					return false;
				}
			}
		}
		return true;
	}

	
	//TODO Move this somewhere
	private Point addPoints(Point a, Point b) {
		return new Point(a.x+b.x, a.y+b.y);
	}

	public void checkCaptures(GameMove move, ScoreBoard sb) {
		//System.out.println("Move: x: "+move.getLocation().x+ " - y: "+move.getLocation().y+" - p: "+move.getPlayer());
		/* Find the point adjacent from the move made which is closest to a corner.*/		
		int yOffset  = (move.getLocation().y <= boardSize/2) ? -1:1;
		int xOffset = (move.getLocation().x <= boardSize/2) ? -1:1;

		Point centerCell = new Point(move.getLocation().x, move.getLocation().y);
		Point offset = new Point(xOffset, yOffset);
		Point pathStart = addPoints(centerCell, offset);
		Point endCell = new Point(pathStart);

		ArrayList<Point> startingPaths = new ArrayList<Point>();
		
		while (!onBoard(pathStart) || board[pathStart.y][pathStart.x] != move.getPlayer()) {
			offset = nextCell(offset);
			pathStart = addPoints(offset, centerCell);
			// We have cycled around with with no possible points to start pathing from, therefore no captures.
			if (pathStart.equals(endCell)) {
				return;
			}
		}
		
		// We have found a piece surrounding the move that matches!
		endCell = new Point(pathStart);
		offset = nextCell(offset);
		pathStart = addPoints(offset, centerCell);

		boolean newPath = true;
		while (!pathStart.equals(endCell)) {
			if (onBoard(pathStart)) {
				if (board[pathStart.y][pathStart.x] != move.getPlayer()) {
					if (newPath) {
						startingPaths.add(new Point(pathStart));
						newPath = false;
					} else {
						newPath = true;
					}
				}
			}
			offset = nextCell(offset);
			pathStart = addPoints(offset, centerCell);
		}		
		
		// Loop through all our starting points and attempt to pathfind to edge from there
		Iterator<Point> it = startingPaths.iterator();
		while(it.hasNext()) {
			Point p = it.next();
			pathfind(p, move, sb);
		}
	}

	private void pathfind(Point startPath, GameMove move, ScoreBoard sb) {
		byte[][] explored = new byte[boardSize][boardSize];
		byte[][] scoreMap = sb.getBoard();

		//start pathfinding
		int i,j;

		// stores variable length lists of possible next cells
		HashMap<Integer, ArrayList<PointPair>> potentialMoves = new HashMap<Integer, ArrayList<PointPair>>(sb.getMaxScore() + 1);

		for (int a = 0; a <= sb.getMaxScore(); a++) {
			potentialMoves.put(a, new ArrayList<PointPair>());
		}

		// list of p cells that have been pathed to, if no path to edge is found all cells in list are captured
		ArrayList<Point> exploredList = new ArrayList<Point>();

		// make sure start point is on board
		if (!onBoard(startPath)) {
			return;
		}

		//check if already at edge
		if (onEdge(startPath)) {
			return;
		}
		
		//add cell to explored list, all necessary checks are done
		exploredList.add(startPath);
		explored[startPath.y][startPath.x] = 1;


		Point currCell = startPath;
		
		for(i = -1; i <= 1; i++) {
			for(j = -1; j <= 1; j++) {
				if((i != 0) || (j != 0)) {	// not current cell
					int newRow = (int)currCell.getY() + i;
					int newCol = (int)currCell.getX() + j;
					Point nextCell = new Point(newCol, newRow);
					if (onBoard(nextCell)) {
						int cellScore = sb.getValue(nextCell);

						PointPair newPointPair = new PointPair(nextCell, currCell);
						potentialMoves.get(cellScore).add(newPointPair);
						explored[nextCell.y][nextCell.x] = 1;

					}
				}
			}
		}

		// end initial cell check

		boolean exhausted = false,
				onlyDiag  = true;	// if only unreachable Diags are left than search is done

		while(!exhausted){
			exhausted = true;
			onlyDiag = true;
			outerloop:
				for(i = sb.getMaxScore(); i >= 0; i--){							// start looking at highest scoring possibles
					if(!potentialMoves.get(i).isEmpty()) {							// is possible point of score i available?
							
						exhausted = false;
						
						for(j = 0; j <potentialMoves.get(i).size(); j++){
							Point newP = potentialMoves.get(i).get(j).getNewCell();
						}
						
						for(j = 0; j <potentialMoves.get(i).size(); j++){
							
							// there are moves with score i to try
							PointPair nextTry = potentialMoves.get(i).get(j);

							Point newCell  = nextTry.getNewCell();
							Point prevCell = nextTry.getPrevCell();
							
							//check that cell can be moved to
							if(board[newCell.y][newCell.x] == move.getPlayer()) {
								onlyDiag = false;
								potentialMoves.get(i).remove(j);
								break outerloop; //restart search through list
							}

							// if diagonal check for adjacents
							if((Math.abs(newCell.x - prevCell.x) + Math.abs(newCell.y - prevCell.y)) == 2) { // diagonal movement
								int ownerCellX, ownerCellY;

								ownerCellX = board[prevCell.y + (newCell.y - prevCell.y)][prevCell.x]; //mutually adjacent hori cell
								ownerCellY = board[prevCell.y][prevCell.x + (newCell.x - prevCell.x)]; //mutually adjacent vert cell

								// Cannot path to cell if both mutual adjacent cells are player owned
								if((ownerCellX == move.getPlayer()) && (ownerCellY == move.getPlayer())) {
									explored[newCell.y][newCell.x] = 0;
									potentialMoves.get(i).remove(j);
									onlyDiag = false;
									break outerloop;
								}
							}
							onlyDiag = false;
							
							// else can use cell to check
							exploredList.add(newCell);
							
							// Are we at one of the edge nodes?
							if(scoreMap[newCell.y][newCell.x] == sb.getMaxScore()){
								return;	// no new cells in capture list
							}

							//else add surrounding cells to potentialMoves

							currCell = newCell;

							for (int k = -1; k <= 1; k++) {
								for (int l = -1; l <= 1; l++) {
									if (k != 0 && l != 0) {	// not current cell
										int newRow = (int)currCell.getY() + k;
										int newCol = (int)currCell.getX() + l;
										newCell = new Point(newCol, newRow);
										if(onBoard(newCell) && (explored[newRow][newCol] == 0)) { // on board, not already in potMoves

											int cellScore = scoreMap[newRow][newCol];

											PointPair newPointPair = new PointPair(newCell, currCell);
											potentialMoves.get(cellScore).add(newPointPair);

											explored[newCell.y][newCell.x] = 1;

										}
									}
								}
							}
							potentialMoves.get(i).remove(j);
							break outerloop; // go back to start of potMoves
						}
					}
				}
			if(onlyDiag) {
				exhausted = true;
			}
		}

		
		// cells can be added to the captures
		while(!exploredList.isEmpty()) {
			int x = exploredList.get(0).x;
			int y = exploredList.get(0).y;
			//TODO Change piece structure
			GameMove newCap = new GameMove(move.getPlayer(), new Point(x,y));

			captureCell(newCap);
			exploredList.remove(0);
		}

		return;
	}

	private static Point nextCell(Point offset) {
		// TODO	
		// STORE HASHMAP PERMANENTLY
		HashMap<Point, Point> next = new HashMap<Point, Point>(8);
		next.put(new Point(-1, -1), new Point(0, -1));
		next.put(new Point(0, -1), new Point(1, -1));
		next.put(new Point(1, -1), new Point(1, 0));
		next.put(new Point(1, 0), new Point(1, 1));
		next.put(new Point(1, 1), new Point(0, 1));
		next.put(new Point(0, 1), new Point(-1, 1));
		next.put(new Point(-1, 1), new Point(-1, 0));
		next.put(new Point(-1, 0), new Point(-1, -1));
		
		offset = next.get(offset);

		return offset;
	}

	private void captureCell(GameMove capMove){
		// TODO cell values (1==BLACK etc are still not set, suggest following modified Peice interface)
		
		int y = (int)capMove.getLocation().getY();
		int x = (int)capMove.getLocation().getX();
		int player = capMove.getPlayer();

		if(player == Piece.BLACK){
			switch (board[y][x]){
			case Piece.WHITE:
				board[y][x] = Piece.WHITE_CAP;
				break;

			case Piece.EMPTY:
				board[y][x] = Piece.CAP;
				break;

			default:
				break;
			}
		}else if(player == Piece.WHITE){
			switch(board[y][x]){
			case Piece.BLACK:
				board[y][x] = Piece.BLACK_CAP;
				break;
			case Piece.EMPTY:
				board[y][x] = Piece.CAP;
				break;
			default:
				break;
			}
		}
		return;				
	}
	
	public static boolean checkUniqueStates(Board a, Board b, GameMove m) {
		byte[][] aBoard = a.getBoard();
		byte[][] bBoard = b.getBoard();
		int x = m.getLocation().x;
		int y = m.getLocation().y;
		if (aBoard[y][x] == Piece.BLACK && bBoard[y][x] == Piece.WHITE ||
				aBoard[y][x] == Piece.WHITE && bBoard[y][x] == Piece.BLACK) {
			//System.out.println("CONFLICTTT for move: x: "+m.getLocation().x + " - y: "+m.getLocation().y + " - p: "+m.getPlayer());
			return false;			
		}
		return true;
	}
	
	public boolean equals(Board other) {
		byte[][] o = other.getBoard();
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				if (board[j][i] != o[j][i]) {
					return false;
				}
			}
		}
		return true;
	}
		
	/*
	 * Transform comparison should be in buildTree
	 * Takes two boards, compares most recent move of the first board to check for conflicts
	 */
	public Board transform(int i) {
		switch (i) {
			case 0: //transform90CW
				return transform90CW();
			case 1:
				return transform90CCW();
			case 2:
				return transform180();
			case 3:
				return transformFlipVertical();
			case 4:
				return transformFlipHorizontal();
			case 5:
				return transformFlipMajorDiagonal();
			case 6:
				return transformFlipMinorDiagonal();
			default:
				return null;
		}		
	}

	private Board transform90CW() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				GameMove m = new GameMove(board[j][i], new Point(boardSize-j-1, i));
				tBoard.setCell(m);
			}
		}
		return tBoard;
	}

	private Board transform90CCW() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				GameMove m = new GameMove(board[j][i], new Point(j, boardSize-i-1));
				tBoard.setCell(m);
			}
		}
		return tBoard;
	}

	private Board transform180() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				GameMove m = new GameMove(board[j][i], new Point(boardSize-i-1, boardSize-j-1));
				tBoard.setCell(m);
			}
		}
		return tBoard;
	}

	private Board transformFlipVertical() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				GameMove m = new GameMove(board[j][i], new Point(boardSize-i-1, j));
				tBoard.setCell(m);
			}
		}
		return tBoard;
	}

	private Board transformFlipHorizontal() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				GameMove m = new GameMove(board[j][i], new Point(i, boardSize-j-1));
				tBoard.setCell(m);
			}
		}
		return tBoard;
	}

	private Board transformFlipMajorDiagonal() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				GameMove m = new GameMove(board[j][i], new Point(j,i));
				tBoard.setCell(m);
			}
		}
		return tBoard;
	}

	private Board transformFlipMinorDiagonal() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				GameMove m = new GameMove(board[j][i], new Point(boardSize-j-1, boardSize-i-1));
				tBoard.setCell(m);
			}
		}
		return tBoard;
	}


	/* TEST FUNCTION */

	public void printBoard() {
		System.out.println("printing scoreboard");
		System.out.println("board size: "+boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				System.out.print(board[j][i] + " ");
			}
			System.out.println();
		}
	}

}



