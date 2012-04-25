package com.github.hakko.musiccabinet.parser.lastfm;

import static com.github.hakko.musiccabinet.parser.lastfm.ScrobbledTracksHandler.State.ALBUM;
import static com.github.hakko.musiccabinet.parser.lastfm.ScrobbledTracksHandler.State.ARTIST;
import static com.github.hakko.musiccabinet.parser.lastfm.ScrobbledTracksHandler.State.NAME;
import static com.github.hakko.musiccabinet.parser.lastfm.ScrobbledTracksHandler.State.PLAY_COUNT;
import static com.github.hakko.musiccabinet.parser.lastfm.ScrobbledTracksHandler.State.TRACK;
import static java.lang.Integer.parseInt;
import static java.lang.Short.parseShort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.library.TrackPlayCount;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class ScrobbledTracksHandler extends DefaultHandler {

	private short page;
	private short totalPages;
	private List<TrackPlayCount> trackPlayCounts = new ArrayList<TrackPlayCount>();
	private TrackPlayCount currentTrackPlayCount; // used internally while parsing

	private State scope; // stores if we're currently reading a track or an artist
	private State state; // stores state based on latest encountered start tag
	private StringBuilder characterData; // used to assemble xml text passed by parser

	// maps start tag to respective state, for tags where we read xml text() data
	private static Map<String, State> xmlToStateMap = new HashMap<String, State>();
	
	private static final String TAG_TRACKS = "tracks";
	private static final String ATTR_PAGE = "page";
	private static final String ATTR_TOTAL_PAGES = "totalPages";
	private static final String TAG_ARTIST = "artist";
	private static final String TAG_ALBUM = "album";
	private static final String TAG_TRACK = "track";
	private static final String TAG_NAME = "name";
	private static final String TAG_PLAY_COUNT = "playcount";
	
	static {
		xmlToStateMap.put(TAG_NAME, NAME);
		xmlToStateMap.put(TAG_PLAY_COUNT, PLAY_COUNT);
	}
	
	enum State {
		ARTIST, ALBUM, TRACK, NAME, PLAY_COUNT;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_TRACKS.equals(qName)) {
			page = parseShort(attributes.getValue(ATTR_PAGE));
			totalPages = parseShort(attributes.getValue(ATTR_TOTAL_PAGES));
		} else if (TAG_TRACK.equals(qName)) {
			scope = TRACK;
			currentTrackPlayCount = new TrackPlayCount();
			currentTrackPlayCount.setTrack(new Track());
			currentTrackPlayCount.getTrack().setArtist(new Artist());
		} else if (TAG_ALBUM.equals(qName)) {
			scope = ALBUM;
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
					currentTrackPlayCount.getTrack().setName(chars);
				} else if (scope == ARTIST) {
					currentTrackPlayCount.getTrack().getArtist().setName(chars);
				}
			} else if (state == PLAY_COUNT) {
				currentTrackPlayCount.setPlayCount(parseInt(chars));
			}
			state = null;
		}
		
		if (TAG_TRACK.equals(qName)) {
			trackPlayCounts.add(currentTrackPlayCount);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (state != null) {
			characterData.append(ch, start, length);
		}
	}

	
	/* Variables to be exposured after parsing */

	public short getPage() {
		return page;
	}
	
	public short getTotalPages() {
		return totalPages;
	}
	
	public List<TrackPlayCount> getTrackPlayCounts() {
		return trackPlayCounts;
	}

}