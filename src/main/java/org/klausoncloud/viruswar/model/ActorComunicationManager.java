package org.klausoncloud.viruswar.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/*
 * Shields the Umpire from the complexity of making parallel calls to the players.
 * Handles time outs and all other things that can go wrong with player communication.
 */
public class ActorComunicationManager {
	
	static final int MAXROUNDDELAY = 100;
	static final TimeUnit MAXROUNDDELAYTIMEUNIT = TimeUnit.MILLISECONDS;
	
	Map<Player, Move> startGame(ArrayList<Player> playerList, int width, int height, int virusNumber) {
		
		Function<Player, Move> getMove = (Player p)->{ 
			return p.getActor().startGame(width, height, virusNumber, p.getId());
		};
		
		return getAllMoves(playerList, getMove);
	}
	
    Map<Player, Move> nextMove(ArrayList<Player> playerList) {
    	
    	Function<Player, Move> getMove = (Player p)->{ 
			return p.getActor().nextMove();
		};
		
		return getAllMoves(playerList, getMove);
    }
    
    void moveNotification(ArrayList<Player> playerList, List<MoveNotification> moveList) {

    	Function<Player, Move> getMove = (Player p)->{ 
			p.getActor().moveNotification(moveList);
			return null;
		};
		
		getAllMoves(playerList, getMove);
    }
    
	void endOfGame(ArrayList<Player> playerList, List<Integer> winnerIdList) {

		Function<Player, Move> getMove = (Player p)->{ 
			p.getActor().endOfGame(winnerIdList);
			return null;
		};
		
		getAllMoves(playerList, getMove);
	}
	
    private Map<Player, Move> getAllMoves(ArrayList<Player> playerList, Function<Player, Move> moveFunction) {
		
		HashMap<Player, Move> result = new HashMap<Player, Move>();		
		List<Callable<PlayerMove>> tasks = new ArrayList<Callable<PlayerMove>>();
		for (Player player : playerList) {
			tasks.add(()->{
				PlayerMove playerMove = new PlayerMove();
				playerMove.player = player;
				playerMove.move = moveFunction.apply(player);
				return playerMove;
			});
			result.put(player, null);
		}
		
		ExecutorService executor = Executors.newWorkStealingPool();
		try {
			List<Future<PlayerMove>> futures = executor.invokeAll(tasks, 100L, TimeUnit.MILLISECONDS);
			for (Future<PlayerMove> f : futures) {
				if (!f.isCancelled()) {
					PlayerMove pm = f.get();
					result.put(pm.player, pm.move);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}

		return result;
	}
	
	protected class PlayerMove {
		Player player;
		Move move;
	}
}
