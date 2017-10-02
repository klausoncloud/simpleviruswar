package org.klausoncloud.viruswar.model;

import java.util.ArrayList;
import java.util.List;

import org.klausoncloud.viruswar.actor.Actor;

public class TestVirusActor implements Actor {

	private ArrayList<Move>moveList = new ArrayList<Move>();
	private int moveMarker = 0;
	private ArrayList<List<MoveNotification>> notificationLists = new ArrayList<List<MoveNotification>>();
	List<Integer> winnerlist = null;
	
	public void addMove(Move theMove) {
		moveList.add(theMove);
	}
	
	public ArrayList<List<MoveNotification>> receivedNotifications() {
		return notificationLists;
	}
	
	@Override
	public Move startGame(int width, int height, int virusNumber, int id) {
		moveMarker = 0;
		return nextMove();
	}

	@Override
	public Move nextMove() {
		return moveList.get(moveMarker++);
	}

	@Override
	public void moveNotification(List<MoveNotification> moveList) {
		notificationLists.add(moveList);
	}

	@Override
	public void endOfGame(List<Integer> winnerIdList) {
		this.winnerlist = winnerIdList;	
	}

}
