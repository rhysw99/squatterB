package Agent;
   
import aiproj.squatter.Move;
import aiproj.squatter.Piece;

import java.awt.Point;


public class Board{

	
	// TODO make into bytes
	private int[][] board;			//the game board
	private int[][] explored;		//has pathfind explored which cell
	private int[][] scoreMap;		//best next move for pathfind
	
	private int     boardSize;
	private int		maxScore;		// max score of the score map function
	
	/* BUILDER */
	public Board(int boardSize){
		this.boardSize = boardSize;
		this.board     = new int[boardSize][boardSize];
		this.explored  = new int[boardSize][boardSize];
		this.maxScore  = genMetadata(boardSize);
		
		
			
	}
	
	
	/* SETTER */
	public void setCell(Move newMove)		{this.board[newMove.Col][newMove.Row] = newMove.P;}
	
	/* GETTER */
	public int getCell(Point point)	{return this.board[point.y][point.x];}
	public int getBoardSize()		{return boardSize;}
	public int getMaxScore()		{return maxScore;}
	
	public int[][] getBoard()		{return board;}
	public int[][] getScoreMap()	{return scoreMap;}
	public int[][] getExplored()	{return explored;}

	
	
	
	public int isLegal(Move newMove, int boardSize){
		
		boolean onBoardX = (newMove.Col >= 0) && (newMove.Col < boardSize);
		boolean onBoardY = (newMove.Row >= 0) && (newMove.Row < boardSize);
	
		boolean canPlace = (this.board[newMove.Col][newMove.Row] == Piece.EMPTY); 
		
		if(onBoardX && onBoardY && canPlace){
			Update.updateBoard(newMove, this);
			
			return 0;
		} else {
			return -1;
		}
	}
	
	
	private int genMetadata(int boardSize){
		// populates the score map
		// TODO check the maths on this one
		
		int maxValue, i, j;
		int isEven;
		float vertScore, horiScore;
		
		for(i = 0; i<boardSize; i++){
			for(j = 0; j<boardSize; j++){
				explored[i][j] = 0;
			}
		}
		
		

		//find max value of score map function
		// score is essentially manhattan distance from center of board
		if(boardSize%2 == 0){ 	//if even
			maxValue = boardSize - 3;
			isEven = 1;
		}else{
			maxValue = boardSize - 2;
			isEven = 0;
		}
		
		
		for(i = 0; i<boardSize; i++){
			for(j = 0; j<boardSize; j++){
				if( j == 0 || i == 0 || i == boardSize - 1 || j == boardSize - 1){
					scoreMap[i][j] = maxValue;											
				}else{
					vertScore = Math.abs((float)(boardSize - 1)/2 - i);
					horiScore = Math.abs((float)(boardSize - 1)/2 - j);
					scoreMap[i][j] = (int)(vertScore + horiScore - isEven); 
				}
			}
		}
		return maxValue;
	}



}



