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
import org.klausoncloud.viruswar.model.Move;
import org.klausoncloud.viruswar.model.MoveNotification;
import org.klausoncloud.viruswar.model.MoveType;

public class ExternalActorWeb implements Actor {
	static final int TIMEOUT = 1000; // Milli Sec
	
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
		System.out.println("ExternalActorWeb: startGame");
		WebTarget myService = service.path("startGame")
    			.queryParam("boardX", width)
    			.queryParam("boardY", height)
    			.queryParam("numPlayers", virusNumber)
    			.queryParam("playerId", id);
    	SyncInvoker myBuilder = myService.request().accept(MediaType.TEXT_PLAIN_TYPE);
    	String responseString;
    	try {
    	    responseString = myBuilder.get(String.class);
    	} catch (Exception e) {
    		throw e;
    	}
    	JsonReader reader = Json.createReader(new StringReader(responseString));        
        JsonObject moveObject = reader.readObject();         
        reader.close();
    	
    	return parseMove(moveObject);
	}

	@Override
	public Move nextMove() {
		System.out.println("ExternalActorWeb: nextMove");
        String path = "nextMove";
    	
    	JsonObject moveJson = getMove(path);
    	
    	return parseMove(moveJson);
    }

	@Override
	public void moveNotification(List<MoveNotification> moveList) {
		System.out.println("ExternalActorWeb: moveNotification");
		WebTarget myService = service.path("moveNotification");
    	SyncInvoker myBuilder = myService.request();
    	Response response;
    	try {
    	    response = myBuilder.post(Entity.json(moveListToJSON(moveList)));
    	    if (response.getStatus() >= 400) {
    	    	System.out.println("Something went wrong with the external player. Responded: " + response.getStatus());
    	    }
    	    
    	    // Needs to go into finally
    	    response.close();
    	} catch (Exception e) {
    		System.out.println(e.toString());
    		throw e;
    	}
    	System.out.println("ExternalActorWeb: moveNotification");
	}

	@Override
	public void endOfGame(List<Integer> winnerIdList) {
		System.out.println("ExternalActorWeb: endOfGame");
		WebTarget myService = service.path("endOfGame");
    	SyncInvoker myBuilder = myService.request();
    	Response response;
    	try {
    	    response = myBuilder.post(Entity.json(winnerListToJSON(winnerIdList)));
    	    if (response.getStatus() >= 400) {
    	    	System.out.println("Something went wrong with the external player. Responded: " + response.getStatus());
    	    }
    	    
    	    // Needs to go into finally
    	    response.close();
    	} catch (Exception e) {
    		throw e;
    	}
	}
	
	private JsonObject getMove(String path) {
    	String responseString;
    	try {
    	//responseString = service.path(path).request().accept(MediaType.APPLICATION_JSON).get(String.class);
    	WebTarget myService = service.path(path);
    	SyncInvoker myBuilder = myService.request().accept(MediaType.TEXT_PLAIN_TYPE);
    	responseString = myBuilder.get(String.class);
    	} catch (Exception e) {
    		throw e;
    	}
    	JsonReader reader = Json.createReader(new StringReader(responseString));        
        JsonObject moveObject = reader.readObject();         
        reader.close();
        
        return moveObject;
    }
    
    private Move parseMove(JsonObject moveJson) {
    	// toDo parsing error handling
    	int fromX = moveJson.getInt("fromX");    	
    	int fromY = moveJson.getInt("fromY");
    	int toX = moveJson.getInt("toX");
    	int toY = moveJson.getInt("toY");
    	MoveType moveType = MoveType.valueOf(moveJson.getString("moveType"));
    	return new Move(moveType, fromX, fromY, toX, toY);
    }
    
   private String moveListToJSON(List<MoveNotification>moveList)  {
	   JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		for (MoveNotification move : moveList) {
			jsonArrayBuilder.add(move.jsonBuilder());
		}
		JsonArray jsonArray = jsonArrayBuilder.build();
		return jsonArray.toString();
   }
   
   private String winnerListToJSON(List<Integer> winnerIdList)  {
	   JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		for (Integer id : winnerIdList) {
			JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			jsonBuilder.add("id", id);
			jsonArrayBuilder.add(jsonBuilder);
		}
		JsonArray jsonArray = jsonArrayBuilder.build();
		return jsonArray.toString();
   }
}
