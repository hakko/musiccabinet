package com.github.hakko.musiccabinet.util;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;
import static java.lang.Thread.currentThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
 * Utility class for reading content from bundled resources.
 */
public class ResourceUtil implements AutoCloseable {

	private InputStream inputStream;
	private String charSet;

	/*
	 * Read content from a bundled file, using default charset (UTF-8).
	 */
	public ResourceUtil(String uri) {
		this(uri, UTF8);
	}

	/*
	 * Read content from a bundled file, using a specified charset.
	 */
	public ResourceUtil(String uri, String charSet) {
		this.inputStream = currentThread().getContextClassLoader().getResourceAsStream(uri);
		if (inputStream == null) {
			throw new IllegalArgumentException(uri + " could not be found on classpath!");
		}
		this.charSet = charSet;
	}

	/*
	 * Returns an inputstream to bundled file.
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/*
	 * Set (override) inputstream to bundled file.
	 */
	protected void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	
	/*
	 * Read file content from a file located on classpath, using a specified charset.
	 * 
	 * @throws IllegalArgumentException if resource is not found/can not be read
	 */
	public String getContent() throws IllegalArgumentException {		
		String resourceContent;
		try (ResourceUtil resourceUtil = this) {
			resourceContent = getContentFromInputStream();
		} catch (IOException e) {
			throw new IllegalArgumentException(
					"Resource can not be read using charset" + charSet + "!");
		}
		return resourceContent; 
	}

	/*
	 * Read content from an input stream and return as a string.
	 * 
	 * @throws IOException if reading fails.
	 */
	protected String getContentFromInputStream() throws IOException  {
		StringBuilder sb = new StringBuilder();
		int BUF_SIZE = 1024, charsRead;
		char[] chunk = new char[BUF_SIZE];
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(inputStream, charSet));
		while ((charsRead = reader.read(chunk, 0, BUF_SIZE)) > 0) {
			sb.append(chunk, 0, charsRead);
		}
		return sb.toString();
	}
	
	public void close() throws IOException {
		this.inputStream.close();
	}
	
}