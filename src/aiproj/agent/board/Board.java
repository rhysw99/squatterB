package aiproj.agent.board;
   
import aiproj.agent.decisionTree.GameMove;
import aiproj.squatter.Move;
import aiproj.squatter.Piece;

import java.awt.Point;

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
	public void setCell(Move newMove) {
		this.board[newMove.Col][newMove.Row] = (byte) newMove.P;
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


	public boolean isLegal(GameMove newMove){
		return isLegal(this, newMove);
	}
	
	public static boolean isLegal(Board b, GameMove newMove) {
		boolean onBoardX = (newMove.getLocation().x >= 0) && (newMove.getLocation().x < b.boardSize);
		boolean onBoardY = (newMove.getLocation().y >= 0) && (newMove.getLocation().y < b.boardSize);
	
		boolean canPlace = (b.board[newMove.getLocation().y][newMove.getLocation().x] == Piece.EMPTY); 
		
		if (!(onBoardX && onBoardY && canPlace)) {
			Update.updateBoard(newMove, b);
			
			return false;
		}
		
		return true;
	}
	
	public void updateBoard(Move newMove)
		byte[][] board = gameBoard.getBoard();
		int boardSize = gameBoard.getBoardSize();
		
		board[newMove.Row][newMove.Col] = (byte) newMove.P;
		
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

}



