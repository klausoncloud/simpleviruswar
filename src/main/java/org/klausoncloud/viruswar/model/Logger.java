package org.klausoncloud.viruswar.model;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {
	public static final int CRITICAL = 0;
	public static final int ERROR = 2;
	public static final int WARNING = 3;
	public static final int INFO = 4;
	
    public static void logMessage(@SuppressWarnings("rawtypes") Class c, String method, int severity, String message) {
    	System.out.println("In " + c.getName() + "/" + method + ":" + message);
    }
    
    public static void logException(@SuppressWarnings("rawtypes") Class c, String method, int severity, Exception e) {
    	StringWriter sr = new StringWriter();
        PrintWriter writer = new PrintWriter(sr);
        e.printStackTrace(writer);
        writer.flush(); // flush is really optional here, as Writer calls the empty StringWriter.flush
        String st = sr.toString();

        logMessage(c, method, severity, e.getMessage());
    	logMessage(c, method, severity, st);
    }
}
