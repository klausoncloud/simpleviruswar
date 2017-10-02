package org.klausoncloud.viruswar.model;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.klausoncloud.viruswar.actor.Actor;

public class PlayerTester {
	Actor virus;
	
    public PlayerTester(Actor virus) {
    	this.virus = virus;
    };
    
    public TestResult testVirus() {
    	TestResult result = new TestResult();
    	
    	String progress = "";
    	try {
    		progress = "Checking startGame...";
    		Move move = virus.startGame(40, 20, 2, 0);
    		
    		if (move.moveType != MoveType.SPAWN) {
    			progress += "failed... did not SPAWN.";
    			
    			result.setMessage(progress);
    	    	result.setPassed(false);
    	    	
    	    	return result;
    		} else {
    			progress += "passed...";
    		}
    			
    		List<MoveNotification> moveList = new ArrayList<MoveNotification>();
    		moveList.add( 
    				new MoveNotification( 
    				    MoveResult.spawnMISS(0, move.toX, move.toY), 40, 20, 10, 10));
    		if (move.toX == 0) {
    			moveList.add( 
        				new MoveNotification( 
        				    MoveResult.spawnMISS(1, 1, 0), 40, 20, 10, 10));
    		} else {
    			moveList.add( 
        				new MoveNotification( 
        				    MoveResult.spawnMISS(1, 0, 0), 40, 20, 10, 10));
    		}
    		
    		progress += "Sending move notifications...";
    		virus.moveNotification(moveList);
    		progress += "passed...";

    		progress += "Requesting next move...";
    		move = virus.nextMove();
    		progress += "passed...";

    		progress += "Communicating winner...";
    		List<Integer> winnerIdList = new ArrayList<Integer>();
    		winnerIdList.add(0);
    		virus.endOfGame(winnerIdList);
    		progress += "passed...";
    		
    	} catch (Exception e) {
			progress += "=> Exception " + e.getMessage();
			
			result.setMessage(progress);
	    	result.setPassed(false);
	    	
	    	return result;
    	}
    	
    	result.setMessage(progress);
    	result.setPassed(true);
    	
    	return result;
    }
    
    public class TestResult {
    	boolean passed = true;
    	String message = "";
    	
		public boolean isPassed() {
			return passed;
		}
		public void setPassed(boolean passed) {
			this.passed = passed;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		
		public String toJsonString() {
			JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			jsonBuilder.add("passed", passed)
				.add("message", message);
			return jsonBuilder.build().toString();
		}
    }
}
