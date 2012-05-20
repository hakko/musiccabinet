package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class UserTopArtistsHandler extends DefaultHandler {
	
	private List<Artist> artists = new ArrayList<Artist>();
	
	 // indicates if we're interested in found data, to skip similar artists/tags/bio
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_NAME = "name";
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		characterData = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		if (TAG_NAME.equals(qName)) {
			String chars = characterData.toString();
			artists.add(new Artist(chars));
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public List<Artist> getArtists() {
		return artists;
	}

}