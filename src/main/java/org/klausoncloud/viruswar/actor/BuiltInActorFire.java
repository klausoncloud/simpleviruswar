package org.klausoncloud.viruswar.actor;

import org.klausoncloud.viruswar.model.Move;

public class BuiltInActorFire extends BuiltInActor {
	@Override
	public Move nextMove() {
		return super.fire();
	}
}
