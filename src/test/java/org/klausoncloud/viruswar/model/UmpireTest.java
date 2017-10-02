package org.klausoncloud.viruswar.model;

import java.util.ArrayList;
import java.util.List;

import org.klausoncloud.viruswar.actor.Actor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class UmpireTest  extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UmpireTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( UmpireTest.class );
    }
    
    Umpire testUmpire;
    ArrayList<TestVirusActor> testVirusActorList = new ArrayList<TestVirusActor>();
    final int numViruses = 4;
    int initialPosW[] = {0, 0, 0, 0};
    int initialPosH[] = {0, 1, 2, 3};
    
    @Override
    protected void setUp() throws Exception {
    	for (int i = 0; i < numViruses; i++) {
    		testVirusActorList.add(new TestVirusActor());
    	}
    	ArrayList<Actor> virusList = new ArrayList<Actor>();
    	virusList.addAll(testVirusActorList);
    	testUmpire = new Umpire(40, 20, virusList);
    }
    
    public void testSetup() {
    	assertTrue((initialPosW.length == numViruses) && (initialPosH.length == numViruses));
    }
    
    public void simulateSpawn() {
    	for (int i = 0; i < numViruses; i++) {
    		Move initialMove = new Move(MoveType.SPAWN, 0, 0, initialPosW[0], initialPosH[i]);
    		testUmpire.processMove(initialMove, testUmpire.getVirusStatusList().get(i));
    	}
    }

    /**
     * Rigourous Test :-)
     */
    public void testFiring() {
    	simulateSpawn();
    	Move nextMove = new Move(MoveType.FIRE, 0, 0, 1, 0);
    	MoveResult moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
    	MoveResult expectedMoveResult = new MoveResult(0, 0, 0, 0, 1, 0, MoveResultType.FIREMISS, false);
    	assertTrue(testUmpire.gameNotOver());
    	assertTrue(expectedMoveResult.equals(moveResult));
    	for (Player vs : testUmpire.getVirusStatusList()) {
    		assertTrue(testUmpire.getBoard().isAlive(vs));
    	}
    	assertTrue(testUmpire.gameNotOver());
    	    	
    	for (int i = 0; i < numViruses-2; i++) {
    		nextMove = new Move(MoveType.FIRE, 0, 0, initialPosW[i], initialPosH[i]);
    		moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(numViruses-1));
    		expectedMoveResult = new MoveResult(testUmpire.getVirusStatusList().get(numViruses-1).getId(), 
    				testUmpire.getVirusStatusList().get(i).getId(), 0, 0, initialPosW[i], initialPosH[i], MoveResultType.FIREHIT, true);
    		assertTrue(expectedMoveResult.equals(moveResult));
        	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(i)) == false);
        	assertTrue(testUmpire.gameNotOver());
    	}
    	
    	nextMove = new Move(MoveType.FIRE, 0, 0, initialPosW[numViruses-1], initialPosH[numViruses-1]);
    	moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
    	expectedMoveResult = new MoveResult(testUmpire.getVirusStatusList().get(0).getId(), 
				testUmpire.getVirusStatusList().get(numViruses-1).getId(), 0, 0, initialPosW[numViruses-1], initialPosH[numViruses-1], MoveResultType.FIREHIT, true);
    	assertTrue(expectedMoveResult.equals(moveResult));
    	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(numViruses-1)) == false);
    	assertTrue(!testUmpire.gameNotOver());
    }
    
    public void testSpawn() {
    	simulateSpawn();
    	Move nextMove = new Move(MoveType.SPAWN, 0, 0, 1, 0);
    	MoveResult moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
    	MoveResult expectedMoveResult = new MoveResult(testUmpire.getVirusStatusList().get(0).getId(), 0, 0, 0, 1, 0, MoveResultType.SPAWNMISS, false);
    	assertTrue(expectedMoveResult.equals(moveResult));
    	assertTrue(testUmpire.gameNotOver());
    	for (Player vs : testUmpire.getVirusStatusList()) {
    		assertTrue(testUmpire.getBoard().isAlive(vs));
    	}
    	assertTrue(testUmpire.gameNotOver());
    	
    	for (int i = 1; i < numViruses-1; i++) {
    		nextMove = new Move(MoveType.SPAWN, 0, 0, initialPosW[i], initialPosH[i]);
    		moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
    		expectedMoveResult = new MoveResult(testUmpire.getVirusStatusList().get(0).getId(), 
    				testUmpire.getVirusStatusList().get(i).getId(), 0, 0, initialPosW[i], initialPosH[i], MoveResultType.SPAWNHIT, true);
    		assertTrue(expectedMoveResult.equals(moveResult));
        	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(i)) == false);
        	assertTrue(testUmpire.gameNotOver());
    	}
    	nextMove = new Move(MoveType.SPAWN, 0, 0, initialPosW[numViruses-1], initialPosH[numViruses-1]);
    	moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
    	expectedMoveResult = new MoveResult(testUmpire.getVirusStatusList().get(0).getId(), 
				testUmpire.getVirusStatusList().get(numViruses-1).getId(), 0, 0, initialPosW[numViruses-1], initialPosH[numViruses-1], MoveResultType.SPAWNHIT, true);
    	assertTrue(expectedMoveResult.equals(moveResult));
    	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(numViruses-1)) == false);
    	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(0)) == true);
    	assertTrue(!testUmpire.gameNotOver());
    }
    
    public void testMove() {
    	simulateSpawn();
    	int oldW = 0, newW = 1;
    	int oldH = 0, newH = 0;
    	Move nextMove = new Move(MoveType.MOVE, oldW, oldH, newW, newH);
    	MoveResult moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
    	MoveResult expectedMoveResult = new MoveResult(testUmpire.getVirusStatusList().get(0).getId(), 0, oldW, oldH, newW, newH, MoveResultType.MOVEMISS, false);
    	assertTrue(expectedMoveResult.equals(moveResult));
    	assertTrue(testUmpire.gameNotOver());
    	for (Player vs : testUmpire.getVirusStatusList()) {
    		assertTrue(testUmpire.getBoard().isAlive(vs));
    	}
    	assertTrue(testUmpire.gameNotOver());
    	
    	for (int i = 1; i < numViruses-1; i++) {
    		oldW = newW; newW = initialPosW[i];
    		oldH = newH; newH = initialPosH[i];
    		nextMove = new Move(MoveType.MOVE, oldW, oldH, newW, newH);
    		moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
    		expectedMoveResult = new MoveResult(testUmpire.getVirusStatusList().get(0).getId(), 
    				testUmpire.getVirusStatusList().get(i).getId(), oldW, oldH, newW, newH, MoveResultType.MOVEHIT, true);
    		assertTrue(expectedMoveResult.equals(moveResult));
        	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(i)) == false);
        	assertTrue(testUmpire.gameNotOver());
    	}
    	
    	oldW = newW; newW = initialPosW[numViruses-1];
		oldH = newH; newH = initialPosH[numViruses-1];
		// Killing the third through SPAWN. Then testing to move to my own position.
    	nextMove = new Move(MoveType.SPAWN, 0, 0, newW, newH);
    	moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
    	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(numViruses-1)) == false);
    	nextMove = new Move(MoveType.MOVE, oldW, oldH, newW, newH);
		moveResult = testUmpire.processMove(nextMove, testUmpire.getVirusStatusList().get(0));
		expectedMoveResult = new MoveResult(testUmpire.getVirusStatusList().get(0).getId(), 
				testUmpire.getVirusStatusList().get(0).getId(), oldW, oldH, newW, newH, MoveResultType.MOVEHIT, false);
    	assertTrue(expectedMoveResult.equals(moveResult));
    	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(numViruses-1)) == false);
    	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(0)) == true);
    	assertTrue(!testUmpire.gameNotOver());
    }
    
    public void testValidMove() {
    	Move spawnMove = new Move(MoveType.SPAWN, 0, 0, 0, 0);
    	Move fireMove = new Move(MoveType.FIRE, 0, 0, 0, 0);
    	Move illegalMove = new Move(MoveType.SPAWN, -1, 0, 0, 0);
    	
    	assertTrue(testUmpire.isValidMove(spawnMove));
    	assertTrue(testUmpire.isValidStartMove(spawnMove));
    	assertFalse(testUmpire.isValidStartMove(fireMove));
    	assertFalse(testUmpire.isValidMove(illegalMove));
    }
    
    public void testInvalidMoving() {
    	Move moveMove = new Move(MoveType.MOVE, initialPosW[0+1], initialPosH[0], 1, 1);
    	MoveResult result = testUmpire.processMove(moveMove, testUmpire.getVirusStatusList().get(0));
    	assertTrue(result.getMoveResultType() == MoveResultType.ILLEGAL);
    }
    
    public void testGetAndProcessMoves() {
    	testVirusActorList.get(0).addMove(new Move((MoveType.SPAWN), 0, 0, 0, 0));
    	testVirusActorList.get(1).addMove(new Move((MoveType.SPAWN), 0, 0, 0, 1));
    	testVirusActorList.get(2).addMove(new Move((MoveType.SPAWN), 0, 0, 0, 2));
    	testVirusActorList.get(3).addMove(new Move((MoveType.SPAWN), 0, 0, 0, 3));
    	ArrayList<MoveResult> moveResultList = testUmpire.getAndProcessStartMoves();
    	assertTrue(moveResultList.size() == 4);
    	for (MoveResult moveResult : moveResultList) {
    		assertTrue(MoveResultType.SPAWNMISS == moveResult.getMoveResultType());
    	}
    	testVirusActorList.get(0).addMove(new Move((MoveType.FIRE), 0, 0, 1, 0));
    	testVirusActorList.get(1).addMove(new Move((MoveType.FIRE), 0, 0, 1, 1));
    	testVirusActorList.get(2).addMove(new Move((MoveType.FIRE), 0, 0, 1, 2));
    	testVirusActorList.get(3).addMove(new Move((MoveType.FIRE), 0, 0, 1, 3));
    	moveResultList = testUmpire.getAndProcessMoves();
    	assertTrue(moveResultList.size() == 4);
    	for (MoveResult moveResult : moveResultList) {
    		assertTrue(MoveResultType.FIREMISS == moveResult.getMoveResultType());
    	}
    	testVirusActorList.get(0).addMove(new Move((MoveType.MOVE), 0, 0, 1, 0));
    	testVirusActorList.get(1).addMove(new Move((MoveType.MOVE), 0, 1, 1, 1));
    	testVirusActorList.get(2).addMove(new Move((MoveType.MOVE), 0, 2, 1, 2));
    	testVirusActorList.get(3).addMove(new Move((MoveType.MOVE), 0, 3, 1, 3));
    	moveResultList = testUmpire.getAndProcessMoves();
    	assertTrue(moveResultList.size() == 4);
    	for (MoveResult moveResult : moveResultList) {
    		assertTrue(moveResult.getMoveResultType().toString(), MoveResultType.MOVEMISS == moveResult.getMoveResultType());
    	}	
    	for (int i = 0; i < MoveType.MOVE.cost; i++) {
    		testVirusActorList.get(0).addMove(new Move((MoveType.FIRE), 0, 0, 1, 0));
        	testVirusActorList.get(1).addMove(new Move((MoveType.FIRE), 0, 0, 1, 1));
        	testVirusActorList.get(2).addMove(new Move((MoveType.FIRE), 0, 0, 1, 2));
        	testVirusActorList.get(3).addMove(new Move((MoveType.FIRE), 0, 0, 1, 3));
    		moveResultList = testUmpire.getAndProcessMoves();
        	assertTrue(moveResultList.isEmpty());
        	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(0)));
        	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(1)));
        	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(2)));
        	assertTrue(testUmpire.getBoard().isAlive(testUmpire.getVirusStatusList().get(3)));
    	}
    	testVirusActorList.get(0).addMove(new Move((MoveType.FIRE), 0, 0, 1, 0));
    	testVirusActorList.get(1).addMove(new Move((MoveType.FIRE), 0, 0, 1, 1));
    	testVirusActorList.get(2).addMove(new Move((MoveType.FIRE), 0, 0, 1, 2));
    	testVirusActorList.get(3).addMove(new Move((MoveType.FIRE), 0, 0, 1, 3));
    	moveResultList = testUmpire.getAndProcessMoves();
    	assertTrue(String.valueOf(moveResultList.size()), moveResultList.size() == 3);
    	for (MoveResult moveResult : moveResultList) {
    		assertTrue(MoveResultType.FIREHIT == moveResult.getMoveResultType());
    	}
    	assertTrue(testUmpire.gameIsOver());
    }
    
    public void testRunGame() {
    	final int roundsPlayed = 3;
    	testVirusActorList.get(0).addMove(new Move((MoveType.SPAWN), 0, 0, 0, 0));
    	testVirusActorList.get(1).addMove(new Move((MoveType.SPAWN), 0, 0, 0, 1));
    	testVirusActorList.get(2).addMove(new Move((MoveType.SPAWN), 0, 0, 0, 2));
    	testVirusActorList.get(3).addMove(new Move((MoveType.FIRE), 0, 0, 0, 3)); // Note: dead for not spawning.
 
    	testVirusActorList.get(0).addMove(new Move((MoveType.SPAWN), 0, 0, 1, 0));
    	testVirusActorList.get(1).addMove(new Move((MoveType.FIRE), 0, 0, 1, 1));
    	testVirusActorList.get(2).addMove(new Move((MoveType.FIRE), 0, 0, 1, 2));
    	testVirusActorList.get(3).addMove(new Move((MoveType.FIRE), 0, 0, 1, 3));

    	testVirusActorList.get(0).addMove(new Move((MoveType.SPAWN), 0, 0, 5, 5));
    	testVirusActorList.get(1).addMove(new Move((MoveType.FIRE), 0, 0, 0, 1));
    	testVirusActorList.get(2).addMove(new Move((MoveType.FIRE), 0, 0, 0, 2));
    	testVirusActorList.get(3).addMove(new Move((MoveType.FIRE), 0, 0, 0, 3));
    	
    	testUmpire.runGame();
    	
    	ArrayList<List<MoveNotification>> notificationLists = testVirusActorList.get(0).receivedNotifications();
    	assertTrue(!notificationLists.isEmpty());
    	for (int i = 0; i < roundsPlayed; i++) {
    		assertTrue(!notificationLists.get(i).isEmpty());
    	}

    	//MoveNotification(int actorId, int victimId, int posX, int posY, int boxWidth, int boxHeight, boolean isHit,
		//                 boolean isDestroyed, MoveType moveType)
    	// Player 3 dead for not spawning.
    	MoveNotification expectedNote = new MoveNotification(3, 3, 0, 0, 1, 1, false, true, MoveType.PASS);
    	assertTrue(notificationLists.get(0).contains(expectedNote));
    	for (int i = 1; i < roundsPlayed; i++) {
    		assertTrue(notificationLists.get(1).size() == testVirusActorList.size()-1);
    	}
    	
    	// Player misses
    	expectedNote = new MoveNotification(2, 0, 1, 2, 1, 1, false, false, MoveType.FIRE);
    	assertTrue(notificationLists.get(1).contains(expectedNote));
    	
    	// Player hits itself
    	expectedNote = new MoveNotification(1, 1, 0, 1, 1, 1, true, true, MoveType.FIRE);
    	assertTrue(notificationLists.get(2).contains(expectedNote));
    	
    	MoveNotification foundNote = findFirstSpawn(notificationLists.get(1));
    	assertTrue(foundNote != null);
    }   
    
    MoveNotification findFirstSpawn(List<MoveNotification> notificationList) {
    	for (MoveNotification note : notificationList) {
    		if (note.getMoveType() == MoveType.SPAWN) {
    			return note;
    		}
    	}
    	return null;
    }
    
    void printNotificationList(List<MoveNotification> notificationList) {
    	for (MoveNotification note : notificationList) {
    		System.out.println(note);
    	}
    }
}
