package org.klausoncloud.viruswar.resources;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class MoveEntry {
	public int player;
	public int posW;
	public int posH;
	public Impact impact;
	
	public MoveEntry(int player, int posW, int posH, Impact impact) {
		super();
		this.player = player;
		this.posW = posW;
		this.posH = posH;
		this.impact = impact;
	}
	
	public int getPlayer() {
		return player;
	}
	public void setPlayer(int player) {
		this.player = player;
	}
	public int getPosW() {
		return posW;
	}
	public void setPosW(int posW) {
		this.posW = posW;
	}
	public int getPosH() {
		return posH;
	}
	public void setPosH(int posH) {
		this.posH = posH;
	}
	public Impact getImpact() {
		return impact;
	}
	public void setImpact(Impact impact) {
		this.impact = impact;
	}
	
	public JsonObjectBuilder jsonBuilder() {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("player", player)
			.add("posW", posW)
			.add("posH", posH)
			.add("impact", impact.getImpactString());
		return jsonBuilder;
	}
}
