package aiproj.agent.board;

import java.awt.Point;
import java.util.ArrayList;

import aiproj.agent.DoublePoint;
import aiproj.agent.decisionTree.GameMove;
import aiproj.squatter.Move;
import aiproj.squatter.Piece;

public class Update {

	
	/* METHODS */
	private static Point nextCell(Point currCell, Point centerCell, boolean clockwise){
		// TODO	
		// consolidate into fewer cycles, messier code
		if(clockwise){
			boolean topLeft     = (currCell.x <= centerCell.x) && (currCell.y <  centerCell.y);
			boolean topRight    = (currCell.x >  centerCell.x) && (currCell.y <= centerCell.y);	
			boolean bottomRight = (currCell.x >= centerCell.x) && (currCell.y >  centerCell.y);
			boolean bottomLeft  = (currCell.x <  centerCell.x) && (currCell.y >= centerCell.y);
			
			if 		(topLeft)     return (new Point(currCell.x + 1, currCell.y    ));
			else if (topRight)    return (new Point(currCell.x    , currCell.y - 1));
			else if (bottomRight) return (new Point(currCell.x - 1, currCell.y    ));
			else if (bottomLeft)  return (new Point(currCell.x    , currCell.y + 1));
			
		} else if(!clockwise){
			boolean topLeft     = (currCell.x <  centerCell.x) && (currCell.y <= centerCell.y);
			boolean topRight    = (currCell.x >= centerCell.x) && (currCell.y <  centerCell.y);	
			boolean bottomRight = (currCell.x >  centerCell.x) && (currCell.y >= centerCell.y);
			boolean bottomLeft  = (currCell.x <= centerCell.x) && (currCell.y >  centerCell.y);
			
			if      (topLeft)     return (new Point(currCell.x    , currCell.y - 1));
			else if (topRight)    return (new Point(currCell.x - 1, currCell.y    ));
			else if (bottomRight) return (new Point(currCell.x    , currCell.y + 1));
			else if (bottomLeft)  return (new Point(currCell.x + 1, currCell.y    ));
		}
		return new Point(-1,-1);	//ERROR - technically unreachable
	}
			
	
}
