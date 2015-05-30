package aiproj.agent.decisionTree;

import aiproj.agent.board.Board;

/** 
 * contains metadata about the current node in the decision tree it is stored in
 *
 */
public class GameState {
	
	private GameMove move;						// information about the most recent move
	private int score = Integer.MIN_VALUE;		// the minimax board state score
	private int[] captures;						// how many captures are made 
	
	private int depth = 0;						// the vertical distance of node from root

	/* CONSTRUCTORS */
	public GameState(GameState parent, GameMove currentMove) {
		this.move = currentMove;
		if (parent != null) {
			this.captures = parent.getCaptures();
		} else {
			this.captures = new int[3];
		}
	}

	/* GETTERS */
	public int getScore() 		{return score;}
	public int[] getCaptures() 	{return captures;}
	public int getDepth() 		{return depth;}
	public GameMove getMove() 	{return move;}
	
	/* SETTERS */
	public void setMove(GameMove m) {this.move = m;}
	public void setScore(int score) {this.score = score;}
	public void setDepth(int depth) {this.depth = depth;}	
	
	/* METHODS */
	public void incrementCapture(int id) {captures[id]++;}
	
}
