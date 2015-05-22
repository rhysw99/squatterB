package aiproj.agent.decisionTree;

import java.awt.Point;

import aiproj.agent.decisionTree.Tree.Root;
import aiproj.squatter.Piece;

/* Test class to show usage of tree class */
public class ExampleUsage {
	
	public ExampleUsage() {
		// EMPTY = 0
		// WHITE = 1
		// BLACK = 2
		// WHITE_C = 3
		// BLACK_C = 4
		byte[][] board = new byte[5][5];
		Tree<GameState> decisionTree = new Tree<GameState>(board);
		Root<GameState> root = decisionTree.getRoot();
		root.insert(new GameState(root.getData(), new GameMove(Piece.BLACK, new Point(0,1))));
		root.insert(new GameState(root.getData(), new GameMove(Piece.WHITE, new Point(0,2))));
		root.insert(new GameState(root.getData(), new GameMove(Piece.BLACK, new Point(0,3))));
	}


}
