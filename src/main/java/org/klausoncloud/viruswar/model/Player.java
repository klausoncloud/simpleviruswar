package org.klausoncloud.viruswar.model;

import org.klausoncloud.viruswar.actor.Actor;

public class Player {
	Actor virus;
	int forcedPass;
	int id;
	
	public Player(Actor virus, int id) {
		super();
		this.virus = virus;
		this.id = id;
		this.forcedPass = 0;
	}
	
	public Actor getActor() {
		return virus;
	}
	
	public int getId() {
		return id;
	}
	
	public int getForcedPass() {
		return forcedPass;
	}

	public void setForcedPass(int forcedPass) {
		this.forcedPass = forcedPass;
		assert(forcedPass >= 0);
	}
	
	boolean checkAndDecrementForcePass() {
		if (forcedPass > 0) {
			this.forcedPass--;
			return false;
		} else {
			return true;
		}
	}
}
