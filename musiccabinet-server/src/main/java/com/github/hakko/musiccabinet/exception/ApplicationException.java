package com.github.hakko.musiccabinet.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

import com.github.hakko.musiccabinet.log.Logger;

public class ApplicationException extends Exception {

	private static final long serialVersionUID = -1625142065190269538L;

	private static final Random rnd = new Random(System.currentTimeMillis());

	private int errorId;
	
	private static final Logger LOG = Logger.getLogger(ApplicationException.class);
	
	public ApplicationException(String message) {
		super(message);
		logError();
	}
	
	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
		logError();
	}
	
	private void logError() {
		errorId = 10000 + rnd.nextInt(1000000 - 10000);
		
		StringBuilder sb = new StringBuilder();
		sb.append("An error with id " + errorId + " has occurred.");
		sb.append(" Error message: " + getMessage());
		if (getCause() != null) {
			StringWriter sw = new StringWriter();
	        getCause().printStackTrace(new PrintWriter(sw));
	        sb.append(" Error stacktrace: " + sw.toString());
		}
		
		LOG.error(sb.toString(), getCause());
	}
	
	public int getErrorId() {
		return errorId;
	}
	
}