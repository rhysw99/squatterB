package aiproj.agent.board;
   
import aiproj.agent.Ajmorton;
import aiproj.agent.DoublePoint;
import aiproj.agent.decisionTree.GameMove;
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
		this.board[move.getLocation().x][move.getLocation().y] = (byte) move.getPlayer();
	}
	
	/* GETTER */
	public int getCell(Point point)	{
		return this.board[point.y][point.x];
	}
	
	public int getBoardSize() {
		return boardSize;
	}
	
	public byte[][] getBoard() {
		return board;
	}

	public boolean isLegal(GameMove move){
		boolean onBoardX = (move.getLocation().x >= 0) && (move.getLocation().x < boardSize);
		boolean onBoardY = (move.getLocation().y >= 0) && (move.getLocation().y < boardSize);
	
		boolean canPlace = (board[move.getLocation().y][move.getLocation().x] == Piece.EMPTY); 
		
		if (!(onBoardX && onBoardY && canPlace)) {
			updateBoard(move);
			
			return false;
		}
		
		return true;
	}
	
	public void updateBoard(GameMove move) {		
		board[move.getLocation().y][move.getLocation().x] = move.getPlayer();
		
		checkCaptures(move);
	}
	
	public boolean onBoard(Point p) {
		if (p.x >= 0 && p.y >= 0 && p.x < boardSize && p.y < boardSize) {
			return true;
		}
		return false;
	}
	
	
	private void checkCaptures(GameMove move) {
		/* Find the point adjacent from the move made which is closest to a corner.*/		
		int yOffset  = (move.getLocation().y <= boardSize/2) ? -1:1;
		int xOffset = (move.getLocation().x <= boardSize/2) ? -1:1;

		Point startCell = new Point(move.getLocation().x + xOffset, move.getLocation().y + yOffset);

		Point pathStart = new Point(startCell);
		Point endCell = new Point(startCell);

		// can path from startCell, else path from next available cell
		// if no paths are possible, end
		if(board[startCell.y][startCell.x] != move.getPlayer()){
			pathfind(startCell, move);
		} else {
			pathStart = nextCell(pathStart, move.getLocation(), true);
			while(board[pathStart.y][pathStart.x] == move.getPlayer()){
				pathStart = nextCell(pathStart, move.getLocation(), true);
				if(pathStart == endCell) {
					return;
				}
			}
			pathfind(pathStart, move);
		}

		// move pointer clockwise until blocked
		while(board[pathStart.y][pathStart.x] != move.getPlayer()){
			pathStart = nextCell(pathStart, move.getLocation(), true);		//magic number
		}

		//move pointer counterclockwise until blocked
		while(board[endCell.y][endCell.x] != move.getPlayer()){
			endCell = nextCell(endCell, move.getLocation(), false);		//magic number
		}

		// no more cells to check
		boolean newPath = true;

		while(pathStart != endCell) {
			if(newPath && (board[pathStart.y][pathStart.x] != move.getPlayer())) {
				pathfind(pathStart, move);
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
	
	private void pathfind(Point startPath, GameMove move) {
		ScoreBoard sb = Ajmorton.getScoreBoard();
		byte[][] explored = new byte[boardSize][boardSize];
		byte[][] scoreMap = sb.getBoard();
		
		//start pathfinding
		int i,j;
		
		int row = startPath.y;
		int col = startPath.x;
		
		// creates a static array of dynamic lists
		// stores variable length lists of possible next cells
		// indexed in static list by score of possible next cell
		// board is scored so that cells can be scored between 0 and maxScore 
		// so that static array is as small as possible
		HashMap<Integer, ArrayList<DoublePoint>> potentialMoves = new HashMap<Integer, ArrayList<DoublePoint>>(sb.getMaxScore() + 1);
		
		for (int a = 0; a < sb.getMaxScore(); a++) {
			potentialMoves.put(a, new ArrayList<DoublePoint>());
		}
		
		// list of p cells that have been explored, if no path to edge is found
		// all cells in list are captured
		ArrayList<Point> exploredList = new ArrayList<Point>();
		
		// make sure start point is on board
		if(row < 0 || row >=boardSize || col < 0 || col >=boardSize)	{return;}
		
		//check if already at edge
		if(row == 0 || row == boardSize || col == 0 || col == boardSize){return;}
		
		//add finished cell to explored list
		exploredList.add(startPath);
		explored[startPath.y][startPath.x] = 1;;
		
		//explore cells around
		// TODO fix nulls
		Point nextPosCell = null, currCell;
		int newRow, newCol, cellScore;
		DoublePoint newPointPair = null;
		
		currCell = startPath;
		newRow = row;
		newCol = col;
		
		for(i = -1; i <= 1; i++) {
			for(j = -1; j <= 1; j++) {
				if(i != 0 && j != 0) {	// not current cell
					newRow = row + i;
					newCol = col + j;

					if (onBoard(new Point(newCol, newRow))) {
						cellScore = sb.getValue(new Point(newCol, newRow));
						nextPosCell.setLocation(newCol, newRow);

						newPointPair.setNewCell(nextPosCell);
						newPointPair.setPrevCell(currCell);
						potentialMoves.get(cellScore).add(newPointPair);
						explored[(int)nextPosCell.getY()][(int)nextPosCell.getX()] = 1;

					}
				}
			}
		}
	
		// end initial cell check
		
		DoublePoint nextTry;
		Point newCell;
		Point prevCell;

		int newCellX, newCellY, prevCellX, prevCellY;
		boolean exhausted = false;
		
		while(!exhausted){
			for(i = sb.getMaxScore(); i >= 0; i--){							// start looking at highest scoring possibles
				if(!potentialMoves.get(i).isEmpty()) {							// is possible point of score i available?
					// there are moves with score i to try
					nextTry = potentialMoves.get(i).get(0);

					newCell  = nextTry.getNewCell();
					prevCell = nextTry.getPrevCell();

					newCellX  = newCell.x;
					newCellY  = newCell.y;
					prevCellX = prevCell.x;
					prevCellY = prevCell.y;
					
					//check that cell can be moved to
					if(board[newCellY][newCellX] == move.getPlayer()) {
						break;
					}

					//check that cell is not previously explored
					if(explored[newCellY][newCellX] == 1){
						break;
					}
					
					// if diagonal check for adjacents
					if((Math.abs(newCellX - prevCellX) + Math.abs(newCellY - prevCellY)) != 0) { // diagonal movement
						int ownerCellX, ownerCellY;

						ownerCellX = board[prevCellY + (newCellY - prevCellY)][prevCellX];
						ownerCellY = board[prevCellY][prevCellX + (newCellX - prevCellX)];

						if((ownerCellX == move.getPlayer()) && (ownerCellY == move.getPlayer())) {
							break;
						}	//cannot path to cell
					}
					
					// else can use cell to check
					
					// Are we at one of the edge nodes?
					if(scoreMap[newCellY][newCellX] == sb.getMaxScore()){
						return;	// no new cells in capture list
					}
					
					//else add surrounding cells to potentialMoves
					for(i = -1; i <= 1; i++) {
						for(j = -1; j <= 1; j++) {
							if(i != 0 && j != 0){	// not current cell
								newRow = row + i;
								newCol = col + j;

								cellScore = scoreMap[newRow][newCol];
								nextPosCell.setLocation(newCol, newRow);

								newPointPair.setNewCell(nextPosCell);
								newPointPair.setPrevCell(currCell);
								potentialMoves.get(cellScore).add(newPointPair);
								
								exploredList.add(newCell);
								explored[newCell.y][newCell.x] = 1;
								
								break;

							}
						}
					}
				}
			}
			exhausted = true;
		}
		
		// cells can be added to the captures
		
		while(!exploredList.isEmpty()){
			int x = exploredList.get(0).x;
			int y = exploredList.get(0).y;
			//TODO Change piece structure
			GameMove newCap = new GameMove(Piece.NO_VALUE_FOR_CAPTURE, new Point(x,y));
			
			setCell(newCap);
			exploredList.remove(0);
		}
		
		return;
	}
	
	private static Point nextCell(Point currCell, Point centerCell, boolean clockwise){
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
		return new Point(-1,-1);	//ERROR - technically unreachable
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



