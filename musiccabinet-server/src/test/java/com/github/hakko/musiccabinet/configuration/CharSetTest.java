package com.github.hakko.musiccabinet.configuration;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;

public class CharSetTest {

	@Test
	public void invokeUTF8CharSetByReadingString() throws IOException {
		InputStreamReader reader = new InputStreamReader(
				new ByteArrayInputStream("ååå".getBytes(UTF8)), UTF8);
		int c;
		while ((c = reader.read()) != -1) {
			Assert.assertEquals('å', (char) c);
		}
	}
	
}