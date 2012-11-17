package com.github.hakko.musiccabinet.parser.musicbrainz;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.xml.sax.SAXException;

import com.github.hakko.musiccabinet.domain.model.music.MBArtist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ArtistQueryParserImpl extends AbstractSAXParserImpl implements
		ArtistQueryParser {

	private ArtistQueryHandler handler = new ArtistQueryHandler();

	public ArtistQueryParserImpl(InputStream source)
			throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public MBArtist getArtist() {
		return handler.getArtist();
	}

	// the XML returned from MusicBrainz is invalid,
	// attribute ext:score has no declared namespace.
	// turn off namespace validation to make it pass.
	protected synchronized SAXParser getSAXParser()
			throws ParserConfigurationException, SAXException {
		SAXParser saxParser = super.getSAXParser();
		saxParser.getXMLReader().setFeature(
				"http://xml.org/sax/features/namespaces", false);
		return saxParser;
	}

}