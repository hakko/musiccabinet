package com.github.hakko.musiccabinet.parser.musicbrainz;

import static java.lang.Boolean.parseBoolean;
import static org.apache.commons.lang.math.NumberUtils.toShort;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.MBArtist;

public class ArtistQueryHandler extends DefaultHandler {
	
	private MBArtist artist = new MBArtist();
	private boolean tagList; // indicates that we're parsing tag-list
	
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_ARTIST = "artist";
	private static final String TAG_NAME = "name";
	private static final String TAG_COUNTRY = "country";
	private static final String TAG_BEGIN = "begin";
	private static final String TAG_ENDED = "ended";
	private static final String TAG_TAGLIST = "tag-list";
	
	private static final String ATTR_ID = "id";
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_ARTIST.equals(qName)) {
			artist.setMbid(attributes.getValue(ATTR_ID));
		} else if (TAG_TAGLIST.equals(qName)) {
			tagList = true;
		}
		characterData = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		String chars = characterData.toString();
		if (TAG_NAME.equals(qName) && !tagList) {
			artist.setName(chars);
		} else if (TAG_COUNTRY.equals(qName)) {
			artist.setCountryCode(chars);
		} else if (TAG_BEGIN.equals(qName)) {
			artist.setStartYear(toShort(chars));
		} else if (TAG_ENDED.equals(qName)) {
			artist.setActive(!parseBoolean(chars));
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public MBArtist getArtist() {
		return artist;
	}

}