package org.klausoncloud.viruswar.model;

import org.klausoncloud.viruswar.actor.BuiltInActorFire;

public class TestActor extends BuiltInActorFire {
    int delay = 0;
    
    public TestActor(int delay) {
    	this.delay = delay;
    }
    
    @Override
    public Move nextMove() {
    	try {
			Thread.currentThread().sleep(delay);
			return super.fire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
