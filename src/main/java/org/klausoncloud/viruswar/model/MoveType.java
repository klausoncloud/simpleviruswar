package org.klausoncloud.viruswar.model;

/*
 * The types of moves virus can request. Note, the virus can implement strategies based on the cost of moves. 
 * Idea here is that the cost can change between games. Of course, for this to happen we need to recompile.
 */
public enum MoveType {
    PASS (0),  // Voluntary pass.
	FIRE (0),  // Destroy the piece on a field, if occupied.
    MOVE (2),  // Move to a field. If occupied, the occupying piece will be destroyed.
    SPAWN (5), // Create a new piece on a field. If occupied, the occupying piece will be destroyed.
	ERROR (0); // This allows the player to report an internal error.
    
    int cost; // The number of rounds a virus will be forced to pass if it requests this type of move.
    
    MoveType(int cost) {
    	this.cost = cost;
    }
    
    int getCost() {
    	return cost;
    }
}
