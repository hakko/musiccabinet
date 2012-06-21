package com.github.hakko.musiccabinet.parser.lastfm;

import static com.github.hakko.musiccabinet.parser.lastfm.ArtistSimilarityHandler.State.MATCH;
import static com.github.hakko.musiccabinet.parser.lastfm.ArtistSimilarityHandler.State.NAME;
import static org.apache.commons.lang.math.NumberUtils.toFloat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;

public class ArtistSimilarityHandler extends DefaultHandler {
	
	private String artistName; // artist name this similarity is based on
	private List<ArtistRelation> artistRelations = new ArrayList<ArtistRelation>();
	private ArtistRelation currentArtistRelation; // used internally while parsing

	private State state; // stores state based on latest encountered start tag
	private StringBuilder characterData; // used to assemble xml text passed by parser

	// maps start tag to respective state, for tags where we read xml text() data
	private static Map<String, State> xmlToStateMap = new HashMap<String, State>();
	
	private static final String TAG_SIMILAR_ARTISTS = "similarartists";
	private static final String TAG_ARTIST = "artist";
	private static final String TAG_NAME = "name";
	private static final String TAG_MATCH = "match";
	
	static {
		xmlToStateMap.put(TAG_NAME, NAME);
		xmlToStateMap.put(TAG_MATCH, MATCH);
	}
	
	enum State {
		NAME, MATCH;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_SIMILAR_ARTISTS.equals(qName)) {
			artistName = attributes.getValue(TAG_ARTIST);
		} else if (TAG_ARTIST.equals(qName)) {
			currentArtistRelation = new ArtistRelation();
		} else {
			state = xmlToStateMap.get(qName);
			if (state != null) {
				characterData = new StringBuilder();
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		if (state != null) {
			String chars = characterData.toString();
			if (state == NAME) {
				currentArtistRelation.setTarget(new Artist(chars));
			} else if (state == MATCH) {
				currentArtistRelation.setMatch(toFloat(chars));
			}
			state = null;
		}
		
		if (TAG_ARTIST.equals(qName)) {
			artistRelations.add(currentArtistRelation);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (state != null) {
			characterData.append(ch, start, length);
		}
	}

	
	/* Variables to be exposured after parsing */

	
	public String getArtistName() {
		return artistName;
	}

	public List<ArtistRelation> getArtistRelations() {
		return artistRelations;
	}

}