package org.klausoncloud.viruswar.actor;

import org.klausoncloud.viruswar.model.Move;

public class BuiltInActorSpawn extends BuiltInActor {
	@Override
	public Move nextMove() {
		return super.spawn();
	}
}
