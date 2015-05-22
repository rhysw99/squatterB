package aiproj.agent.decisionTree;

import java.awt.Point;


/* Not sure what to call this */
public class GameState {
	
	/* Do we want to store a comprehensive history of moves or store one move per node and traverse up the tree (and hence back down) to construct the board state? */
	private GameMove move;
	
	private boolean[] transformFlag = new boolean[7];
	
	private static final byte ROTATE_90_CW = 0;
	private static final byte ROTATE_90_CCW = 1;
	private static final byte ROTATE_180 = 2;
	private static final byte FLIP_VERTICAL = 3;
	private static final byte FLIP_HORIZONTAL = 4;
	private static final byte FLIP_MAJOR_DIAGONAL = 5;
	private static final byte FLIP_MINOR_DIAGONAL = 6;
	
	public GameState(GameState parent, GameMove currentMove) {
		this.move = currentMove;
	}
	
	public void checkGameStateMatch(GameState existing) {
		transformFlag[ROTATE_90_CW] = false;
	}

}
