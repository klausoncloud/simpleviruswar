package org.klausoncloud.viruswar.actor;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.klausoncloud.viruswar.model.Move;
import org.klausoncloud.viruswar.model.MoveNotification;
import org.klausoncloud.viruswar.model.MoveType;

/*
 * The actor part of a player. Implements an algorithm to destroy other viruses.
 */
public abstract class BuiltInActor implements Actor {
	
	int boardW, boardH, numViruses, id;
	int myPosW, myPosH;

	public Move startGame(int width, int height, int virusNumber, int id) {
		this.boardW = width;
		this.boardH = height;
		this.numViruses = virusNumber;
		this.id = id;
		
		return spawn();
	}

	public abstract Move nextMove();

	public void moveNotification(List<MoveNotification> moveList) {
		// Default: Ignore.
	}
	
	public void endOfGame(List<Integer> winnerIdList) {
		// Default: Ignore.
	}
	
	protected Move fire() {
		int targetW = ThreadLocalRandom.current().nextInt(0, boardW);
		int targetH = ThreadLocalRandom.current().nextInt(0, boardH);
		return new Move(MoveType.FIRE, 0, 0, targetW, targetH);
	}
	
	protected Move spawn() {
		myPosW = ThreadLocalRandom.current().nextInt(0, boardW);
		myPosH = ThreadLocalRandom.current().nextInt(0, boardH);
		return new Move(MoveType.SPAWN, 0, 0, myPosW, myPosH);
	}
}
