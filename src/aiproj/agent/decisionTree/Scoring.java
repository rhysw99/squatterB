package aiproj.agent.decisionTree;

import java.awt.Point;

import aiproj.agent.board.Board;
import aiproj.agent.decisionTree.Tree.Node;
import aiproj.agent.decisionTree.Tree.Root;
import aiproj.squatter.Piece;

public class Scoring {

	// holdover location, will move to tree.java most likely
	
	
	/* Plan
	 * 
	 * 5x5 plan
	 * need a first mover flag:
	 * 
	 * if true
	 * 		place first piece in center
	 * 		prevent score, look for lambda
	 * 
	 * if false
	 * 		prevent score
	 * 		fill center cross	(1,1), (1,3), (2,2), (3,1), (3,3)
	 * 
	 * 
	 * 
	 * 7x7
	 * look for features (lambda, \, L etc.)
	 * can features center on newest cell?
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	
	public <T> void scoreState5x5(Node<T> currentNode, Root<T> rootNode, byte[][] currentBoard){

		//TODO Node needs player capture difference (scoreDifference)
		//		Update will need to modify them


		// score weightings for different conditions, must be so that more 
		// important weights are larger than any combination of all lesser weights
		int captureWeight     = 645,				
			lambdaWeight      = 129,
			longDiagWeight    = 26,
			shortDiagWeight   = 3,
			centerCellWeight  = 2,
			centerCrossWeight = 1;


		int score = 0;


		// check if either player has scored, if they have no further analysis is needed
		int captureDifference = (currentNode.scoreDifference - rootNode.scoreDifference);
		if(captureDifference != 0)	{currentNode.score = captureDifference*captureWeight; return;}


		//checks if most recent move was on a diagonal cell (cells a chess bishop could reach if it started at the center)
		// these are the important cells for our game plan
		int     posSum = currentNode.getMostRecentMove().x + currentNode.getMostRecentMove().y;
		boolean onDiag = (posSum%2 == 0);

		GameMove recentMove = currentNode.getRecentMove();
		int x 			= recentMove.getLocation().x,	// x location of recent move
				y 			= recentMove.getLocation().y,	// y location of recent move
				transform	= 0,							// iterator
				checks		= 0;							// number of transformations to compare to shortDiag etc.


		byte P	 	= recentMove.getPlayer(),	// player id
				E		= Piece.EMPTY;				// empty  id		//TODO fix Piece reference, create own class

		boolean match = true;					// if comparison to shortDiag is true

		// start scoring
		if(onDiag){
			if((x == 2) && (y == 2))	{score += centerCellWeight;}
			else if((x < 4) && (x > 0) && (y < 4) && (y > 0)){score += centerCrossWeight;}


			// check for short diags 8 possible combos
			GameMove a = new GameMove(E, new Point(2,0)),	// the important cells from short diag
					b = new GameMove(P, new Point(1,1)),	//		0 0 E 0 0
					c = new GameMove(E, new Point(3,1)),	//		0 P 0 E 0
					d = new GameMove(P, new Point(2,2));	//		0 0 P 0 0
															//		0 0 0 0 0
															//		0 0 0 0 0

			checks = 8;		//check all rotations/mirrors
			for(transform = 0; transform <checks; transform++){
				match = true;
				if(!compareMatch5x5(a, currentBoard, transform)) {match = false;}
				if(!compareMatch5x5(b, currentBoard, transform)) {match = false;}
				if(!compareMatch5x5(c, currentBoard, transform)) {match = false;}
				if(!compareMatch5x5(d, currentBoard, transform)) {match = false;}

				if(match){score += shortDiagWeight;}
				else{match = true;}
			}// end for transform 0 ++ <checks
			
			if(score <shortDiagWeight){currentNode.score = score;	return;}	//no short diags found end scoring
			
			
			
			
			
			// check for long diags 4 possible combos
						GameMove e = new GameMove(E, new Point(4,2)),	// the important cells from long diag (builds off short diag)
								 f = new GameMove(P, new Point(3,3));	//		0 0 E 0 0
								 										//		0 P 0 E 0
								 										//		0 0 P 0 E
								 										//		0 0 0 P 0
								 										//		0 0 0 0 0
														 
						checks = 4;		//check all rotations/mirrors
						for(transform = 0; transform <checks; transform++){
							match = true;
							if(!compareMatch5x5(a, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(b, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(c, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(d, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(e, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(f, currentBoard, transform)) {match = false;}

							if(match){score += longDiagWeight;}
							else{match = true;}
						}// end for transform 0 ++ <checks
						
						
	
						
						// check for lambdas 4 possible combos
								c = new GameMove(P, new Point(4,2));	// the important cells for lambda (alters long diag)
																		//		0 0 E 0 0
								 										//		0 P 0 P 0
								 										//		0 0 P 0 E
								 										//		0 0 0 P 0
								 										//		0 0 0 0 0
														 
						checks = 4;		//check all rotations/mirrors
						for(transform = 0; transform <checks; transform++){
							match = true;
							if(!compareMatch5x5(a, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(b, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(c, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(d, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(e, currentBoard, transform)) {match = false;}
							if(!compareMatch5x5(f, currentBoard, transform)) {match = false;}

							if(match){score += lambdaWeight;}
							else{match = true;}
						}// end for transform 0 ++ <checks
						
						
						// no more scoring to do
						currentNode.score = score;	
						return;
						
		}// end if onDiag

		// no scorable pieces
		currentNode.score = score;
		return;
	}
	
	



	public boolean compareMatch5x5(GameMove move, byte[][] board, int transformType){

		int n = 5,
				x		  = (int) move.getLocation().getX(),
				y		  = (int) move.getLocation().getY(),		
				owner	  = (int) move.getPlayer();


		switch(transformType){
		case 0:	return (board[	   y	][x		    ] == owner);			//no rotation
		case 1: return (board[	   x	][(n- 1) - y] == owner);			//rot90
		case 2: return (board[(n- 1) - y][(n- 1) - x] == owner);			//rot180
		case 3:	return (board[(n- 1) - x][     y	] == owner);			//rot270
		case 4: return (board[	   y	][(n- 1) - x] == owner);			//flip |
		case 5: return (board[(n- 1) - x][(n- 1) - y] == owner);			//flip /
		case 6: return (board[(n- 1) - y][	   x	] == owner);			//flip -
		case 7: return (board[	   x	][	   y	] == owner);			//flip \
		default:
			return false;
		}
	}


}

