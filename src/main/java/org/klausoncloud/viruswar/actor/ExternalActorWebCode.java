package org.klausoncloud.viruswar.actor;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class ExternalActorWebCode extends ExternalActorWeb {
	
	final static String SERVICE_PATH_SETCODE = "setCode";
	final static String CODE_PARM = "code";
	
	final static String SERVICE_PATH_ISRUNNING = "isRunning";

	public ExternalActorWebCode(String url) {
		super(url);
	}

	public void setCode(String code) {
		System.out.println("ExternalActorWeb: " + SERVICE_PATH_SETCODE);
		WebTarget myService = service.path(SERVICE_PATH_SETCODE);
    	SyncInvoker myBuilder = myService.request();
    	Response response;
    	try {
    		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
			jsonBuilder.add(CODE_PARM, code);
			String str = jsonBuilder.build().toString();
			System.out.println(str);
    	    response = myBuilder.post(Entity.json(str));
    	    if (response.getStatus() >= 400) {
    	    	System.out.println("Something went wrong with the external player. Responded: " + response.getStatus());
    	    	// Todo: Raise an exception.
    	    }
    	    System.out.println("Done setting code.");
    	    
    	    // Needs to go into finally
    	    response.close();
    	} catch (Exception e) {
    		System.out.println("SetCode - Exception: " + e.getMessage());
    		e.printStackTrace();
    		throw e;
    	}
	}
	
	public boolean isResponding() {
		WebTarget myService = service.path(SERVICE_PATH_ISRUNNING);
    	SyncInvoker myBuilder = myService.request();
    	Response response;
    	try {
    		
    	    response = myBuilder.get();
    	    String message = "Trying";
    	    boolean result = true;
    	    if (response.getStatus() < 400) {
    	    	message = "Actor responded with ok: " + response.getStatus();
    	    	result = true;
    	    } else {
    	        message = "Actor responded with error: " + response.getStatus();
    	        result = false;
    	    }
    	    // Needs to go into finally
    	    response.close();
    	    
    	    System.out.println(message);
    	    return result;
 
    	} catch (Exception e) {
    		System.out.println("IsRunning - Exception: " + e.getMessage());
    		//e.printStackTrace();
    		throw e;
    	}
	}
}
