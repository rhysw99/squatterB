package Agent;
   
import aiproj.squatter.Move;
import aiproj.squatter.Piece;

import java.awt.Point;

public class Board{

	private int[][] board;
	private int boardSize;
	
	/* BUILDER */
	public Board(int boardSize){
		this.boardSize = boardSize;
		this.board = new int[boardSize][boardSize];
	}
	
	
	/* SETTER */
	public void setCell(Move newMove)		{this.board[newMove.Col][newMove.Row] = newMove.P;}
	
	/* GETTER */
	public int getCell(Point point)	{return this.board[point.y][point.x];}


	
	/* METHODS */
	public void updateBoard(Move newMove){
		// TODO
		
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
			pathfind(startCell);
		}else{
			ptrClockwise = nextCell(ptrClockwise, centerCell, true);
			while(board[ptrClockwise.y][ptrClockwise.x] == newMove.P){
				ptrClockwise = nextCell(ptrClockwise, centerCell, true);
				if(ptrClockwise == ptrCounter) {return;}
			}
			pathfind(ptrClockwise);
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
				pathfind(ptrClockwise);
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

	private void pathfind(Point startPath) {
		
		
		
	}

	
	
	private Point nextCell(Point currCell, Point centerCell, boolean clockwise){
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
	
	
	public int isLegal(Move newMove, int boardSize){
		
		boolean onBoardX = (newMove.Col >= 0) && (newMove.Col < boardSize);
		boolean onBoardY = (newMove.Row >= 0) && (newMove.Row < boardSize);
	
		boolean canPlace = (this.board[newMove.Col][newMove.Row] == Piece.EMPTY); 
		
		if(onBoardX && onBoardY && canPlace){
			updateBoard(newMove);
			return 0;
		} else {
			return -1;
		}
	}
}
