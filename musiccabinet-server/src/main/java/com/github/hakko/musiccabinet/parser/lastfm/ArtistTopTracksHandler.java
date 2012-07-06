package com.github.hakko.musiccabinet.parser.lastfm;

import static com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksHandler.State.ARTIST;
import static com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksHandler.State.NAME;
import static com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksHandler.State.TRACK;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class ArtistTopTracksHandler extends DefaultHandler {
	
	private String artistName; // artist name this similarity is based on
	private Artist sourceArtist; // artist based on artist name
	private List<Track> topTracks = new ArrayList<>();
	private Track currentTrack; // used internally while parsing

	private State scope; // stores if we're currently reading a track or an artist
	private State state; // stores state based on latest encountered start tag
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_TOP_TRACKS = "toptracks";
	private static final String TAG_ARTIST = "artist";
	private static final String TAG_TRACK = "track";
	private static final String TAG_NAME = "name";
	
	enum State {
		TRACK, ARTIST, NAME;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_TOP_TRACKS.equals(qName)) {
			artistName = attributes.getValue(TAG_ARTIST);
			sourceArtist = new Artist(artistName);
		} else if (TAG_TRACK.equals(qName)) {
			scope = TRACK;
			currentTrack = new Track();
			currentTrack.setArtist(sourceArtist);
		} else if (TAG_ARTIST.equals(qName)) {
			scope = ARTIST;
		} else if (TAG_NAME.equals(qName)) {
			state = NAME;
			characterData = new StringBuilder();
		} else {
			state = null;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		if (state == NAME && scope == TRACK) {
			String chars = characterData.toString();
			currentTrack.setName(chars);
		}
		
		if (TAG_TRACK.equals(qName)) {
			topTracks.add(currentTrack);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (state == NAME && scope == TRACK) {
			characterData.append(ch, start, length);
		}
	}

	
	/* Variables to be exposured after parsing */

	
	public Artist getArtist() {
		return sourceArtist;
	}

	public List<Track> getTopTracks() {
		return topTracks;
	}

}