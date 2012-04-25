package com.github.hakko.musiccabinet.parser;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.configuration.CharSet;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class AbstractSAXParserImplTest {

	/*
	 * Validate that an IOException thrown while parsing gets caught and wrapped
	 * as an ApplicationException.
	 */
	@Test (expected = ApplicationException.class)
	public void handleIOExceptionWhileParsing() throws ApplicationException, IOException {
		new AbstractSAXParserImpl() {}.parseFromStream(
				getFailingInputStream(), getValidDefaultHandler());
	}

	/*
	 * Validate that a SAXException thrown while parsing gets caught and wrapped
	 * as an ApplicationException.
	 */
	@Test (expected = ApplicationException.class)
	public void handleSAXExceptionWhileParsing() throws ApplicationException, UnsupportedEncodingException {
		new AbstractSAXParserImpl() {}.parseFromStream(
				getValidInputStream(), getFailingDefaultHandler());
	}

	/*
	 * Validate that a ParserConfigurationException thrown while creating parser
	 * gets caught and wrapped as an ApplicationException.
	 */
	@Test (expected = ApplicationException.class)
	public void handleParserConfigurationException() throws UnsupportedEncodingException, ApplicationException {
		new AbstractSAXParserImpl() {
			@Override
			protected synchronized SAXParser getSAXParser() 
			throws ParserConfigurationException, SAXException {
				throw new ParserConfigurationException();
			}
		}.parseFromStream(getValidInputStream(), getValidDefaultHandler());
	}
	
	/*
	 * Returns an InputStream that can be parsed as xml.
	 */
	private InputStream getValidInputStream() throws UnsupportedEncodingException {
		return new ByteArrayInputStream(
				"<?xml version=\"1.0\" encoding=\"us-ascii\"?><tag>data</tag>"
				.getBytes(CharSet.US_ASCII));
	}

	/*
	 * Returns an InputStream that throws an IOException when reading from it.
	 */
	@SuppressWarnings("unchecked")
	private InputStream getFailingInputStream() throws IOException {
		InputStream inputStream = mock(InputStream.class);
		when(inputStream.read()).thenThrow(IOException.class);
		return inputStream;
	}

	/*
	 * Returns a DefaultHandler that really doesn't anything.
	 */
	private DefaultHandler getValidDefaultHandler() {
		return new DefaultHandler();
	}
	
	/*
	 * Returns a DefaultHandler that throws a SAXException when parsing anything.
	 */
	private DefaultHandler getFailingDefaultHandler() {
		return new DefaultHandler() {
			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) 
			throws SAXException {
				throw new SAXException();
			}
		};
	}

}