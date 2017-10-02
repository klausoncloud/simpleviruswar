package org.klausoncloud.viruswar.model;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GameBoardTest extends TestCase {
	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GameBoardTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GameBoardTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testIsOccupied()
    {
    	int width = 20, height = 10;
    	Player player = new Player(null, 0);
    	ArrayList<Player> players = new ArrayList<Player>();
    	players.add(player);
		Gameboard board = new Gameboard(width, height, players);
		
		board.setPiece(0, 0, player);
		assertTrue( board.isOccupied(0, 0) );
        assertTrue( !board.isOccupied(width-1, height-1) );
        try {
            assertTrue( !board.isOccupied(width, height) );
            assertTrue(false);
        } catch (Throwable e) { }
    }
    
    public void testGetPiece() {
    	int width = 20, height = 10;
    	Player player = new Player(null, 0);
    	ArrayList<Player> players = new ArrayList<Player>();
    	players.add(player);
		Gameboard board = new Gameboard(width, height, players);
		
		board.setPiece(0, 0, player);
		assertTrue( null != board.removePiece(0, 0) );
		
		board.setPiece(0, 0, player);
        try {
            assertTrue( null != board.removePiece(1, 0) );
            assertTrue(false);
        } catch (Throwable e) { }
    }
    
    public void testIsValidPosition() {
    	int width = 20, height = 10;
    	Gameboard board = new Gameboard(width, height, new ArrayList<Player>());
    	
    	assertTrue(board.isValidPosition(0, 0));
    	assertTrue(board.isValidPosition(width-1, height-1));
    	assertTrue(!board.isValidPosition(width, height-1));
    	assertTrue(!board.isValidPosition(width-1, height));
    	assertTrue(!board.isValidPosition(-1, 0));
    	assertTrue(!board.isValidPosition(0, -1));
    }
    
    public void testAddRemovePiece() {
    	int width = 20, height = 10;
    	Player player = new Player(null, 0);
    	ArrayList<Player> players = new ArrayList<Player>();
    	players.add(player);
		Gameboard board = new Gameboard(width, height, players);
    	
    	board.setPiece(0, 0, player);
    	board.setPiece(1, 1, player);
    	assertTrue(board.removePiece(0, 0).equals(player));
    	assertTrue(null == board.getPlayerStatus(0, 0));
    	assertTrue(player.equals(board.getPlayerStatus(1, 1)));
    }
}
