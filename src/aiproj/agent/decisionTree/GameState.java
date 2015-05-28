package aiproj.agent.decisionTree;

import aiproj.agent.board.Board;

/* Not sure what to call this */
public class GameState {
	
	/* Do we want to store a comprehensive history of moves or store one move per node and traverse up the tree (and hence back down) to construct the board state? */
	private GameMove move;
	
	private byte[] moves;
	
	private boolean[] transformFlags;
	
	private int score = Integer.MIN_VALUE;
	
	private int depth = 0;
	
	public int id = 0;
	
	private static final byte FLIP_VERTICAL = 0;
	private static final byte FLIP_HORIZONTAL = 1;
	private static final byte FLIP_MAJOR_DIAGONAL = 2;
	private static final byte FLIP_MINOR_DIAGONAL = 3;
	
	public GameState(GameState parent, GameMove currentMove) {
		this.move = currentMove;
		this.id = (int) Math.round(Math.random() * 1000000);
		this.moves = new byte[4];
		
		if (parent != null) {
			transformFlags = parent.transformFlags;
		} else {
			transformFlags = new boolean[4];
			for (int i = 0; i < transformFlags.length; i++) {
				transformFlags[i] = true;
			}
		}
	}

	public GameMove getMove() {
		return move;
	}
	
	public void calculateScore(Board b) {
		score = 20 + (int) (Math.random()*500000);
	}
	
	public int getScore() {
		return score;
	}
	
	public boolean[] getTransformFlags() {
		return transformFlags;
	}
	
	public void setTransformFlag(int flagId, boolean value) {
		this.transformFlags[flagId] = value;
	}

	public void setScore(int score) {
		this.score = score;		
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public int getDepth() {
		return depth;
	}
	
}
