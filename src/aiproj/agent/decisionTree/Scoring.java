package aiproj.agent.decisionTree;

import aiproj.agent.Cell;
import aiproj.agent.board.Board;
import aiproj.agent.decisionTree.Tree.Node;

//TODO remove from final product

public class Scoring {
	
	public static void scoreState(Node<GameState> currentNode, Board board, int currentPlayer) {
		//TODO Node needs player capture difference (scoreDifference)
		//		Update will need to modify them
		// only consider top corner for aggression?
		// move to bottom right? will probably see less attention from similar algorithms

		// score weightings for different conditions, must be so that more 
		// important weights are larger than any combination of all lesser weights
		int captureWeight     = 65000,				
			lambdaWeight      = 130,
			longDiagWeight    = 26,
			shortDiagWeight   = 3,
			centerCellWeight  = 2,
			centerCrossWeight = 1;

		
		byte[][] currentBoard = board.getBoard();

		int score = 0;
		int playerID = currentPlayer;	//only care when player makes move, not opponent
		int opponentID = (playerID == Cell.WHITE) ? Cell.BLACK : Cell.WHITE;
		
		// check if either player has scored since root, if they have no further analysis is needed
		int currentPlayerCap = currentNode.getData().getCaptures()[playerID];
		int rootPlayerCap = 0;
		int currentOpponentCap = currentNode.getData().getCaptures()[opponentID];
		int rootOpponentCap = 0;
		
		int currentCapDiff = currentPlayerCap - currentOpponentCap;
		int rootCapDiff = 0;
		
		//System.out.println("currentPlayerCap: "+currentPlayerCap);
		//System.out.println("currentPlayerCap: "+currentOpponentCap);
		//board.printBoard();
		currentNode.getData().setScore(captureWeight*(currentCapDiff - rootCapDiff));
		//System.out.println("score: "+captureWeight*(currentCapDiff - rootCapDiff));
		//checks if most recent move was on a diagonal cell (cells a chess bishop could reach if it started at the center)
		// these are the important cells for our game plan
		
		
		/*
		int mostRecentMoveX = currentNode.getData().getMove().getX();
		int mostRecentMoveY = currentNode.getData().getMove().getY();
		
		int     positionSum = mostRecentMoveX + mostRecentMoveY;
		
		boolean onDiag = (positionSum%2 == 0);

		GameMove recentMove = currentNode.getData().getMove();
		int x 			= recentMove.getX(),	// x location of recent move
			y 			= recentMove.getY(),	// y location of recent move
			transform	= 0,							// iterator through transforms types
			checks		= 0;							// number of transformations to compare to shortDiag etc.

		boolean topLeft = (x < 4 )&& (y < 4);		// constrains offensive moves to the top left 5x5

		byte P	 	= recentMove.getPlayer(),	// player id
			 E		= Cell.EMPTY;				// empty  id		//TODO fix Piece reference, create own class

		boolean match = true;					// if comparison to shortDiag is true

		// start scoring
		
		if(onDiag && topLeft){
			if((x == 2) && (y == 2))	{score += centerCellWeight;}						//newMove on center Cell
			else if((x < 4) && (x > 0) && (y < 4) && (y > 0)){score += centerCrossWeight;}	//newMove in center cross


			// check for short diags, 8 possible combos
			// -1 as value is irrelevant for here
			GameMove a = new GameMove(E, 2, 0),	// the important cells from short diag
					 b = new GameMove(P, 1, 1),	//		0 0 E 0 0
					 c = new GameMove(E, 3, 1),	//		0 P 0 E 0
					 d = new GameMove(P, 2, 2);	//		0 0 P 0 0
												//		0 0 0 0 0
												//		0 0 0 0 0

			checks = 8;		//check all rotations/mirrors
			for(transform = 0; transform <checks; transform++){
				match = true;
				if(!compareMatch5x5(a, currentBoard, transform, playerID)) {match = false;}
				if(!compareMatch5x5(b, currentBoard, transform, playerID)) {match = false;}
				if(!compareMatch5x5(c, currentBoard, transform, playerID)) {match = false;}
				if(!compareMatch5x5(d, currentBoard, transform, playerID)) {match = false;}

				if(match){score += shortDiagWeight; System.out.println("shortMatch");}
			}// end for transform (0; ++; < checks)
			
			if(score <shortDiagWeight){currentNode.getData().setScore(score);	return;}	//if no short diags found end scoring
			
			
			// check for long diags 4 possible combos
						GameMove e = new GameMove(E, 4, 2),	// the important cells from long diag (builds off short diag)
								 f = new GameMove(P, 3, 3);	//		0 0 E 0 0
								 								//		0 P 0 E 0
								 								//		0 0 P 0 E
								 								//		0 0 0 P 0
								 								//		0 0 0 0 0
														 
						checks = 4;		//check all rotations only
						for(transform = 0; transform <checks; transform++) {
							match = true;
							if(!compareMatch5x5(a, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(b, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(c, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(d, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(e, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(f, currentBoard, transform, playerID)) {match = false;}

							if(match){score += longDiagWeight; System.out.println("longMatch");}
						}// end for transform (0; ++ ; <checks)
						
						
	
						
						// check for lambdas 4 possible combos
								c = new GameMove(P, 3, 1);	// the important cells for lambda (alters long diag)
																		//		0 0 E 0 0
								 										//		0 P 0 P 0
								 										//		0 0 P 0 E
								 										//		0 0 0 P 0
								 										//		0 0 0 0 0
														 
						checks = 4;		//check all rotations only
						for(transform = 0; transform < checks; transform++){
							match = true;
							if(!compareMatch5x5(a, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(b, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(c, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(d, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(e, currentBoard, transform, playerID)) {match = false;}
							if(!compareMatch5x5(f, currentBoard, transform, playerID)) {match = false;}

							if(match){score += lambdaWeight;System.out.println("lambdaMatch");}
						}// end for transform 0 ++ <checks
						
						
						// no more scoring to do
						currentNode.getData().setScore(score);	
						return;
						
		}// end if onDiag

		// no scorable pieces we care about
		
		currentNode.getData().setScore(score);	//score is 0
		return;*/
	}
	
	public static boolean compareMatch5x5(GameMove move, byte[][] board, int transformType, int playerID){

		int n 		  = 5,									// boardSize always 5 as only called for top left 5x5
			x		  = (int) move.getX(),
			y		  = (int) move.getY(),		
			owner	  = (int) move.getPlayer();

		boolean matchPlayer, matchEmpty;
		
		if(owner == playerID){	//must be player piece in comparison
			switch(transformType){
				case 0:	return (board[	   y	 ][	    x     ] == owner);			//no rotation
				case 1: return (board[	   x	 ][(n - 1) - y] == owner);			//rot90
				case 2: return (board[(n - 1) - y][(n - 1) - x] == owner);			//rot180
				case 3:	return (board[(n - 1) - x][     y	  ] == owner);			//rot270
				case 4: return (board[	   y	 ][(n - 1) - x] == owner);			//flip |
				case 5: return (board[(n - 1) - x][(n - 1) - y] == owner);			//flip /
				case 6: return (board[(n - 1) - y][	    x	  ] == owner);			//flip -
				case 7: return (board[	   x	 ][	    y	  ] == owner);			//flip \
				default:
					return false;
			}	
		}else if(owner == Cell.EMPTY) {
			switch(transformType) {
				case 0:																//no rotation
					matchPlayer = board[	   y	 ][	    x     ] == owner;
					matchEmpty  = board[	   y	 ][	    x     ] == playerID;
					return matchPlayer || matchEmpty;	
					
				case 1:																//rot90
					matchPlayer = board[	   x	 ][(n - 1) - y] == owner;
					matchEmpty  = board[	   x	 ][(n - 1) - y] == playerID;
					return matchPlayer || matchEmpty;		
			
				case 2: 															//rot180
					matchPlayer = board[(n - 1) - y][(n - 1) - x] == owner;
					matchEmpty  = board[(n - 1) - y][(n - 1) - x] == playerID;
					return matchPlayer || matchEmpty;
					
				case 3:																//rot270
					matchPlayer = board[(n - 1) - x][     y	  ] == owner;
					matchEmpty  = board[(n - 1) - x][     y	  ] == playerID;
					return matchPlayer || matchEmpty;
				
				case 4: 															//flip |
					matchPlayer = board[	   y	 ][(n - 1) - x] == owner;
					matchEmpty  = board[	   y	 ][(n - 1) - x] == playerID;
					return matchPlayer || matchEmpty;
					
				case 5: 															//flip /
					matchPlayer = board[(n - 1) - x][(n - 1) - y] == owner;
					matchEmpty  = board[(n - 1) - x][(n - 1) - y] == playerID;
					return matchPlayer || matchEmpty;
					
				case 6: 															//flip -
					matchPlayer = board[(n - 1) - y][	    x	  ] == owner;
					matchEmpty  = board[(n - 1) - y][	    x	  ] == playerID;
					return matchPlayer || matchEmpty;
					
				case 7:																//flip \
					matchPlayer = board[	   x	 ][	    y	  ] == owner;
					matchEmpty  = board[	   x	 ][	    y	  ] == playerID;
					return matchPlayer || matchEmpty;
				
				default:
					return false;
			}
		}
		return false;
	}
}

