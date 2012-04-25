package com.github.hakko.musiccabinet.parser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ctc.wstx.sax.WstxSAXParserFactory;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Contains common functionality for all SAX parsers.
 */
public abstract class AbstractSAXParserImpl {

	private static SAXParserFactory parserFactory = new WstxSAXParserFactory();

	protected void parseFromStream(InputStream source, DefaultHandler handler) 
	throws ApplicationException {
		try {
			SAXParser saxParser = getSAXParser();
			saxParser.parse(source, handler);
		} catch (IOException e) {
			throw new ApplicationException("Could not read data from stream!", e);
		} catch (SAXException e) {
			throw new ApplicationException("Could not parse data from stream!", e);
		} catch (ParserConfigurationException e) {
			throw new ApplicationException("Could not set up SAX parser!", e);
		}
	}
	
	/*
	 * From http://docs.oracle.com/javase/1.4.2/docs/api/javax/xml/parsers/SAXParserFactory.html
	 * 
	 * An implementation of the SAXParserFactory class is NOT guaranteed to be thread safe.
	 * It is up to the user application to make sure about the use of the SAXParserFactory
	 * from more than one thread. Alternatively the application can have one instance of the
	 * SAXParserFactory per thread. An application can use the same instance of the factory
	 * to obtain one or more instances of the SAXParser provided the instance of the factory
	 * isn't being used in more than one thread at a time. 
	 * 
	 * TODO : woodstox parserfactory synchronization?
	 */
	protected synchronized SAXParser getSAXParser() 
	throws ParserConfigurationException, SAXException {
		SAXParser saxParser = parserFactory.newSAXParser();
		saxParser.getXMLReader().setEntityResolver(new EntityResolver() {
			
			@Override
			public InputSource resolveEntity(String arg0, String arg1)
					throws SAXException, IOException {
				return null;
			}
		});
		return saxParser;
	}

}