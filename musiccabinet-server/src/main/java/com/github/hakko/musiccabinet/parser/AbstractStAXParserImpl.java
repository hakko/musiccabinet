package com.github.hakko.musiccabinet.parser;

import java.io.InputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.ctc.wstx.stax.WstxInputFactory;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Contains common functionality for all StAX parsers.
 */
public abstract class AbstractStAXParserImpl {

	private static XMLInputFactory xmlInputFactory = new WstxInputFactory();

	static {
		xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
	}

	// TODO : woodstox xmlInputFactory synchronization?
	protected synchronized XMLEventReader getXMLEventReader(InputStream source) throws ApplicationException {
		XMLEventReader eventReader;
		try {
			eventReader = xmlInputFactory.createXMLEventReader(source);
		} catch (XMLStreamException e) {
			throw new ApplicationException("Could not read input data!", e);
		}
		return eventReader;
	}

}