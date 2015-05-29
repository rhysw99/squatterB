package aiproj.agent.decisionTree;

import aiproj.agent.board.Board;

/* Not sure what to call this */
public class GameState {
	
	/* Do we want to store a comprehensive history of moves or store one move per node and traverse up the tree (and hence back down) to construct the board state? */
	private GameMove move;
	private int score = Integer.MIN_VALUE;
	private int[] captures;
	
	private int depth = 0;

	public GameState(GameState parent, GameMove currentMove) {
		this.move = currentMove;
		this.captures = new int[3];
		if (parent != null) {
			int[] pCaptures = parent.getCaptures();
			System.arraycopy(pCaptures, 0, this.captures, 0, pCaptures.length);
		}
	}

	public GameMove getMove() {
		return move;
	}
	
	public void setMove(GameMove m) {
		this.move = m;
	}
	
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;		
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public void incrementCapture(int id) {
		captures[id]++;
	}
	
	public int[] getCaptures() {
		return captures;
	}
	
	public int getDepth() {
		return depth;
	}
	
}
