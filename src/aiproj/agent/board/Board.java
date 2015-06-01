package aiproj.agent.board;


import aiproj.agent.Cell;
import aiproj.agent.Misc;
import aiproj.agent.decisionTree.GameMove;
import aiproj.agent.decisionTree.GameState;
import aiproj.squatter.Piece;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** 
 * The board object that stores the current gamestate
 */
public class Board {	
	protected byte[][] board;			//the game board

	protected int boardSize;
	
	private byte[] captures = new byte[3];
	/* CONSTRUCTORS */
	public Board(int boardSize){
		this(new byte[boardSize][boardSize], boardSize);
	}

	public Board(byte[][] board, int boardSize) {
		this.board = board;
		this.boardSize = boardSize;
	}

	/* SETTERS */
	public void setCell(GameMove move) 		  {board[move.getY()][move.getX()] = move.getPlayer();}
	public void setCell(int x, int y, byte v) {board[y][x] = v;}
	public void resetCell(GameMove move) 	  {board[move.getY()][move.getX()] = Cell.EMPTY;}
	public void resetCell(int i, int j, int p){board[j][i] = Piece.EMPTY;}

	/* GETTERS */
	public int getCell(Point point)	{return this.board[point.y][point.x];}
	public int getBoardSize() 		{return boardSize;}
	public int getBoardSpaces() 	{return boardSize*boardSize;}
	public byte[][] getBoard() 		{return board;}
	
	
	/* METHODS */
	// return the number of empty cells on the board
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

	// check if a move is legal on the board
	public boolean isLegal(GameMove move){
		return isLegal(move.getX(), move.getY());
	}
	
	// check if a move can be made at a specific cell on the board
	public boolean isLegal(int x, int y) {
		if (!onBoard(x, y)) {
			return false;
		} else if (isOccupied(x, y)) {
			return false;
		}
		return true;
	}
	
	// return the id of a cell at location board[y][x]
	public byte getValueAtPosition(int x, int y) {
		if (onBoard(x, y)) {
			return board[y][x];
		} else {
			return -1;
		}
	}

	// check that the point co-ordinates are on the gameboard
	public boolean onBoard(Point p) {
		if (p.x >= 0 && p.y >= 0 && p.x < boardSize && p.y < boardSize) {
			return true;
		}
		return false;
	}
	
	// check if the integer coordinates are on the gameboard
	public boolean onBoard(int x, int y) {
		if (x >= 0 && y >= 0 && x < boardSize && y < boardSize) {
			return true;
		}
		return false;
	}

	// check that a cell at point p is occupied or not
	public boolean isOccupied(Point p) {
		return (board[p.y][p.x] != Cell.EMPTY);
	}
	
	// check that a cell at locaiton[y][x] is occupied or not
	public boolean isOccupied(int x, int y) {
		return (board[y][x] != Cell.EMPTY);
	}

	// check if a cell at point p is on the edge of the baord or not
	// used for pathfinding
	public boolean onEdge(Point p) {
		return onEdge(p.x, p.y);
	}
	
	// check is a cell at board[y][x] is on the edge of the board or not
	// used for pathfinding
	public boolean onEdge(int x, int y) {
		if (x == 0 || y == 0 || x == boardSize-1 || y == boardSize-1) {
			return true;						
		}
		return false;
	}
	
	// copies the current board
	public static Board copy(Board b) {
		int size = b.getBoardSize();
		byte[][] board = b.getBoard();
		byte[][] newBoard = new byte[size][];
		
		for (int j = 0; j < size; j++) {
			newBoard[j] = board[j].clone();
		}
		
		return new Board(newBoard, size);
	}
	
	// check if the gameboard is full or not
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
	
	// return a zobrist hash of the board for mirror/rotation detection of ismialr states
	public long hashKey() {
		long key = 0;
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				if (board[j][i] != Cell.EMPTY) {
					key ^= Misc.zobrist[j*boardSize+i][board[j][i]];
				}
			}
		}
		return key;
	}
	
	// upadate a zobrist hash jey
	public long updateKey(long oldKey, int i, int j, int p) {
		return oldKey ^ Misc.zobrist[j*boardSize + i][p];
	}

	// check if the most recent move has captured any cells on the board
	public boolean checkCaptures(GameState gs, ScoreBoard sb, int currentPlayer, boolean actualMove) {
		/* Find the point adjacent from the move made which is closest
		 * to a corner of the board.
		 */
		GameMove move = gs.getMove();
		
		int yOffset  = (move.getY() <= boardSize/2) ? -1:1;
		int xOffset = (move.getX() <= boardSize/2) ? -1:1;
		
		// a flag for if captures are made
		boolean boardModified = false;

		Point centerCell = new Point(move.getX(), move.getY());
		Point offset = new Point(xOffset, yOffset);		
		
		int pathStartX = move.getX() + offset.x;
		int pathStartY = move.getY() + offset.y;

		int centerCellX = centerCell.x;
		int	centerCellY = centerCell.y;
		
		Point endCell = new Point(pathStartX, pathStartY);
		
		ArrayList<Point> startingPaths = new ArrayList<Point>();
		// move clockwise around the new cell to check if capture detection needs to take place from there
		while (!onBoard(pathStartX, pathStartY) ||
				board[pathStartY][pathStartX] != move.getPlayer()) {
			offset = Misc.nextCell(offset);
			pathStartX = centerCellX + offset.x;
			pathStartY = centerCellY + offset.y;
			// We have cycled around with with no possible points to start
			// pathing from, therefore no captures.
			if (pathStartX == endCell.x && pathStartY == endCell.y) {
				return boardModified;
			}
		}
		
		// start pathfinding from this cell
		endCell.setLocation(pathStartX, pathStartY);
		offset = Misc.nextCell(offset);
		pathStartX = centerCellX + offset.x;
		pathStartY = centerCellY + offset.y;

		boolean newPath = true;
		while (!(pathStartX == endCell.x && pathStartY == endCell.y)) {
			if (onBoard(pathStartX, pathStartY)) {
				if (board[pathStartY][pathStartX] != move.getPlayer() &&
						board[pathStartY][pathStartX] < Cell.BLACK_CAP) {
					if (newPath) {
						Point p = new Point(pathStartX, pathStartY);
						startingPaths.add(p);
						newPath = false;
					}
				} else {
					newPath = true;
				}
			}
			offset = Misc.nextCell(offset);
			pathStartX = centerCellX + offset.x;
			pathStartY = centerCellY + offset.y;
		}
		
		// Loop through all our starting points and attempt to pathfind to
		// edge from there
		Iterator<Point> it = startingPaths.iterator();
		
		while(it.hasNext()) {
			Point p = it.next();
			
			boolean[][] explored = new boolean[boardSize][boardSize];
			ArrayList<Point> capturedCells = new ArrayList<Point>();
			 if (!pathToEdge(p, gs, sb, explored, capturedCells, currentPlayer)) {
				 //Captures made
				 //printBoard();
				 Iterator<Point> i = capturedCells.iterator();
				 while (i.hasNext()) {
					 Point a = i.next();
					 captureCell(move, a);
 					 gs.incrementCapture(currentPlayer);
 					 if (actualMove) {
 						 captures[currentPlayer]++;
 					 }
				 }
				 boardModified = true;
			 }
		}
		return boardModified;
	}
	
	// find a path to the edge of the board from the current cell
	private boolean pathToEdge(Point startPath, GameState gs, ScoreBoard scoreBoard, boolean[][] explored, ArrayList<Point> capturedCells, int currentPlayer) {
		int x = startPath.x;
		int y = startPath.y;
		GameMove m = gs.getMove();
		
		if (x == 3 && y == 3) {
			//System.out.println("MIDDLE");
		} else {
			//System.out.println("Exploring: p.x: "+startPath.x+" - p.y: "+startPath.y);
		}
		
		if (onEdge(x, y)) {
			//System.out.println("=========");
			return true;
		}
		
		byte[][] scoreMap = scoreBoard.getBoard();
		
		HashMap<Byte, ArrayList<Point>> next = new HashMap<Byte, ArrayList<Point>>();
		for (byte i = 0; i <= scoreBoard.getMaxScore(); i++) {
			next.put(i, new ArrayList<Point>());
		}
		
		if (onBoard(x, y+1) && board[y+1][x] != m.getPlayer()) {
			if (!explored[y+1][x])
				next.get(scoreMap[y+1][x]).add(new Point(x, y+1));
		}
		if (onBoard(x, y-1) && board[y-1][x] != m.getPlayer()) {
			if (!explored[y-1][x])
				next.get(scoreMap[y-1][x]).add(new Point(x, y-1));
		}
		if (onBoard(x+1, y) && board[y][x+1] != m.getPlayer()) {
			if (!explored[y][x+1])
				next.get(scoreMap[y][x+1]).add(new Point(x+1, y));
		}
		if (onBoard(x-1, y) && board[y][x-1] != m.getPlayer()) {
			if (!explored[y][x-1])
				next.get(scoreMap[y][x-1]).add(new Point(x-1, y));
		}
		
		explored[startPath.y][startPath.x] = true;
		capturedCells.add(startPath);
		
		for (byte i = (byte) scoreBoard.getMaxScore(); i >= 0; i--) {
			if (!next.get(i).isEmpty()) {
				Point p = next.get(i).remove(0);
				if (pathToEdge(p, gs, scoreBoard, explored, capturedCells, currentPlayer)) {
					return true;
				}
			}
		}

		return false;
	}
	
	// changes a cell to its captured form if valid
	// + to -
	// B to b
	// W to w
	private void captureCell(GameMove move, Point cell) {
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

	// checks if two boards are exactly alike
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

	// TODO is this still used?
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
	
	// transforms the board for simialr state detection
	private Board transform90CW() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(boardSize-j-1, i, board[j][i]);
			}
		}
		return tBoard;
	}

	// transforms the board for simialr state detection
	private Board transform90CCW() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(j, boardSize-i-1, board[j][i]);
			}
		}
		return tBoard;
	}

	// transforms the board for simialr state detection
	private Board transform180() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(boardSize-i-1, boardSize-j-1, board[j][i]);
			}
		}
		return tBoard;
	}

	// transforms the board for similar state detection
	private Board transformFlipVertical() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(boardSize-i-1, j, board[j][i]);
			}
		}
		return tBoard;
	}

	// transforms the board for similar state detection
	private Board transformFlipHorizontal() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(i, boardSize-j-1, board[j][i]);
			}
		}
		return tBoard;
	}

	// transforms the board for similar state detection
	private Board transformFlipMajorDiagonal() {
		Board tBoard = new Board(boardSize);
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				tBoard.setCell(j, i, board[j][i]);
			}
		}
		return tBoard;
	}

	// transforms the board for similar state detection
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

	public byte[] getCaptures() {
		return captures;
	}

}



