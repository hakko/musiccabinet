package com.github.hakko.musiccabinet.util;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;

import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class StringUtilTest {
	
	@Test
	public void stringUtilReturnsStringAsInputstream() throws ApplicationException, IOException {
		String str = "abcåäö\n{}";
		
		StringUtil stringUtil = new StringUtil(str);
		
		InputStreamReader isr = new InputStreamReader(stringUtil.getInputStream(), UTF8);
		int pos = 0, c;
		while ((c = isr.read()) != -1) {
			Assert.assertEquals(str.charAt(pos++), (char) c); 
		}
	}
	
	/*
	 * TODO : run with PowerMock?
	 * 
	@SuppressWarnings("unchecked")
	@Test (expected = ApplicationException.class)
	public void stringUtilThrowsErrorWhenUTF8IsntSupported() throws ApplicationException, UnsupportedEncodingException {
		String str = Mockito.mock(String.class);
		Mockito.when(str.getBytes(Mockito.any(String.class)))
		.thenThrow(UnsupportedEncodingException.class);
		
		new StringUtil(str);
	}
	*/

}