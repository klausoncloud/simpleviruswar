package org.klausoncloud.viruswar.model;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Implements the gameboard. To be accessed only by the umpire.
 * Keeps status of the game.
 */
public class Gameboard {
    private Piece[][] theBoard;
    private ArrayList<Player> playerList = new ArrayList<Player>();
    private HashMap<Player, ArrayList<Piece>> piecesPerPlayer = new HashMap<Player, ArrayList<Piece>>();
    
	public Gameboard(int boardWidth, int boardHeight, ArrayList<Player> playerList) {
		super();
		this.theBoard = new Piece[boardWidth][boardHeight];
		this.playerList.addAll(playerList);
		for (Player player : playerList) {
			piecesPerPlayer.put(player,new ArrayList<Piece>());
		}
	}
    
	public int getWidth() {
		return theBoard.length;
	}
	
	public int getHeight() {
		return theBoard[0].length;
	}
	
	public boolean isOccupied(int posW, int posH) {
		if (isValidPosition(posW, posH)) {
			return null != theBoard[posW][posH];
		} else {
			return false;
		}
	}
	
	public boolean isKilled(Player player) {
		return piecesPerPlayer.get(player).isEmpty();
	}
	
	public boolean isAlive(Player player) {
		return !isKilled(player);
	}
	
	public ArrayList<Player> getPlayerStatusList() {
		ArrayList<Player> result = new ArrayList<Player>();
		result.addAll(playerList);
		return result;
	}
	
	public int numberOfPlayers() {
		return playerList.size();
	}
	
	void setPiece(int posW, int posH, Player player) {
		Piece piece = new Piece(player);
		theBoard[posW][posH] = piece;
		piecesPerPlayer.get(piece.getVirusStatus()).add(piece);
	}
	
	Player removePiece(int posW, int posH) {
		Piece piece = theBoard[posW][posH];
		theBoard[posW][posH] = null;
		piecesPerPlayer.get(piece.getVirusStatus()).remove(piece);
		return piece.getVirusStatus();
	}
	
	Player getPlayerStatus(int posW, int posH) {
		if (theBoard[posW][posH] != null) {
		    return theBoard[posW][posH].getVirusStatus();
		} else {
			return null;
		}
	}

	boolean isValidPosition(int posW, int posH) {
		return (posW >= 0 && posW < theBoard.length && posH >= 0 && posH < theBoard[0].length);
	}
	
	class Piece {
	    Player playerStatus;

		public Piece(Player playerStatus) {
			super();
			this.playerStatus = playerStatus;
		}

		public Player getVirusStatus() {
			return playerStatus;
		}
	}
}
