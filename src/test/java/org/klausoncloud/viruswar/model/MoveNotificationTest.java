package org.klausoncloud.viruswar.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MoveNotificationTest extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MoveNotificationTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MoveNotificationTest.class );
    }
    
    public void testBoxApproximation() {
    	final int boxSize = 5;
    	final int boardSize = 20;
    	MoveNotification note = new MoveNotification(0, 0, 0, 0, 1, 1, false, false, MoveType.PASS);
    	// approxPos(int pos, int boardMax, int boxSize)
    	
    	for (int i = 0; i < boardSize; i++) {
    		note.approxPos(i, boardSize, boxSize);
    	}
    	
    	// On the low line
    	int posX = 0;
    	int a = note.approxPos(0, boardSize, boxSize);
    	assertTrue(a == 0);
    	
    	// Close to the low line
    	posX = boxSize/2;
    	a = note.approxPos(posX, boardSize, boxSize);
    	assertTrue(a >= 0);
    	assertTrue(a <= posX);
    	
    	// Somewhere in the middle
    	posX = boardSize/2;
    	a = note.approxPos(posX, boardSize, boxSize);
    	assertTrue(a > posX - boxSize);
    	assertTrue(a < posX + boxSize);
    	
    	// On the high line
    	posX = boardSize-1;
    	a = note.approxPos(boardSize-1, boardSize, boxSize);
    	assertTrue(a == boardSize-boxSize);
    	
    	// Close to the high line
    	posX = boardSize - boxSize/2;
    	a = note.approxPos(boardSize-boxSize/2, boardSize, boxSize);
    	assertTrue(a <= boardSize-boxSize);
    	assertTrue(a > posX - boxSize);
    }
}
