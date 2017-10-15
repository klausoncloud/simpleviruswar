package org.klausoncloud.viruswar.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GameReportTest  extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GameReportTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GameReportTest.class );
    }
    
    public void testCreateMoveEntry() {
    	// Miss
    	assertTrue(testConversion(MoveResult.fireMiss(0, 0, 0),
    			"[{\"player\":0,\"posW\":0,\"posH\":0,\"impact\":\"miss\"}]"));
    	// Hit, not killed
    	assertTrue(testConversion(MoveResult.fireHit(0, 1, 10, 15, false),
    			"[{\"player\":0,\"posW\":10,\"posH\":15,\"impact\":\"hit\"}]"));
    	// Hit, killed
    	assertTrue(testConversion(MoveResult.fireHit(0, 1, 10, 15, true),
    			"[{\"player\":0,\"posW\":10,\"posH\":15,\"impact\":\"hit\"}"
    			+ ",{\"player\":1,\"posW\":0,\"posH\":0,\"impact\":\"lose\"}]"));
    	// Move, empty space
    	assertTrue(testConversion(MoveResult.moveMISS(0, 0, 0, 1, 1),
    			"[{\"player\":0,\"posW\":0,\"posH\":0,\"impact\":\"exit\"}"
    			+ ",{\"player\":0,\"posW\":1,\"posH\":1,\"impact\":\"enter\"}]"));
    	// Move, occupied, not killed
    	assertTrue(testConversion(MoveResult.moveHit(0, 1, 0, 0, 1, 1, false),
    			"[{\"player\":0,\"posW\":0,\"posH\":0,\"impact\":\"exit\"}"
    			+ ",{\"player\":0,\"posW\":1,\"posH\":1,\"impact\":\"enter\"}]"));
    	// Move, occupied, killed
    	assertTrue(testConversion(MoveResult.moveHit(0, 1, 0, 0, 1, 1, true),
    			"[{\"player\":0,\"posW\":0,\"posH\":0,\"impact\":\"exit\"}"
    			+ ",{\"player\":0,\"posW\":1,\"posH\":1,\"impact\":\"enter\"}"
    			+ ",{\"player\":1,\"posW\":0,\"posH\":0,\"impact\":\"lose\"}]"));
    	// Spawn, empty space
    	assertTrue(testConversion(MoveResult.spawnMISS(0, 0, 0),
    			"[{\"player\":0,\"posW\":0,\"posH\":0,\"impact\":\"enter\"}]"));
    	// Spawn, occupied, not killed
    	assertTrue(testConversion(MoveResult.spawnHit(0, 1, 1, 1, false),
    			"[{\"player\":0,\"posW\":1,\"posH\":1,\"impact\":\"enter\"}]"));
    	// Spawn, occupied, killed
    	assertTrue(testConversion(MoveResult.spawnHit(0, 1, 1, 1, true),
    			"[{\"player\":0,\"posW\":1,\"posH\":1,\"impact\":\"enter\"}"
    			+ ",{\"player\":1,\"posW\":0,\"posH\":0,\"impact\":\"lose\"}]"));
    	// Pass
    	assertTrue(testConversion(MoveResult.pass(0),
    			"[{\"player\":0,\"posW\":0,\"posH\":0,\"impact\":\"pass\"}]"));
    	// Illegal start
    	assertTrue(testConversion(MoveResult.illegalStart(0),
    			"[{\"player\":0,\"posW\":0,\"posH\":0,\"impact\":\"lose\"}]"));
    	
    }
    
    private boolean testConversion(MoveResult moveResult, String expected) {
    	GameReport gameReport = new GameReport();
    	gameReport.addMoveResult(moveResult);
    	if (expected.equals(gameReport.toJsonString())) {
    		return true;
    	} else {
    		System.out.println("Expected:  " + expected);
    		System.out.println("Converted: " + gameReport.toJsonString());
        	return false;
    	}
    	
    }
}
