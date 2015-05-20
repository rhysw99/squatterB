package aiproj.agent.decisionTree;

import java.awt.Point;

import aiproj.agent.decisionTree.Tree.Root;

/* Test class to show usage of tree class */
public class ExampleUsage {
	
	public ExampleUsage() {
		// EMPTY = 0
		// WHITE = 1
		// BLACK = 2
		// WHITE_C = 3
		// BLACK_C = 4
		byte[][] board = new byte[][] {{0, 0, 0},{0, 1, 2},{0, 0, 0}};
		Tree<GameState> decisionTree = new Tree<GameState>(board);
		Root<GameState> root = decisionTree.getRoot();
		root.insert(new GameState(root.getData(), new Move('B', new Point(0,1))));
		
	}


}
