package aiproj.agent.decisionTree;

import java.awt.Point;

import aiproj.agent.board.Board;
import aiproj.squatter.Move;
import aiproj.squatter.Piece;

/* ADT to store move history */
public class GameMove {

	/* Maybe use a byte instead and use hashTable or something to get the char from the byte and vice versa as in project A. */
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

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public static GameMove getGameMove(Board b, int move) {
		int size = b.getBoardSize();
		int player;
		if (move > size*11) {
			player = Piece.WHITE;
		} else {
			player = Piece.BLACK;
		}
		move = (player == Piece.BLACK) ? move-size*11 : move;
		int y = move / 10;
		int x = move - y*10;
		
		return new GameMove(player, x, y);
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
