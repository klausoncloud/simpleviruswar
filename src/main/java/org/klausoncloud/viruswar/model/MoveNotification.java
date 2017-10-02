package org.klausoncloud.viruswar.model;

import java.util.concurrent.ThreadLocalRandom;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class MoveNotification {
    private int actorId;              // Who made the move.
    private int victimId = 0;         // Who was impacted, if any. Default = nobody.
    private int posX, posY;           // Target position of the move. E.g. coordinates fired at.
    private int boxWidth = 1, 
    		    boxHeight = 1;        // If the target position is approximate, this denotes the size of the area. Default = precise.
    private boolean isHit = false, 
    		    isDestroyed = false;  // Did the move impact anything?
    MoveType moveType;                // Type of the move made.
    
    MoveNotification(MoveResult moveResult, int boardMaxX, int boardMaxY, int boxSizeX, int boxSizeY) {
    	this.actorId = moveResult.getActorId();
    	switch (moveResult.getMoveResultType()) {
    	case FIREHIT:		
    		this.victimId = moveResult.getVictimId();
    		this.isDestroyed = moveResult.isDestroyed();
    		this.isHit = true;
    	case FIREMISS:
    		this.moveType = MoveType.FIRE;
    		this.posX = moveResult.getToX();
    		this.posY = moveResult.getToY();
    		break;
    	case MOVEHIT:
    		this.victimId = moveResult.getVictimId();
    		this.isDestroyed = moveResult.isDestroyed();
    		this.isHit = true;
    	case MOVEMISS:
    		this.moveType = MoveType.MOVE;
    		setBox(moveResult, boardMaxX, boardMaxY, boxSizeX, boxSizeY);
    		break;
    	case SPAWNHIT:
    		this.victimId = moveResult.getVictimId();
    		this.isDestroyed = moveResult.isDestroyed();
    		this.isHit = true;
    	case SPAWNMISS:
    		this.moveType = MoveType.SPAWN;
    		setBox(moveResult, boardMaxX, boardMaxY, boxSizeX, boxSizeY);
    		break;
    	case PASS:
    		this.moveType = MoveType.PASS;
    		break;
    	case ILLEGALSTART:
    		this.moveType = MoveType.PASS;
    		this.victimId = moveResult.getVictimId();
    		this.isDestroyed = moveResult.isDestroyed();
    	    break;
    	default:
    		assert(false); // This should never happen.
    		break;
    	}
    }
    
    protected void setBox(MoveResult moveResult, int boardSizeX, int bordSizeY, int boxSizeX, int boxSizeY) {
    	this.posX = approxPos(moveResult.getToX(), boardSizeX, boxSizeX);
		this.posY = approxPos(moveResult.getToY(), bordSizeY, boxSizeY);
		this.boxHeight = boxSizeX;
		this.boxWidth = boxSizeY;
    }
    
    // Goal here is to create a box where the actual position is randomly inside of the box.
    // But this box has to be within the board. Problem is the same for X and Y. So one function, called twice.
    protected int approxPos(int pos, int boardSize, int boxSize) {
    	int min = (pos - boxSize) < 0 ? 0 : pos - boxSize + 1;
    	int max = (pos + boxSize) >= boardSize ? boardSize - boxSize : pos;
    	
    	//System.out.println("Pos: " + pos + " Min: " + min + " Max: " + max);
    	if (min == max) {
    		return min;
    	} else {
    		return ThreadLocalRandom.current().nextInt(min, max);
    	}
    	
    }

	public int getActorId() {
		return actorId;
	}

	public int getVictimId() {
		return victimId;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public int getBoxWidth() {
		return boxWidth;
	}

	public int getBoxHeight() {
		return boxHeight;
	}

	public boolean isHit() {
		return isHit;
	}

	public boolean isDestroyed() {
		return isDestroyed;
	}

	public MoveType getMoveType() {
		return moveType;
	}
	
	// For unit tests
	protected MoveNotification(int actorId, int victimId, int posX, int posY, int boxWidth, int boxHeight, boolean isHit,
			boolean isDestroyed, MoveType moveType) {
		super();
		this.actorId = actorId;
		this.victimId = victimId;
		this.posX = posX;
		this.posY = posY;
		this.boxWidth = boxWidth;
		this.boxHeight = boxHeight;
		this.isHit = isHit;
		this.isDestroyed = isDestroyed;
		this.moveType = moveType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actorId;
		result = prime * result + boxHeight;
		result = prime * result + boxWidth;
		result = prime * result + (isDestroyed ? 1231 : 1237);
		result = prime * result + (isHit ? 1231 : 1237);
		result = prime * result + ((moveType == null) ? 0 : moveType.hashCode());
		result = prime * result + posX;
		result = prime * result + posY;
		result = prime * result + victimId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MoveNotification other = (MoveNotification) obj;
		if (actorId != other.actorId)
			return false;
		if (boxHeight != other.boxHeight)
			return false;
		if (boxWidth != other.boxWidth)
			return false;
		if (isDestroyed != other.isDestroyed)
			return false;
		if (isHit != other.isHit)
			return false;
		if (moveType != other.moveType)
			return false;
		if (posX != other.posX)
			return false;
		if (posY != other.posY)
			return false;
		if (victimId != other.victimId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MoveNotification [actorId=" + actorId + ", victimId=" + victimId + ", posX=" + posX + ", posY=" + posY
				+ ", boxWidth=" + boxWidth + ", boxHeight=" + boxHeight + ", isHit=" + isHit + ", isDestroyed="
				+ isDestroyed + ", moveType=" + moveType + "]";
	}
	
	public JsonObjectBuilder jsonBuilder() {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add("actorId", actorId)
			.add("victimId", victimId)
			.add("posX", posX)
			.add("posY", posY)
			.add("boxWidth", boxWidth)
			.add("boxHeight", boxHeight)
			.add("isHit", isHit)
			.add("isDestroyed", isDestroyed)
			.add("moveType", moveType.toString());
		return jsonBuilder;
	}
}
