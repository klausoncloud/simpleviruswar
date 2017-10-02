package org.klausoncloud.viruswar.model;

import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

import org.klausoncloud.viruswar.resources.Impact;
import org.klausoncloud.viruswar.resources.MoveEntry;

public class GameReport {
	public ArrayList<MoveEntry> moveEntries = new ArrayList<MoveEntry>();

	void addMoveEntry(MoveEntry moveEntry) {
		moveEntries.add(moveEntry);
	}
	
	public void addMoveEntry(ArrayList<MoveResult> moveResultList) {
		for (MoveResult moveResult : moveResultList) {
			addMoveEntryList(moveResult);
		}
	}
	
	// WE cannot move the conversion into MoveEntry as one MoveResult can create several MoveEntries.
	public void addMoveEntryList(MoveResult moveResult) {
		switch (moveResult.getMoveResultType()) {
		case FIREHIT:
			moveEntries.add(new MoveEntry(moveResult.getActorId(), moveResult.getToX(), moveResult.getToY(), Impact.HIT));
			break;
		case FIREMISS:
			moveEntries.add(new MoveEntry(moveResult.getActorId(), moveResult.getToX(), moveResult.getToY(), Impact.MISS));
			break;
		case MOVEHIT:
		case MOVEMISS:
			moveEntries.add(new MoveEntry(moveResult.getActorId(), moveResult.getFromX(), moveResult.getFromY(), Impact.LEAVE));
			moveEntries.add(new MoveEntry(moveResult.getActorId(), moveResult.getToX(), moveResult.getToY(), Impact.SPAWN));
			break;
		case SPAWNHIT:
		case SPAWNMISS:
			moveEntries.add(new MoveEntry(moveResult.getActorId(), moveResult.getToX(), moveResult.getToY(), Impact.SPAWN));
			break;
		case PASS:
			moveEntries.add(new MoveEntry(moveResult.getActorId(), 0, 0, Impact.PASS));
			break;
		default: // Should not happen. Should we throw something?
			break;
		}
		
		if (moveResult.isDestroyed()) {
			moveEntries.add(new MoveEntry(moveResult.getVictimId(), 0, 0, Impact.LOSE));
		}
	}
	
	ArrayList<MoveEntry>getMoveEntries() {
		ArrayList<MoveEntry> result = new ArrayList<MoveEntry>();
		result.addAll(moveEntries);
		return result;
	}
	
	public String toJsonString() {
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		for (MoveEntry moveEntry : moveEntries) {
			jsonArrayBuilder.add(moveEntry.jsonBuilder());
		}
		JsonArray jsonArray = jsonArrayBuilder.build();
		return jsonArray.toString();
	}
}
