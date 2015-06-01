package aiproj.ajmorton.agent.decisionTree;

/** 
2  * COMP30024 Artificial Intelligence 
3  * Project B
4  * ajmorton Andrew Morton 522139  
5  * rhysw    Rhys Williams 661561 
6  */ 

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
		this.captures = new int[3];
		if (parent != null) {
			int[] pCaptures = parent.getCaptures();
			System.arraycopy(pCaptures, 0, this.captures, 0, pCaptures.length);
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
