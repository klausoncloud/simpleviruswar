package org.klausoncloud.viruswar.actor;

import java.util.List;

import org.klausoncloud.viruswar.model.Move;
import org.klausoncloud.viruswar.model.MoveNotification;

/*
 * Describes the interface a virus playing in the game has to implement. 
 * The umpire will call the methods.
 */
public interface Actor {
	// Umpire starts game, provides 
	// - dimension of field
	// - number of viruses contesting
	// - id of this virus
	// Expects first move. First move cannot be PASS.
	Move startGame(int width, int height, int virusNumber, int id);
	
	// Umpire requests next move until virus has lost or wins.
    Move nextMove();
    
    // Umpire informs virus of all moved done in the last round.
    public void moveNotification(List<MoveNotification> moveList); 
    
    // Umpire reports end of game.
    void endOfGame(List<Integer> winnerIdList);
}
