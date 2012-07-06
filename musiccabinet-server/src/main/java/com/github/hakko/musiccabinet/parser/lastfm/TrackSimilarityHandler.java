package com.github.hakko.musiccabinet.parser.lastfm;

import static com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityHandler.State.ARTIST;
import static com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityHandler.State.MATCH;
import static com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityHandler.State.NAME;
import static com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityHandler.State.TRACK;
import static org.apache.commons.lang.math.NumberUtils.toFloat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;

public class TrackSimilarityHandler extends DefaultHandler {
	
	private String artistName; // artist name this similarity is based on
	private String trackName; // track name this similarity is based on
	private Track sourceTrack; // track assembled from artist+track name
	private List<TrackRelation> trackRelations = new ArrayList<>();
	private TrackRelation currentTrackRelation; // used internally while parsing

	private State scope; // stores if we're currently reading a track or an artist
	private State state; // stores state based on latest encountered start tag
	private StringBuilder characterData; // used to assemble xml text passed by parser

	// maps start tag to respective state, for tags where we read xml text() data
	private static Map<String, State> xmlToStateMap = new HashMap<>();
	
	private static final String TAG_SIMILAR_TRACKS = "similartracks";
	private static final String TAG_ARTIST = "artist";
	private static final String TAG_TRACK = "track";
	private static final String TAG_NAME = "name";
	private static final String TAG_MATCH = "match";
	
	static {
		xmlToStateMap.put(TAG_NAME, NAME);
		xmlToStateMap.put(TAG_MATCH, MATCH);
	}
	
	enum State {
		TRACK, ARTIST, NAME, MATCH;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_SIMILAR_TRACKS.equals(qName)) {
			artistName = attributes.getValue(TAG_ARTIST);
			trackName = attributes.getValue(TAG_TRACK);
			sourceTrack = new Track(artistName, trackName);
		} else if (TAG_TRACK.equals(qName)) {
			scope = TRACK;
			currentTrackRelation = new TrackRelation();
			currentTrackRelation.setTarget(new Track());
		} else if (TAG_ARTIST.equals(qName)) {
			scope = ARTIST;
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
				if (scope == TRACK) {
					currentTrackRelation.getTarget().setName(chars);
				} else { // ARTIST
					currentTrackRelation.getTarget().getArtist().setName(chars);
				}
			} else if (state == MATCH) {
				currentTrackRelation.setMatch(toFloat(chars));
			}
			state = null;
		}
		
		if (TAG_TRACK.equals(qName)) {
			trackRelations.add(currentTrackRelation);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (state != null) {
			characterData.append(ch, start, length);
		}
	}

	
	/* Variables to be exposured after parsing */

	
	public Track getSourceTrack() {
		return sourceTrack;
	}

	public List<TrackRelation> getTrackRelations() {
		return trackRelations;
	}

}