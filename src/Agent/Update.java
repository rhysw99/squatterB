package Agent;

import java.awt.Point;
import java.util.ArrayList;

import aiproj.squatter.Move;
import aiproj.squatter.Piece;

public class Update {

	
	/* METHODS */
	public static void updateBoard(Move newMove, Board gameBoard){
		// TODO
		
		int[][] board = gameBoard.getBoard();
		int boardSize = gameBoard.getBoardSize();
		
		board[newMove.Row][newMove.Col] = newMove.P;
		
		// find pathfinding start points
			
		int top  = (newMove.Row <= boardSize/2) ? -1:1;
		int left = (newMove.Col <= boardSize/2) ? -1:1;
		
		Point centerCell   = new Point(newMove.Col,        newMove.Row);
		Point startCell    = new Point(newMove.Col + left, newMove.Row + top);
		
		Point ptrClockwise = new Point(startCell);
		Point ptrCounter   = new Point(startCell);
		
		// can path from startCell, else path from next available cell
		// if no paths are possible, end
		if(board[startCell.y][startCell.x] != newMove.P){
			pathfind(startCell, newMove.P, gameBoard);
		}else{
			ptrClockwise = nextCell(ptrClockwise, centerCell, true);
			while(board[ptrClockwise.y][ptrClockwise.x] == newMove.P){
				ptrClockwise = nextCell(ptrClockwise, centerCell, true);
				if(ptrClockwise == ptrCounter) {return;}
			}
			pathfind(ptrClockwise, newMove.P, gameBoard);
		}
		
		// move pointer clockwise until blocked
		while(board[ptrClockwise.y][ptrClockwise.x] != newMove.P){
			ptrClockwise = nextCell(ptrClockwise, centerCell, true);		//magic number
		}

		//move pointer counterclockwise until blocked
		while(board[ptrCounter.y][ptrCounter.x] != newMove.P){
			ptrCounter = nextCell(ptrCounter, centerCell, false);		//magic number
		}
		
		// no more cells to check
		boolean newPath = true;
		
		while(ptrClockwise != ptrCounter){
			if(newPath &&(board[ptrClockwise.y][ptrClockwise.x] != newMove.P)){
				pathfind(ptrClockwise, newMove.P, gameBoard);
				newPath = false;
			}else if(!newPath && (board[ptrClockwise.y][ptrClockwise.x] == newMove.P)){
				newPath = true;
			}
		}
		
		return;
		
		// TODO
		// consider Points not in board range
		
		// look over start point detections
		// in particular if startCell is necessary, can't be replace by ptrCounter
		
		/* outline
		start path from startCell (if possible)
		otherwise continue clockwise until a start point is reached
		when ptrClockwise and ptrCounter overlap boardUpdate is finished
		pointer 1 goes clockwise until blocked
		pointer 2 goes counter clockwise till blocked
		
		ptrClockwise continues till not player cell
		sets cell as next path begin
		repeats till ptr's overlap
		*/
		
	}

	private static void pathfind(Point startPath, int player, Board gameBoard) {
		
		// TODO
		
		/* Plan
		 * 
		 * Reference Map determines A* next move by highest value
		 * Board that values edges as highest score
		 * score is roughly a manhattan from center of board, edges are set to highest value
		 * abs(currPos - centerPos) roughly
		 * 
		 * pot_moves list
		 * every cell that is adjacent to a previously checked cell
		 * number of possible values is boardSize - 1 if odd
		 * 								boardSize - 2 if even
		 * if(boardSize%2)
		 * 
		 * make potMoves list a 2D array of Points				**** perhaps 2d array of Points and Originating Points
		 * iterate through vertically, highest scores
		 * then horizontally, through options
		 * 
		 * for(i = boardSize - (1 or 2) ; i >= 0 ; i--){
		 * 		if(potMoves[i] is not empty){
		 * 			iterate through entries there
		 * 			if(entry found){
		 * 				add adjacent cells to potMoves
		 * 				break to top for loop
		 * 			}
		 * 		}
		 * }
		 * 
		 * 
		 * will have to check startCell first in case where is already at edge
		 * 
		 * 
		 * store list movesMade, if no return from above recursion-y thing then
		 * all cells in movesMade are captures. 
		 * 
		 * 
		 * Progression paths
		 * dead end if next cell is player owned, or two communally adjacent cells are player owned
		 * communally adjacent only applies if previous cell is known - may lead to problem
		 * if dead end then do not add cell to movesMade, do not add adjacent cells to potMoves
		 * 
		 * need to prevent considering cells multiple time, perhaps scoremap has visited flag
		 * 	requires score map to be rebuilt each time that pathfind is run
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		
		int boardSize    = gameBoard.getBoardSize();
		int maxScore     = gameBoard.getMaxScore();
		int[][] board    = gameBoard.getBoard();
		int[][] explored = gameBoard.getExplored();
		int[][] scoreMap = gameBoard.getScoreMap();
		
		
		//start pathfinding
		int i,j;
		
		int row = startPath.y;
		int col = startPath.x;
		
		// creates a static array of dynamic lists
		// stores variable length lists of possible next cells
		// indexed in static list by score of possible next cell
		// board is scored so that cells can be scored between 0 and maxScore 
		// so that static array is as small as possible
		ArrayList<DoublePoint>[] nextCell = (ArrayList<DoublePoint>[]) new ArrayList[maxScore];
		for(i = 0; i < maxScore; i++){
			nextCell[i] = new ArrayList<DoublePoint>();
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
		
		//explore cells around
		// TODO fix nulls
		Point nextPosCell = null, currCell;
		int newRow, newCol, cellScore;
		DoublePoint newPointPair = null;
		
		currCell = startPath;
		newRow = row;
		newCol = col;
		
		for(i = -1; i <= 1; i++){
			for(j = -1; j <=1; j++){
				if(i != 0 && j != 0){	// not current cell
					newRow = row + i;
					newCol = col + j;

					if(newRow >= 0 && newRow < boardSize && newCol >= 0 && newCol < boardSize ){
						// new cell is on board
						// TODO maybe create stand-alone function for onBoard()

						cellScore = scoreMap[newRow][newCol];
						nextPosCell.setLocation(newCol, newRow);

						newPointPair.setNewCell(nextPosCell);
						newPointPair.setPrevCell(currCell);
						nextCell[cellScore].add(newPointPair);

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
			for(i = maxScore; i >= 0; i--){							// start looking at highest scoring possibles
				
				if(!nextCell[i].isEmpty()){							// is possible point of score i available?
					// there are moves with score i to try
					nextTry = (DoublePoint) nextCell[i].get(0);

					newCell = nextTry.getNewCell();
					prevCell = nextTry.getPrevCell();

					newCellX = (int)newCell.getX();
					newCellY = (int)newCell.getY();
					prevCellX = (int)prevCell.getX();
					prevCellY = (int)prevCell.getY();
					
					//check that cell can be moved to
					if(board[newCellY][newCellX] == player){break;}

					// if diagonal check for adjacents
					if((Math.abs(newCellX - prevCellX) + Math.abs(newCellY - prevCellY)) != 0){ // diagonal movement

						int ownerCellX, ownerCellY;

						ownerCellX = board[prevCellY + (newCellY - prevCellY)][prevCellX                         ];
						ownerCellY = board[prevCellY                         ][prevCellX + (newCellX - prevCellX)];

						if((ownerCellX == player) && (ownerCellY == player)){break;}	//cannot path to cell
					}
					
					// else can use cell to check
					
					//at edge?
					if(scoreMap[newCellY][newCellX] == maxScore){
						return;	// no new cells in capture list
					}
					
					//else add surrounding cells to nextCell
					for(i = -1; i <= 1; i++){
						for(j = -1; j <=1; j++){
							if(i != 0 && j != 0){	// not current cell
								newRow = row + i;
								newCol = col + j;

								cellScore = scoreMap[newRow][newCol];
								nextPosCell.setLocation(newCol, newRow);

								newPointPair.setNewCell(nextPosCell);
								newPointPair.setPrevCell(currCell);
								nextCell[cellScore].add(newPointPair);
								
								exploredList.add(newCell);
								
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
			Move newCap = new Move();
			newCap.Row = exploredList.get(0).y;
			newCap.Col = exploredList.get(0).x;
			newCap.P   = Piece.NO_VALUE_FOR_CAPTURE;
			
			gameBoard.setCell(newCap);
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
			
			if      (topLeft)     return (new Point(currCell.x + 1, currCell.y    ));
			else if (topRight)    return (new Point(currCell.x    , currCell.y - 1));
			else if (bottomRight) return (new Point(currCell.x - 1, currCell.y    ));
			else if (bottomLeft)  return (new Point(currCell.x    , currCell.y + 1));
			
		}else if(!clockwise){
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
			
	
}
