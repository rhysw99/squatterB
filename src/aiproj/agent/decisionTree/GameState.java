package aiproj.agent.decisionTree;

import java.util.ArrayList;

/* Not sure what to call this */
public class GameState {
	
	/* Do we want to store a comprehensive history of moves or store one move per node and traverse up the tree (and hence back down) to construct the board state? */
	private ArrayList<Move> history;
	
	public GameState(GameState parent, Move currentMove) {
		/* Won't let me do this on one line for some reason.... */
		if (parent != null) {
			this.history = parent.getHistory();
		} else {
			this.history = new ArrayList<Move>();
		}
		this.history.add(currentMove);
	}
	
	public ArrayList<Move> getHistory() {
		return history;
	}

}
