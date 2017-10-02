package org.klausoncloud.viruswar.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.klausoncloud.viruswar.actor.Actor;
import org.klausoncloud.viruswar.resources.Impact;
import org.klausoncloud.viruswar.resources.MoveEntry;

/*
 * Runs the game. Main control class of the application.
 */
public class Umpire {	
    private Gameboard board;
    private ActorComunicationManager actorManager;
    final private int boxSizeX = 5;
    final private int boxSizeY = 5;
    
    public Umpire(int boardWidth, int boardHeight, ArrayList<Actor> viruses) {
    	ArrayList<Player> playerList = new ArrayList<Player>(viruses.size());
    	int id = 0;
    	for (Actor virus : viruses) {
    		Player virusStatus = new Player(virus, id++);
    		playerList.add(virusStatus);
    	}
    	this.board = new Gameboard(boardWidth, boardHeight, playerList);
    	actorManager = new ActorComunicationManager();
    }
    
	// Returns a JSON string representing the game.
	public GameReport runGame() {
		final int maxRounds = 500;
		int roundsPlayed = 1;
		GameReport gameReport = new GameReport();
		
		{ // First round, special processing
			ArrayList<MoveResult> movesFirstRound = getAndProcessStartMoves();
			notifyVirusActors(movesFirstRound);
			gameReport.addMoveEntry(movesFirstRound);
		}
		
		// Other rounds
		while (gameNotOver() && (roundsPlayed++ < maxRounds)) {
			ArrayList<MoveResult> movesThisRound = getAndProcessMoves();
			gameReport.addMoveEntry(movesThisRound);
			notifyVirusActors(movesThisRound);
		}
		
		gameReport.addMoveEntry(new MoveEntry(0, 0, 0, Impact.END));
		processWinner(gameReport);
		
		return gameReport;
	}
	
	protected void notifyVirusActors(ArrayList<MoveResult> movesThisRound) {
		ArrayList<MoveNotification> notificationList = new ArrayList<MoveNotification>();
		for (MoveResult moveResult : movesThisRound) {
			notificationList.add(new MoveNotification(moveResult, board.getWidth(), board.getHeight(), boxSizeX, boxSizeY));
		}
		actorManager.moveNotification(board.getPlayerStatusList(), notificationList);
	}
	
	protected ArrayList<MoveResult> getAndProcessStartMoves() {
		ArrayList<MoveResult> movesThisRound = new ArrayList<MoveResult>();
		Map<Player, Move> moveMap = actorManager.startGame(board.getPlayerStatusList(), board.getWidth(), board.getHeight(), board.numberOfPlayers());
		for (Entry<Player, Move> moveEntry : moveMap.entrySet()) {
			if (isValidStartMove(moveEntry.getValue())) {
				movesThisRound.add(processMove(moveEntry.getValue(), moveEntry.getKey()));
			} else {
				// The virus is dead.
				movesThisRound.add(MoveResult.illegalStart(moveEntry.getKey().getId()));
			}
			// No penalty for a spawn in the first round!
			moveEntry.getKey().setForcedPass(0);
		}
		return movesThisRound;
	}
	
	protected ArrayList<MoveResult> getAndProcessMoves() {
		
		ArrayList<Player>playersAllowedToMakeMove = new ArrayList<Player>();
		for (Player virusStatus : board.getPlayerStatusList()) {
			if (board.isAlive(virusStatus) && virusStatus.checkAndDecrementForcePass()) {
				playersAllowedToMakeMove.add(virusStatus);
			}
		}
		
		Map<Player, Move> moveMap = actorManager.nextMove(playersAllowedToMakeMove);
		
		ArrayList<MoveResult> movesThisRound = new ArrayList<MoveResult>();
		for (Entry<Player, Move> moveEntry : moveMap.entrySet()) {
			if (moveEntry.getValue() == null) {
				movesThisRound.add(MoveResult.illegalMove(moveEntry.getKey().getId()));
			} else {
				MoveResult moveResult = processMove(moveEntry.getValue(), moveEntry.getKey());
				movesThisRound.add(moveResult);
			}
			if (gameIsOver()) break;
		}
		return movesThisRound;
	}
	
	protected void processWinner(GameReport gameReport) {
		for (Player virusStatus : board.getPlayerStatusList()) {
			if (board.isAlive(virusStatus)) {
				gameReport.addMoveEntry(new MoveEntry(virusStatus.getId(), 0, 0, Impact.WIN));
			}
		}
	}
    
	protected MoveResult processMove(Move move, Player virusStatus) {
    	switch (move.getMoveType()) {
    	case PASS:
    		return MoveResult.pass(virusStatus.getId());
    	case FIRE:
    		return processFiring(move, virusStatus);
    	case MOVE:
    		return processMoving(move, virusStatus);
    	case SPAWN:
    		return processSpawning(move, virusStatus);
    	default:
    		return null; // This should never happen!
    	}
    }
    
	private MoveResult processMoving(Move move, Player virusStatus) {
		if (isValidMove(move) && occupiesField(move.getFromX(), move.getFromY(), virusStatus)) {
			boolean isHit = detectHit(move);
			Player victim = null;
			if (isHit) {
				victim = board.removePiece(move.getToX(), move.getToY());

			}
			board.removePiece(move.getFromX(), move.getFromY());
			board.setPiece(move.getToX(), move.getToY(), virusStatus);
			virusStatus.setForcedPass(move.getMoveType().getCost());

			if (isHit) {
				return MoveResult.moveHit(virusStatus.getId(), victim.getId(), move.getFromX(), move.getFromY(),
						move.getToX(), move.getToY(), board.isKilled(victim));
			} else {
				return MoveResult.moveMISS(virusStatus.getId(), move.getFromX(), move.getFromY(), move.getToX(),
						move.getToY());
			}
		} else {
			return MoveResult.illegalMove(virusStatus.getId());
		}
	}
	
	protected boolean occupiesField(int posW, int posH, Player virusStatus) {
		if (board.isOccupied(posW, posH)) {
			Player playerStatusFound = board.getPlayerStatus(posW, posH);
			if (playerStatusFound.getId() == virusStatus.getId()) {
				return true;
			}
		}
		
		return false;
	}
	
	private MoveResult processFiring(Move move, Player virusStatus) {
		if (isValidMove(move)) {
			boolean isHit = detectHit(move);
			if (isHit) {
				Player victim = board.removePiece(move.getToX(), move.getToY());
				return MoveResult.fireHit(virusStatus.getId(), victim.getId(), move.getToX(), move.getToY(),
						board.isKilled(victim));
			} else {
				return MoveResult.fireMiss(virusStatus.getId(), move.getToX(), move.getToY());
			}
		} else {
			return MoveResult.illegalMove(virusStatus.getId());
		}
	}

	private MoveResult processSpawning(Move move, Player virusStatus) {
		if (isValidMove(move)) {
			boolean isHit = detectHit(move);
			Player victim = null;
			if (isHit) {
				victim = board.removePiece(move.getToX(), move.getToY());
			}
			board.setPiece(move.getToX(), move.getToY(), virusStatus);
			virusStatus.setForcedPass(move.getMoveType().getCost());

			if (isHit) {
				return MoveResult.spawnHit(virusStatus.getId(), victim.getId(), move.getToX(), move.getToY(),
						board.isKilled(victim));
			} else {
				return MoveResult.spawnMISS(virusStatus.getId(), move.getToX(), move.getToY());
			}
		} else {
			return MoveResult.illegalMove(virusStatus.getId());
		}
	}
    
	protected boolean detectHit(Move move) {
    	if (board.isOccupied(move.toX, move.toY)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
	protected boolean gameNotOver() {
    	int count = 0;
    	for (Player virusStatus : board.getPlayerStatusList()) {
    		if (board.isAlive(virusStatus)) {
    			count++;
    		}
    	}
    	// One left means we got a winner
    	return count > 1;
    }
	
	protected boolean gameIsOver() {
		return !gameNotOver();
	}
	
	protected boolean isValidStartMove(Move move) {
		if (move != null && move.getMoveType() == MoveType.SPAWN 
				&& isValidMove(move)) {
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean isValidMove(Move move) {
		if (board.isValidPosition(move.fromX, move.fromY) && board.isValidPosition(move.toX, move.toY)) {
			return true;
		} else {
			return false;
		}
	}
	
	protected ArrayList<Actor> allAliveViruses() {
		ArrayList<Actor> result = new ArrayList<Actor>();
		for (Player virusStatus : board.getPlayerStatusList()) {
			if (board.isAlive(virusStatus)) {
				result.add(virusStatus.getActor());
			}
		}
		return result;
	}
	
	
	/*
	 * Allow flexibility to change data structures around virus/virusStstus mapping.
	 */
	protected ArrayList<Actor> allViruses() {
		ArrayList<Actor> result = new ArrayList<Actor>();
		for (Player virusStatus : board.getPlayerStatusList()) {
			result.add(virusStatus.getActor());
		}
		return result;
	}
	
	protected Player getVirusStatus(Actor virus) {
		Player found = null;
		for (Player virusStatus : board.getPlayerStatusList()) {
			if (virusStatus.getActor().equals(virus)) {
				found =  virusStatus;
				break;
			}
		}
		assert(found != null); // This should not happen!
		return found;  
	}
	
	// For unit tests only
	protected Gameboard getBoard() {
		return board;
	}
	protected ArrayList<Player> getVirusStatusList() {
		return  board.getPlayerStatusList();
	}
	protected int getBoxSizeX() {
		return boxSizeX;
	}
	protected int getBoxSizeY() {
		return boxSizeY;
	}
}
