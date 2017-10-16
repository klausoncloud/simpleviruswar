package org.klausoncloud.viruswar.actor;

import java.io.StringReader;
import java.net.URI;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.klausoncloud.viruswar.model.Logger;
import org.klausoncloud.viruswar.model.Move;
import org.klausoncloud.viruswar.model.MoveNotification;
import org.klausoncloud.viruswar.model.MoveType;

public class ExternalActorWeb implements Actor {
	static final int TIMEOUT = 1000; // Milli Sec
	
	// Query strings and Json IDs expected by the client
	final static String SERVICE_PATH_STARTGAME = "startGame";
	final static String QUERY_PARM_BOARDX = "boardX";
	final static String QUERY_PARM_BOARDY = "boardY";
	final static String QUERY_PARM_NUMPLAYERS = "numPlayers";
	final static String QUERY_PARM_PLAYERID = "playerId";
	
	final static String SERVICE_PATH_NEXTMOVE = "nextMove";
	
	final static String SERVICE_PATH_MOVENOTIFICATION = "moveNotification";
	final static String MOVE_NOTE_PARM_ACTORID = "actorId";
	final static String MOVE_NOTE_PARM_VICTIMID = "victimId";
	final static String MOVE_NOTE_PARM_POSX = "col";
	final static String MOVE_NOTE_PARM_POSY = "row";
	final static String MOVE_NOTE_PARM_BOXWIDTH = "boxWidth";
	final static String MOVE_NOTE_PARM_BOXHEIGHT = "boxHeight";
	final static String MOVE_NOTE_PARM_ISHIT = "isHit";
	final static String MOVE_NOTE_PARM_ISDESTROYED = "isDestroyed";
	final static String MOVE_NOTE_PARM_MOVETYPE = "moveType";
	final static String MOVE_PARAM_ERROR = "error";
	
	final static String SERVICE_PATH_ENDOFGAME = "endOfGame";
	final static String WINNER_ID = "id";
	
	// Json IDs sent by the client
	final static String MOVE_PARM_FROMX = "fromX";
	final static String MOVE_PARM_FROMY = "fromY";
	final static String MOVE_PARM_TOX = "toX";
	final static String MOVE_PARM_TOY = "toY";
	final static String MOVE_PARM_TYPE = "moveType";
	
	
	ClientConfig config = new ClientConfig();
    Client client = ClientBuilder.newClient(config);
    WebTarget service;
    URI uri;

	public ExternalActorWeb(String url) {
		uri = UriBuilder.fromUri(url).build();
		client.property(ClientProperties.CONNECT_TIMEOUT, TIMEOUT);
		client.property(ClientProperties.READ_TIMEOUT, TIMEOUT);
    	service = client.target(uri);
	}
	
	@Override
	public Move startGame(int width, int height, int virusNumber, int id) {
		Logger.logMessage(this.getClass(), "startGame", Logger.INFO, "ExternalActorWeb: " + SERVICE_PATH_STARTGAME);
		
		WebTarget myService = service.path(SERVICE_PATH_STARTGAME)
    			.queryParam(QUERY_PARM_BOARDX, width)
    			.queryParam(QUERY_PARM_BOARDY, height)
    			.queryParam(QUERY_PARM_NUMPLAYERS, virusNumber)
    			.queryParam(QUERY_PARM_PLAYERID, id);
    	SyncInvoker myBuilder = myService.request().accept(MediaType.TEXT_PLAIN_TYPE);
    	String responseString;

    	responseString = myBuilder.get(String.class);

    	JsonReader reader = Json.createReader(new StringReader(responseString));        
        JsonObject moveObject = reader.readObject();         
        reader.close();
    	
    	return parseMove(moveObject);
	}

	@Override
	public Move nextMove() {
		Logger.logMessage(this.getClass(), "nextMove", Logger.INFO, "ExternalActorWeb: " + SERVICE_PATH_NEXTMOVE);

		String responseString;
		WebTarget myService = service.path(SERVICE_PATH_NEXTMOVE);
		SyncInvoker myBuilder = myService.request().accept(MediaType.TEXT_PLAIN_TYPE);
		responseString = myBuilder.get(String.class);

		JsonReader reader = Json.createReader(new StringReader(responseString));
		JsonObject moveObject = reader.readObject();
		reader.close();

		return parseMove(moveObject);
	}

	@Override
	public void moveNotification(List<MoveNotification> moveList) {
		Logger.logMessage(this.getClass(), "moveNotification", Logger.INFO, "ExternalActorWeb: " + SERVICE_PATH_MOVENOTIFICATION);
		WebTarget myService = service.path(SERVICE_PATH_MOVENOTIFICATION);
    	SyncInvoker myBuilder = myService.request();
    	Response response;
    	try {
    	    response = myBuilder.post(Entity.json(moveListToJSON(moveList)));
    	    if (response.getStatus() >= 400) {
    	    	Logger.logMessage(this.getClass(), "moveNotification", Logger.INFO, "Something went wrong with the external player. Responded: " + response.getStatus());
    	    }
    	    
    	    // Needs to go into finally
    	    response.close();
    	} catch (Exception e) {
    		Logger.logException(this.getClass(), "moveNotification", Logger.WARNING, e);
    		throw e;
    	}
	}

	@Override
	public void endOfGame(List<Integer> winnerIdList) {
		Logger.logMessage(this.getClass(), "endOfGame", Logger.INFO, "ExternalActorWeb: " + SERVICE_PATH_ENDOFGAME);
		WebTarget myService = service.path(SERVICE_PATH_ENDOFGAME);
    	SyncInvoker myBuilder = myService.request();
    	Response response;
    	try {
    	    response = myBuilder.post(Entity.json(winnerListToJSON(winnerIdList)));
    	    if (response.getStatus() >= 400) {
    	    	Logger.logMessage(this.getClass(), "endOfGame", Logger.INFO, "Something went wrong with the external player. Responded: " + response.getStatus());
    	    }
    	    
    	    // Needs to go into finally
    	    response.close();
    	} catch (Exception e) {
    		Logger.logException(this.getClass(), "endOfGame", Logger.WARNING, e);
    		throw e;
    	}
	}
    
    private Move parseMove(JsonObject moveJson) {
    	// toDo parsing error handling
    	MoveType moveType = MoveType.valueOf(moveJson.getString(MOVE_PARM_TYPE));
    	
    	if (moveType == MoveType.ERROR) {
    		String errorMessage = moveJson.getString(MOVE_PARAM_ERROR);
    		return Move.failedToMove(errorMessage);
    	} else {
    		int fromX = moveJson.getInt(MOVE_PARM_FROMX);    	
        	int fromY = moveJson.getInt(MOVE_PARM_FROMY);
        	int toX = moveJson.getInt(MOVE_PARM_TOX);
        	int toY = moveJson.getInt(MOVE_PARM_TOY);
        	
        	return new Move(moveType, fromX, fromY, toX, toY);
    	}
    	
    }
    
   private String moveListToJSON(List<MoveNotification>moveList)  {
	   JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		for (MoveNotification move : moveList) {
			jsonArrayBuilder.add(jsonBuilder(move));
		}
		JsonArray jsonArray = jsonArrayBuilder.build();
		return jsonArray.toString();
   }
   
   private JsonObjectBuilder jsonBuilder(MoveNotification move) {
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder.add(MOVE_NOTE_PARM_ACTORID, move.getActorId())
			.add(MOVE_NOTE_PARM_VICTIMID, move.getVictimId())
			.add(MOVE_NOTE_PARM_POSX, move.getPosX())
			.add(MOVE_NOTE_PARM_POSY, move.getPosY())
			.add(MOVE_NOTE_PARM_BOXWIDTH, move.getBoxWidth())
			.add(MOVE_NOTE_PARM_BOXHEIGHT, move.getBoxHeight())
			.add(MOVE_NOTE_PARM_ISHIT, move.isHit())
			.add(MOVE_NOTE_PARM_ISDESTROYED, move.isDestroyed())
			.add(MOVE_NOTE_PARM_MOVETYPE, move.getMoveType().toString());
		return jsonBuilder;
	}
   
   private String winnerListToJSON(List<Integer> winnerIdList)  {
	   JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		for (Integer id : winnerIdList) {
			JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			jsonBuilder.add(WINNER_ID, id);
			jsonArrayBuilder.add(jsonBuilder);
		}
		JsonArray jsonArray = jsonArrayBuilder.build();
		return jsonArray.toString();
   }
}
