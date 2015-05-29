package aiproj.agent.decisionTree;

import aiproj.agent.board.Board;
import aiproj.squatter.Move;
import aiproj.squatter.Piece;

/* ADT to store move history */
public class GameMove {
	
	private byte player;
	private int x;
	private int y;
	
	public GameMove(int player, int x, int y) {
		this.player = (byte) player;
		this.x = x;
		this.y = y;
	}

	public byte getPlayer() {
		return player;
	}

	public void setPlayer(byte player) {
		this.player = player;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public static GameMove getGameMove(Move m) {
		return new GameMove((byte) m.P, m.Col, m.Row);
	}
	
	public static Move getMove(GameMove gm) {
		Move m = new Move();
		m.P = gm.getPlayer();
		m.Col = gm.getX();
		m.Row = gm.getY();
		
		return m;
	}
}
