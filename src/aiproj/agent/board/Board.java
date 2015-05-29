package aiproj.agent.board;


import aiproj.agent.Cell;
import aiproj.agent.Miscellaneous;
import aiproj.agent.PointPair;
import aiproj.agent.decisionTree.GameMove;
import aiproj.agent.decisionTree.GameState;
import aiproj.squatter.Piece;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

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
		board[move.getY()][move.getX()] = move.getPlayer();
	}
	
	public void setCell(int x, int y, byte v) {
		board[y][x] = v;
	}

	public void resetCell(GameMove move) {
		board[move.getY()][move.getX()] = Cell.EMPTY;
	}
	
	public void resetCell(int i, int j, int p) {
		board[j][i] = Piece.EMPTY;
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
	
	public int getFreeSpaces() {
		int count = 0;
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				if (board[j][i] == Cell.EMPTY) {
					count++;
				}
			}
		}
		return count;
	}

	public boolean isLegal(GameMove move){
		return isLegal(move.getX(), move.getY());
	}
	
	public boolean isLegal(int x, int y) {
		if (!onBoard(x, y)) {
			return false;
		} else if (isOccupied(x, y)) {
			return false;
		}

		return true;
	}
	
	public byte getValueAtPosition(int x, int y) {
		return board[y][x];
	}

	public boolean onBoard(Point p) {
		if (p.x >= 0 && p.y >= 0 && p.x < boardSize && p.y < boardSize) {
			return true;
		}
		return false;
	}
	
	public boolean onBoard(int x, int y) {
		if (x >= 0 && y >= 0 && x < boardSize && y < boardSize) {
			return true;
		}
		return false;
	}

	public boolean isOccupied(Point p) {
		return (board[p.y][p.x] != Cell.EMPTY);
	}
	
	public boolean isOccupied(int x, int y) {
		return (board[y][x] != Cell.EMPTY);
	}

	public boolean onEdge(Point p) {
		return onEdge(p.x, p.y);
	}
	
	public boolean onEdge(int x, int y) {
		if (x == 0 || y == 0 || x == boardSize-1 || y == boardSize-1) {
			return true;						
		}
		return false;
	}
	
	public static Board copy(Board b) {
		int size = b.getBoardSize();
		byte[][] board = b.getBoard();
		byte[][] newBoard = new byte[size][];
		
		for (int j = 0; j < size; j++) {
			newBoard[j] = board[j].clone();
		}
		
		return new Board(newBoard, size);
	}
	
	public boolean isFull() {
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				if (board[j][i] == Cell.EMPTY) {
					return false;
				}
			}
		}
		return true;
	}
	
	public long hashKey() {
		long key = 0;
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				if (board[j][i] != Cell.EMPTY) {
					key ^= Miscellaneous.zobrist[j*boardSize+i][board[j][i]];
				}
			}
		}
		return key;
	}
	
	public long updateKey(long oldKey, int i, int j, int p) {
		return oldKey ^ Miscellaneous.zobrist[j*boardSize + i][p];
	}

	public boolean checkCaptures(GameState gs, ScoreBoard sb, int currentPlayer) {
		/* Find the point adjacent from the move made which is closest
		 * to a corner.
		 */
		GameMove move = gs.getMove();
		
		int yOffset  = (move.getY() <= boardSize/2) ? -1:1;
		int xOffset = (move.getX() <= boardSize/2) ? -1:1;
		
		boolean boardModified = false;

		Point centerCell = new Point(move.getX(), move.getY());
		Point offset = new Point(xOffset, yOffset);		
		
		int pathStartX = move.getX() + offset.x;
		int pathStartY = move.getY() + offset.y;

		int centerCellX = centerCell.x;
		int	centerCellY = centerCell.y;
		
		Point endCell = new Point(pathStartX, pathStartY);
		
		ArrayList<Point> startingPaths = new ArrayList<Point>();
		// find first pathStart
		while (!onBoard(pathStartX, pathStartY) ||
				board[pathStartY][pathStartX] != move.getPlayer()) {
			offset = Miscellaneous.nextCell(offset);
			pathStartX = centerCellX + offset.x;
			pathStartY = centerCellY + offset.y;
			// We have cycled around with with no possible points to start
			// pathing from, therefore no captures.
			if (pathStartX == endCell.x && pathStartY == endCell.y) {
				return boardModified;
			}
		}
		
		// We have found a piece surrounding the move that matches!
		endCell.setLocation(pathStartX, pathStartY);
		offset = Miscellaneous.nextCell(offset);
		pathStartX = centerCellX + offset.x;
		pathStartY = centerCellY + offset.y;

		boolean newPath = true;
		while (!(pathStartX == endCell.x && pathStartY == endCell.y)) {
			if (onBoard(pathStartX, pathStartY)) {
				if (board[pathStartY][pathStartX] != move.getPlayer()) {
					if (newPath) {
						Point p = new Point(pathStartX, pathStartY);
						if (!startingPaths.contains(p))
							startingPaths.add(p);
						else
							System.out.println("dup");
						newPath = false;
					} else {
						newPath = true;
					}
				}
			}
			offset = Miscellaneous.nextCell(offset);
			pathStartX = centerCellX + offset.x;
			pathStartY = centerCellY + offset.y;
		}
		
		// Loop through all our starting points and attempt to pathfind to
		// edge from there
		Iterator<Point> it = startingPaths.iterator();
		//System.out.println("Move: x: "+move.getX() + " - y: "+move.getY()+ " - p: "+move.getPlayer());
		//System.out.println("starting nodes: "+startingPaths.size());
		boolean[][] explored = new boolean[boardSize][boardSize];
		while(it.hasNext()) {
			Point p = it.next();
			//System.out.println("Start: x: "+p.x+" - y: "+p.y);
			/*if (pathfind(p, gs, sb, currentPlayer)) {
				boardModified = true;
			}*/
			
			ArrayList<Point> capturedCells = new ArrayList<Point>();
			 if (!pathToEdge(p, gs, sb, explored, capturedCells, currentPlayer)) {
				 //Captures made
				 //System.out.println("Capture: x: "+p.x+" - y: "+p.y);
				 //printBoard();
				 Iterator<Point> i = capturedCells.iterator();
				 while (i.hasNext()) {
					 Point a = i.next();
					 captureCell(move, a);
					 gs.incrementCapture(currentPlayer);
					// System.out.println("\tExplored: x: "+a.x+" - y: "+a.y);
				 }
				 boardModified = true;
			 }
		}
		return boardModified;
	}
	
	private boolean pathToEdge(Point startPath, GameState gs, ScoreBoard scoreBoard, boolean[][] explored, ArrayList<Point> capturedCells, int currentPlayer) {
		int x = startPath.x;
		int y = startPath.y;
		GameMove m = gs.getMove();
		
		if (onEdge(x, y)) {
			return true;
		}
		
		explored[startPath.y][startPath.x] = true;
		capturedCells.add(startPath);
		
		byte[][] scoreMap = scoreBoard.getBoard();
		
		HashMap<Byte, ArrayList<Point>> next = new HashMap<Byte, ArrayList<Point>>();
		for (byte i = 0; i <= scoreBoard.getMaxScore(); i++) {
			next.put(i, new ArrayList<Point>());
		}
		
		boolean foundPath = false;
		
		if (onBoard(x, y+1) && board[y+1][x] != m.getPlayer()) {
			if (!explored[y+1][x])
				next.get(scoreMap[y+1][x]).add(new Point(x, y+1));
			else
				foundPath = true;
		}
		if (onBoard(x, y-1) && board[y-1][x] != m.getPlayer()) {
			if (!explored[y-1][x])
				next.get(scoreMap[y-1][x]).add(new Point(x, y-1));
			else
				foundPath = true;
		}
		if (onBoard(x+1, y) && board[y][x+1] != m.getPlayer()) {
			if (!explored[y][x+1])
				next.get(scoreMap[y][x+1]).add(new Point(x+1, y));
			else
				foundPath = true;
		}
		if (onBoard(x-1, y) && board[y][x-1] != m.getPlayer()) {
			if (!explored[y][x-1])
				next.get(scoreMap[y][x-1]).add(new Point(x-1, y));
			else
				foundPath = true;
		}
		
		for (byte i = (byte) scoreBoard.getMaxScore(); i >= 0; i--) {
			if (!next.get(i).isEmpty()) {
				Point p = next.get(i).remove(0);
				if (pathToEdge(p, gs, scoreBoard, explored, capturedCells, currentPlayer)) {
					return true;
				}
			}
		}
		
		return foundPath;
	}

	private boolean pathfind(Point startPath, GameState gs, ScoreBoard sb, int currentPlayer) {
		byte[][] explored = new byte[boardSize][boardSize];
		byte[][] scoreMap = sb.getBoard();

		boolean captures = false;

		//start pathfinding
		int i,j;
		
		GameMove move = gs.getMove();
		
		// stores variable length lists of possible next cells
		HashMap<Integer, ArrayList<PointPair>> potentialMoves = 
				new HashMap<Integer, ArrayList<PointPair>>(sb.getMaxScore() + 1);

		for (int a = 0; a <= sb.getMaxScore(); a++) {
			potentialMoves.put(a, new ArrayList<PointPair>());
		}

		// list of p cells that have been pathed to, if no path to edge is
		// found all cells in list are captured
		ArrayList<Point> exploredList = new ArrayList<Point>();

		// make sure start point is on board
		if (!onBoard(startPath)) {
			return false;
		}

		//check if already at edge
		if (onEdge(startPath)) {
			return false;
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
					if (onBoard(nextCell) &&
							(board[nextCell.y][nextCell.x]) != move.getPlayer()) {

						int cellScore = sb.getValue(nextCell);

						PointPair newPointPair = new PointPair(nextCell,
								currCell);
						potentialMoves.get(cellScore).add(newPointPair);
						explored[nextCell.y][nextCell.x] = 1;

					}
				}
			}
		}

		// end initial cell check

		boolean exhausted = false;

		while(!exhausted) {
			exhausted = true;
			outerloop:
				// start looking at highest scoring possibles
				for(i = sb.getMaxScore(); i >= 0; i--) {
					// is possible point of score i available?
					if(!potentialMoves.get(i).isEmpty()) {
						exhausted = false;

						int listLength = potentialMoves.get(i).size();
						for(j = 0; j < listLength; j++){

							// there are moves with score i to try
							PointPair nextTry = potentialMoves.get(i).get(j);

							Point newCell  = nextTry.getNewCell();
							Point prevCell = nextTry.getPrevCell();

							//check that cell can be moved to
							if(board[newCell.y][newCell.x] ==
									move.getPlayer()) {
								potentialMoves.get(i).remove(j);
								j -= 1;
								listLength -= 1;
								continue;
							}

							// if diagonal check for adjacents
							if((Math.abs(newCell.x - prevCell.x) +
									Math.abs(newCell.y - prevCell.y)) == 2) {
								int ownerCellX, ownerCellY;

								ownerCellX = board[newCell.y][prevCell.x];
								ownerCellY = board[prevCell.y][newCell.x];

								// Cannot path to cell if both mutual adjacent
								// cells are player owned
								if((ownerCellX == move.getPlayer()) && 
										(ownerCellY == move.getPlayer())) {
									explored[newCell.y][newCell.x] = 0;
									potentialMoves.get(i).remove(j);
									j -= 1;
									listLength -= 1;
									continue;
								}
							}

							// else can use cell to check
							exploredList.add(newCell);

							// Are we at one of the edge nodes?
							if(onEdge(newCell.x, newCell.y)){
								return false; // no new cells in capture list
							}

							//else add surrounding cells to potentialMoves

							currCell = newCell;
							exhausted = false;
							for (int k = -1; k <= 1; k++) {
								for (int l = -1; l <= 1; l++) {
									if ((k != 0) || (l != 0)) {	// not current
										int newRow = (int)currCell.getY() + k;
										int newCol = (int)currCell.getX() + l;
										newCell = new Point(newCol, newRow);
										if(onBoard(newCell) &&
												(explored[newRow][newCol] == 0
												&& (board[newRow][newCol] !=
													move.getPlayer()))) {

											int cellScore =
													scoreMap[newRow][newCol];

											PointPair newPointPair =
													new PointPair(newCell,
															currCell);
											potentialMoves.get(cellScore).
												add(newPointPair);

											explored[newCell.y][newCell.x]=1;

										}
									}
								}
							}
							potentialMoves.get(i).remove(j);
							break outerloop; // go back to start of potMoves
						}
					}
				}
		}
		
		// cells can be added to the captures
		while(!exploredList.isEmpty()) {
			int x = exploredList.get(0).x;
			int y = exploredList.get(0).y;

			GameMove newCap = new GameMove(move.getPlayer(), x, y);

			//captureCell(newCap);
			captures = true;
			
			exploredList.remove(0);
			
			gs.incrementCapture(currentPlayer);

			// was capture made by player? (not opponent)
			boolean playerCapture = (move.getPlayer() == currentPlayer);
			//Node.setCaptureDifference(playerCapture);
		}

		return captures;
	}

	private void captureCell(GameMove move, Point cell){
		int y = cell.y;
		int x = cell.x;
		int player = move.getPlayer();

		if(player == Cell.BLACK) {
			switch (board[y][x]){
			case Cell.WHITE:
				board[y][x] = Cell.WHITE_CAP;
				break;

			case Cell.EMPTY:
				board[y][x] = Cell.CAP;
				break;

			default:
				break;
			}
		} else if (player == Cell.WHITE){
			switch (board[y][x]) {
				case Cell.BLACK:
					board[y][x] = Cell.BLACK_CAP;
					break;
				case Cell.EMPTY:
					board[y][x] = Cell.CAP;
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
		int x = m.getX();
		int y = m.getY();
		if (aBoard[y][x] == Cell.BLACK && bBoard[y][x] == Cell.WHITE ||
				aBoard[y][x] == Cell.WHITE && bBoard[y][x] == Cell.BLACK) {
			return false;			
		}
		return true;
	}

	public boolean equals(Board other) {
		byte[][] ob = other.getBoard();
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				if (board[j][i] != ob[j][i]) {
					return false;
				}
			}
		}
		return true;
	}

	/*
	 * Transform comparison should be in buildTree
	 * Takes two boards, compares most recent move of the first board
	 * to check for conflicts
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
				tBoard.setCell(boardSize-j-1, i, board[j][i]);
			}
		}
		return tBoard;
	}

	private Board transform90CCW() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(j, boardSize-i-1, board[j][i]);
			}
		}
		return tBoard;
	}

	private Board transform180() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(boardSize-i-1, boardSize-j-1, board[j][i]);
			}
		}
		return tBoard;
	}

	private Board transformFlipVertical() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(boardSize-i-1, j, board[j][i]);
			}
		}
		return tBoard;
	}

	private Board transformFlipHorizontal() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(i, boardSize-j-1, board[j][i]);
			}
		}
		return tBoard;
	}

	private Board transformFlipMajorDiagonal() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(j, i, board[j][i]);
			}
		}
		return tBoard;
	}

	private Board transformFlipMinorDiagonal() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(j, i, board[j][i]);
			}
		}
		return tBoard;
	}


	/* TEST FUNCTION */

	public void printBoard() {
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				System.out.print(board[j][i] + " ");
			}
			System.out.println();
		}
	}

}



