package org.klausoncloud.viruswar.model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.klausoncloud.viruswar.actor.BuiltInActorFire;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ActorCommunicationManagerTest extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ActorCommunicationManagerTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ActorCommunicationManagerTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testGameNormal()
    {
    	ActorComunicationManager actorManager = new ActorComunicationManager();
    	
    	int width = 10; 
    	int height = 10; 
    	int virusNumber = 3;
    	ArrayList<Player> playerList = createPlayerList(virusNumber);
        
    	Map<Player, Move> result = actorManager.startGame(playerList, width, height, virusNumber);
    	assertTrue(result.size() == virusNumber);
    	for (Entry<Player, Move> entry : result.entrySet()) {
    		assertTrue(entry.getKey() != null);
    		assertTrue(entry.getValue() != null);
    		assertTrue(entry.getValue().getMoveType() == MoveType.SPAWN);
    	}
    	
    	result = actorManager.nextMove(playerList);
    	assertTrue(result.size() == virusNumber);
    	for (Entry<Player, Move> entry : result.entrySet()) {
    		assertTrue(entry.getKey() != null);
    		assertTrue(entry.getValue() != null);
    		assertTrue(entry.getValue().getMoveType() == MoveType.FIRE);
    	}
    }
    
    public void testGameActorDelay()
    {
    	ActorComunicationManager actorManager = new ActorComunicationManager();
    	
    	int width = 10; 
    	int height = 10; 
    	int virusNumber = 3;
    	ArrayList<Player> playerList = createPlayerList(virusNumber-1);
    	playerList.add(new Player(new TestActor(10000), virusNumber-1));
        
    	Map<Player, Move> result = actorManager.startGame(playerList, width, height, virusNumber);
    	assertTrue(result.size() == virusNumber);
    	for (Entry<Player, Move> entry : result.entrySet()) {
    		assertTrue(entry.getKey() != null);
    		assertTrue(entry.getValue() != null);
    		assertTrue(entry.getValue().getMoveType() == MoveType.SPAWN);
    	}
    	
    	result = actorManager.nextMove(playerList);
    	assertTrue(result.size() == virusNumber);
    	for (Entry<Player, Move> entry : result.entrySet()) {
    		Player player = entry.getKey();
    		Move move = entry.getValue();
    		
    		assertTrue(player != null);
    		if (player.getActor().getClass() == TestActor.class) {
    			assertTrue(move == null);
    		} else {
    		    assertTrue(move != null);
    		    assertTrue(move.getMoveType() == MoveType.FIRE);
    		}  		
    	}
    }
    
    private ArrayList<Player> createPlayerList(int size) {
    	ArrayList<Player> playerList = new ArrayList<Player>();
    	for (int i = 0; i < size; i++) {
    	    playerList.add(new Player(new BuiltInActorFire(), i));
    	}
    	
    	return playerList;
    }
}
