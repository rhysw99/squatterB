package aiproj.agent.board;


import aiproj.agent.Cell;
import aiproj.agent.Miscellaneous;
import aiproj.agent.PointPair;
import aiproj.agent.decisionTree.GameMove;
import aiproj.squatter.Piece;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
		return isLegal(move.getX(), move.getY(), move.getPlayer());
	}
	
	public boolean isLegal(int x, int y, int p) {
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

	public boolean checkCaptures(GameMove move, ScoreBoard sb) {
		/* Find the point adjacent from the move made which is closest
		 * to a corner.
		 */
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
						startingPaths.add(new Point(pathStartX, pathStartY));
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
		while(it.hasNext()) {
			Point p = it.next();
			 if (pathfind(p, move, sb)) {
				 //Captures made
				 boardModified = true;
			 }
		}
		return boardModified;
	}

	private boolean pathfind(Point startPath, GameMove move, ScoreBoard sb) {
		byte[][] explored = new byte[boardSize][boardSize];
		byte[][] scoreMap = sb.getBoard();

		boolean captures = false;

		//start pathfinding
		int i,j;

		int playerID = move.getPlayerID();

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

			GameMove newCap = new GameMove(move.getPlayer(), x, y, playerID);

			captureCell(newCap);
			captures = true;
			
			exploredList.remove(0);

			// was capture made by player? (not opponent)
			boolean playerCapture = (move.getPlayer() == move.getPlayerID());
			//Node.setCaptureDifference(playerCapture);
		}

		return captures;
	}

	private void captureCell(GameMove capMove){
		int y = (int)capMove.getY();
		int x = (int)capMove.getX();
		int player = capMove.getPlayer();

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
		} else if(player == Cell.WHITE) {
			switch(board[y][x]){
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



