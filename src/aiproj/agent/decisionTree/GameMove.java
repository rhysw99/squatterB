package aiproj.agent.decisionTree;

import java.awt.Point;

import aiproj.squatter.Move;

/* ADT to store move history */
public class GameMove {
	
	/* Maybe use a byte instead and use hashTable or something to get the char from the byte and vice versa as in project A. */
	private byte player;
	/* Using point is better java design, however it has much more overhead than just two bytes */
	private Point location;
	
	public GameMove(int player, Point location) {
		this.player = (byte) player;
		this.location = location;
	}

	public byte getPlayer() {
		return player;
	}

	public Point getLocation() {
		return location;
	}
	
	public static GameMove getGameMove(Move m) {
		return new GameMove((byte) m.P, new Point(m.Col, m.Row));
	}
	
	public static Move getMove(GameMove gm) {
		Move m = new Move();
		m.P = gm.getPlayer();
		m.Col = gm.getLocation().x;
		m.Row = gm.getLocation().y;
		
		return m;
	}
}
