package org.klausoncloud.viruswar.actor;

import java.util.concurrent.ThreadLocalRandom;

import org.klausoncloud.viruswar.model.Move;
import org.klausoncloud.viruswar.model.MoveType;

public class BuiltActorMoveFire extends BuiltInActor {

	int moveRate = 5; // Means one in X;
	
	@Override
	public Move nextMove() {
		int decider = ThreadLocalRandom.current().nextInt(0, moveRate);
		if (decider == 0) {
			return move();
		} else {
			return super.fire();
		}
	}

	protected Move move() {
		int oldPosW = myPosW, oldPosH=myPosH;
		myPosW = ThreadLocalRandom.current().nextInt(0, boardW);
		myPosH = ThreadLocalRandom.current().nextInt(0, boardH);
		return new Move(MoveType.MOVE, oldPosW, oldPosH, myPosW, myPosH);
	}
}
