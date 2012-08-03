package com.github.hakko.musiccabinet.parser.lastfm;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public class AuthSessionHandler extends DefaultHandler {
	
	private LastFmUser lastFmUser = new LastFmUser();
	
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_NAME = "name";
	private static final String TAG_KEY = "key";
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
		characterData = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String chars = characterData.toString();
		if (TAG_NAME.equals(qName)) {
			lastFmUser.setLastFmUsername(chars);
		} else if (TAG_KEY.equals(qName)) {
			lastFmUser.setSessionKey(chars);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public LastFmUser getLastFmUser() {
		return lastFmUser;
	}

}