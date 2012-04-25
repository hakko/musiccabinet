package com.github.hakko.musiccabinet.exception;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

public class ApplicationExceptionTest {

	private static final String ERROR_MESSAGE = "An expected error has occurred.";
	
	@Test
	public void catchAnExceptionAndBundleItNicely() {
		Exception originalException = null;
		ApplicationException applicationException = null;
		try {
			(new int[1])[0] = (new int[0])[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			applicationException = new ApplicationException(ERROR_MESSAGE, 
					originalException = e);
		}
		
		assertNotNull(originalException);
		
		assertNotNull(applicationException);
		assertTrue(applicationException.getErrorId() > 0);
		assertTrue(applicationException.getMessage().equals(ERROR_MESSAGE));
		assertTrue(applicationException.getCause().equals(originalException));
	}
	
}