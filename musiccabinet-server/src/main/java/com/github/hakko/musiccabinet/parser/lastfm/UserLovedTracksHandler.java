package com.github.hakko.musiccabinet.parser.lastfm;

import static org.apache.commons.lang.math.NumberUtils.toShort;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.Track;

public class UserLovedTracksHandler extends DefaultHandler {

	private short page;
	private short totalPages;
	private List<Track> lovedTracks = new ArrayList<>();
	private String currentTrackName; // used internally while parsing

	private boolean stateArtist;
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_LOVED_TRACKS = "lovedtracks";
	private static final String TAG_TRACK = "track";
	private static final String TAG_ARTIST = "artist";
	private static final String TAG_NAME = "name";
	private static final String ATTR_PAGE = "page";
	private static final String ATTR_TOTAL_PAGES = "totalPages";
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_LOVED_TRACKS.equals(qName)) {
			page = toShort(attributes.getValue(ATTR_PAGE));
			totalPages = toShort(attributes.getValue(ATTR_TOTAL_PAGES));
		} else if (TAG_TRACK.equals(qName)) {
			stateArtist = false;
		} else if (TAG_ARTIST.equals(qName)) {
			stateArtist = true;
		}
		characterData = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		if (TAG_NAME.equals(qName)) {
			String chars = characterData.toString();
			if (stateArtist) {
				lovedTracks.add(new Track(chars, currentTrackName));
			} else {
				currentTrackName = chars;
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}
	
	/* Variables to be exposured after parsing */

	public short getPage() {
		return page;
	}
	
	public short getTotalPages() {
		return totalPages;
	}
	
	public List<Track> getLovedTracks() {
		return lovedTracks;
	}

}