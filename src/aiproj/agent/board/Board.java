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

	public void updateBoard(GameMove move, ScoreBoard sb) {		
		board[move.getLocation().y][move.getLocation().x] = move.getPlayer();

		checkCaptures(move, sb);
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
		if (p.x == 0 && p.y == 0 && p.x == boardSize-1 && p.y == boardSize-1) {
			return true;						
		}
		return false;
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


	private void checkCaptures(GameMove move, ScoreBoard sb) {
		/* Find the point adjacent from the move made which is closest to a corner.*/		
		int yOffset  = (move.getLocation().y <= boardSize/2) ? -1:1;
		int xOffset = (move.getLocation().x <= boardSize/2) ? -1:1;

		Point startCell = new Point(move.getLocation().x + xOffset, move.getLocation().y + yOffset);

		Point pathStart = new Point(startCell);
		Point endCell = new Point(startCell);

		// can path from startCell, else path from next available cell
		// if no paths are possible, end
		if(board[startCell.y][startCell.x] != move.getPlayer()){
			pathfind(startCell, move, sb);
		} else {
			pathStart = nextCell(pathStart, move.getLocation(), true);
			while(board[pathStart.y][pathStart.x] == move.getPlayer()){
				pathStart = nextCell(pathStart, move.getLocation(), true);
				if(pathStart == endCell) {
					return;
				}
			}
			pathfind(pathStart, move, sb);
		}

		// move pointer clockwise until blocked
		while(board[pathStart.y][pathStart.x] != move.getPlayer()){
			pathStart = nextCell(pathStart, move.getLocation(), true);		
		}

		//move pointer counterclockwise until blocked
		while(board[endCell.y][endCell.x] != move.getPlayer()){
			endCell = nextCell(endCell, move.getLocation(), false);		
		}

		// no more cells to check
		boolean newPath = true;

		while(pathStart != endCell) {
			if(newPath && (board[pathStart.y][pathStart.x] != move.getPlayer())) {
				pathfind(pathStart, move, sb);
				newPath = false;
			} else if(!newPath && (board[pathStart.y][pathStart.x] == move.getPlayer())) {
				newPath = true;
			}
		}

		return;

		// TODO
		// consider Points not in board range

		// look over start point detections
		// in particular if startCell is necessary, can't be replace by endCell
	}

	private void pathfind(Point startPath, GameMove move, ScoreBoard sb) {
		byte[][] explored = new byte[boardSize][boardSize];
		byte[][] scoreMap = sb.getBoard();

		//start pathfinding
		int i,j;

		// stores variable length lists of possible next cells
		HashMap<Integer, ArrayList<PointPair>> potentialMoves = new HashMap<Integer, ArrayList<PointPair>>(sb.getMaxScore() + 1);

		for (int a = 0; a < sb.getMaxScore(); a++) {
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
				if(i != 0 && j != 0) {	// not current cell
					int newRow = (int) currCell.getY() + i;
					int newCol = (int) currCell.getX() + j;

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



		boolean exhausted = false;

		while(!exhausted){
			outerloop:
				for(i = sb.getMaxScore(); i >= 0; i--){							// start looking at highest scoring possibles
					if(!potentialMoves.get(i).isEmpty()) {							// is possible point of score i available?
						for(j = 0; j <potentialMoves.get(i).size(); j++){

							// there are moves with score i to try
							PointPair nextTry = potentialMoves.get(i).get(0);

							Point newCell  = nextTry.getNewCell();
							Point prevCell = nextTry.getPrevCell();

							//check that cell can be moved to
							if(board[newCell.y][newCell.x] == move.getPlayer()) {
								potentialMoves.get(i).remove(0);
								break outerloop; //restart search through list
							}

							// if diagonal check for adjacents
							if((Math.abs(newCell.x - prevCell.x) + Math.abs(newCell.y - prevCell.y)) == 2) { // diagonal movement

								int ownerCellX, ownerCellY;

								ownerCellX = board[prevCell.y + (newCell.y - prevCell.y)][prevCell.x]; //mutually adjacent hori cell
								ownerCellY = board[prevCell.y][prevCell.x + (newCell.x - prevCell.x)]; //mutually adjacent vert cell

								// Cannot path to cell if both mutual adjacent cells are player owned
								if((ownerCellX == move.getPlayer()) && (ownerCellY == move.getPlayer())) {
									continue;
								}
							}

							// else can use cell to check

							// Are we at one of the edge nodes?
							if(scoreMap[newCell.y][newCell.x] == sb.getMaxScore()){
								return;	// no new cells in capture list
							}

							//else add surrounding cells to potentialMoves

							currCell = newCell;

							for(i = -1; i <= 1; i++) {
								for(j = -1; j <= 1; j++) {
									if(i != 0 && j != 0) {	// not current cell
										int newRow = (int)currCell.getY() + i;
										int newCol = (int)currCell.getX() + j;
										newCell = new Point(newCol, newRow);
										if(onBoard(newCell) && (explored[newRow][newCol] == 0)){ // on board, not already in potMoves

											int cellScore = scoreMap[newRow][newCol];

											PointPair newPointPair = new PointPair(newCell, currCell);
											potentialMoves.get(cellScore).add(newPointPair);

											exploredList.add(newCell);
											explored[newCell.y][newCell.x] = 1;

										}
									}
								}
							}
							break outerloop; // go back to start of potMoves
						}
					}
				}
		exhausted = true;	// there are no more cells in potentialMoves --> no path to edges
		}

		
		// cells can be added to the captures
		while(!exploredList.isEmpty()){
			int x = exploredList.get(0).x;
			int y = exploredList.get(0).y;
			//TODO Change piece structure
			GameMove newCap = new GameMove(move.getPlayer(), new Point(x,y));

			captureCell(newCap);
			exploredList.remove(0);
		}

		return;
	}

	private static Point nextCell(Point currCell, Point centerCell, boolean clockwise) {
		// TODO	
		// consolidate into fewer cycles, messier code
		if(clockwise){
			boolean topLeft     = (currCell.x <= centerCell.x) && (currCell.y <  centerCell.y);
			boolean topRight    = (currCell.x >  centerCell.x) && (currCell.y <= centerCell.y);	
			boolean bottomRight = (currCell.x >= centerCell.x) && (currCell.y >  centerCell.y);
			boolean bottomLeft  = (currCell.x <  centerCell.x) && (currCell.y >= centerCell.y);

			if 		(topLeft)     return (new Point(currCell.x + 1, currCell.y    ));
			else if (topRight)    return (new Point(currCell.x    , currCell.y - 1));
			else if (bottomRight) return (new Point(currCell.x - 1, currCell.y    ));
			else if (bottomLeft)  return (new Point(currCell.x    , currCell.y + 1));

		} else if(!clockwise){
			boolean topLeft     = (currCell.x <  centerCell.x) && (currCell.y <= centerCell.y);
			boolean topRight    = (currCell.x >= centerCell.x) && (currCell.y <  centerCell.y);	
			boolean bottomRight = (currCell.x >  centerCell.x) && (currCell.y >= centerCell.y);
			boolean bottomLeft  = (currCell.x <= centerCell.x) && (currCell.y >  centerCell.y);

			if      (topLeft)     return (new Point(currCell.x    , currCell.y - 1));
			else if (topRight)    return (new Point(currCell.x - 1, currCell.y    ));
			else if (bottomRight) return (new Point(currCell.x    , currCell.y + 1));
			else if (bottomLeft)  return (new Point(currCell.x + 1, currCell.y    ));
		}
		return null;	//ERROR - technically unreachable
	}

	public static boolean checkUniqueStates(Board a, Board b) {
		byte[][] aBoard = a.getBoard();
		byte[][] bBoard = b.getBoard();
		for (int j = 0; j < a.getBoardSize(); j++) {
			for (int i = 0; i < a.getBoardSize(); i++) {
				//TODO Add in this algorithm
			}
		}
		return false;
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
		// TODO Auto-generated method stub
		return this;
	}

	private Board transform180() {
		// TODO Auto-generated method stub
		return this;
	}

	private Board transformFlipVertical() {
		// TODO Auto-generated method stub
		return this;
	}

	private Board transformFlipHorizontal() {
		// TODO Auto-generated method stub
		return this;
	}

	private Board transformFlipMajorDiagonal() {
		// TODO Auto-generated method stub
		return this;
	}

	private Board transformFlipMinorDiagonal() {
		// TODO Auto-generated method stub
		return this;
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



