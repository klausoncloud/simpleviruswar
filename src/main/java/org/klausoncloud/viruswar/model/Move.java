package org.klausoncloud.viruswar.model;

/*
 * Represents one move a virus can make per round. This is the object passed between umpire and virus.
 * There is no identification of the actual virus in this object, as the virus is not allowed to
 * assume another virus' identity.
 */
public class Move {
    MoveType moveType = MoveType.ERROR;
    int fromX = 0, fromY = 0;
    int toX = 0, toY = 0;
    String errorMessage;
    
	public Move(MoveType moveType, int fromX, int fromY, int toX, int toY) {
		this.moveType = moveType;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
	}
	
	public Move(String errorMessage) {
		this.moveType = MoveType.ERROR;
		this.errorMessage = errorMessage;
	}
	
	public static Move failedToMove(String errorMessage) {
		return new Move(errorMessage);
	}

	public MoveType getMoveType() {
		return moveType;
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
}
