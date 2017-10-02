package org.klausoncloud.viruswar.model;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class MoveResultTest extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MoveResultTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MoveResultTest.class );
    }
    
    public void testEquals() {
    	MoveResult firstResult = MoveResult.spawnHit(0, 1, 0, 0, false),
    			secondResult = MoveResult.spawnHit(0, 1, 0, 0, false),
    			thirdResult = MoveResult.spawnHit(0, 1, 0, 0, true);
    	assertTrue(firstResult.equals(firstResult));
    	assertTrue(firstResult.equals(secondResult));
    	assertTrue(!firstResult.equals(thirdResult));
    	assertTrue(!secondResult.equals(this));
    }
}
