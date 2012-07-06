package com.github.hakko.musiccabinet.parser.lastfm;

import static com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTagsHandler.State.COUNT;
import static com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTagsHandler.State.NAME;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;

public class ArtistTopTagsHandler extends DefaultHandler {
	
	private String artistName; // artist name this similarity is based on
	private Artist sourceArtist; // artist based on artist name
	private List<Tag> topTags = new ArrayList<>();
	private Tag currentTag; // used internally while parsing

	private State state; // stores state based on latest encountered start tag
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_TOP_TAGS = "toptags";
	private static final String TAG_ARTIST = "artist";
	private static final String TAG_NAME = "name";
	private static final String TAG_COUNT = "count";
	
	enum State {
		NAME, COUNT;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_TOP_TAGS.equals(qName)) {
			artistName = attributes.getValue(TAG_ARTIST);
			sourceArtist = new Artist(artistName);
		} else if (TAG_NAME.equals(qName)) {
			currentTag = new Tag();
			state = NAME;
			characterData = new StringBuilder();
		} else if (TAG_COUNT.equals(qName)) {
			state = COUNT;
			characterData = new StringBuilder();
		} else {
			state = null;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		if (state == NAME) {
			String chars = characterData.toString();
			currentTag.setName(chars);
		} else if (state == COUNT) {
			String chars = characterData.toString();
			currentTag.setCount(Short.parseShort(chars));
			topTags.add(currentTag);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (state != null) {
			characterData.append(ch, start, length);
		}
	}

	
	/* Variables to be exposured after parsing */

	
	public Artist getArtist() {
		return sourceArtist;
	}

	public List<Tag> getTopTags() {
		return topTags;
	}

}