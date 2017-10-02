package org.klausoncloud.viruswar.model;

public class MoveResult {
	private int actorId, victimId;
	private int fromX, fromY;
	private int toX, toY;
	private MoveResultType moveResultType;
	private boolean isDestroyed = false;
	
	public MoveResult(int actorId, int victimId, int fromX, int fromY, int toX, int toY, MoveResultType moveResultType,
			boolean isDestroyed) {
		super();
		this.actorId = actorId;
		this.victimId = victimId;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
		this.moveResultType = moveResultType;
		this.isDestroyed = isDestroyed;
	}
	
	static MoveResult pass(int actorId) {
		return new MoveResult(actorId, 0, 0, 0, 0, 0, MoveResultType.PASS, false);
	}
	
	static MoveResult fireHit(int actorId, int victimId, int toX, int toY, boolean isDestroyed) {
		return new MoveResult(actorId, victimId, 0, 0, toX, toY, MoveResultType.FIREHIT, isDestroyed);
	}
	
	static MoveResult fireMiss(int actorId, int toX, int toY) {
		return new MoveResult(actorId, 0, 0, 0, toX, toY, MoveResultType.FIREMISS, false);
	}
	
	static MoveResult moveHit(int actorId, int victimId, int fromX, int fromY, int toX, int toY, boolean isDestroyed) {
		return new MoveResult(actorId, victimId, fromX, fromY, toX, toY, MoveResultType.MOVEHIT, isDestroyed);
	}
	
	static MoveResult moveMISS(int actorId, int fromX, int fromY, int toX, int toY) {
		return new MoveResult(actorId, 0, fromX, fromY, toX, toY, MoveResultType.MOVEMISS, false);
	}
	
	static MoveResult spawnHit(int actorId, int victimId, int toX, int toY, boolean isDestroyed) {
		return new MoveResult(actorId, victimId, 0, 0, toX, toY, MoveResultType.SPAWNHIT, isDestroyed);
	}
	
	static MoveResult spawnMISS(int actorId, int toX, int toY) {
		return new MoveResult(actorId, 0, 0, 0, toX, toY, MoveResultType.SPAWNMISS, false);
	}
	
	static MoveResult illegalStart(int actorId) {
		return new MoveResult(actorId, actorId, 0, 0, 0, 0, MoveResultType.ILLEGALSTART, true);
	}
	
	static MoveResult illegalMove(int actorId) {
		return new MoveResult(actorId, 0, 0, 0, 0, 0, MoveResultType.ILLEGAL, true);
	}

	public int getActorId() {
		return actorId;
	}

	public int getVictimId() {
		return victimId;
	}

	public int getFromX() {
		return fromX;
	}

	public int getFromY() {
		return fromY;
	}

	public int getToX() {
		return toX;
	}

	public int getToY() {
		return toY;
	}

	public MoveResultType getMoveResultType() {
		return moveResultType;
	}

	public boolean isDestroyed() {
		return isDestroyed;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actorId;
		result = prime * result + fromX;
		result = prime * result + fromY;
		result = prime * result + (isDestroyed ? 1231 : 1237);
		result = prime * result + ((moveResultType == null) ? 0 : moveResultType.hashCode());
		result = prime * result + toX;
		result = prime * result + toY;
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
		MoveResult other = (MoveResult) obj;
		if (actorId != other.actorId)
			return false;
		if (fromX != other.fromX)
			return false;
		if (fromY != other.fromY)
			return false;
		if (isDestroyed != other.isDestroyed)
			return false;
		if (moveResultType != other.moveResultType)
			return false;
		if (toX != other.toX)
			return false;
		if (toY != other.toY)
			return false;
		if (victimId != other.victimId)
			return false;
		return true;
	}
	
	
}
