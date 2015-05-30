package aiproj.agent.decisionTree;

import aiproj.agent.board.Board;
import aiproj.squatter.Move;
import aiproj.squatter.Piece;

/**
 * stores the features of a move made in the game 
 */
public class GameMove {
	
	private byte player;	// the id of the player making the move (BLACK or WHITE)
	private int x;			// the x position of the move
	private int y;			// the y position of the cell
	
	/* CONSTRUCTOR */
	public GameMove(int player, int x, int y) {
		this.player = (byte) player;
		this.x = x;
		this.y = y;
	}

	/* GETTERS */
	public byte getPlayer() {return player;}
	public int  getX() 		{return x;}
	public int  getY() 		{return y;}

	/* SETTERS */
	public void setPlayer(byte player) 			{this.player = player;}
	public static GameMove getGameMove(Move m) 	{return new GameMove((byte) m.P, m.Col, m.Row);}
	
	public static Move getMove(GameMove gm) {
		Move m = new Move();
		m.P = gm.getPlayer();
		m.Col = gm.getX();
		m.Row = gm.getY();
		
		return m;
	}
}
