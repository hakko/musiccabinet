package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ArtistTopTracksParserImpl extends AbstractSAXParserImpl implements ArtistTopTracksParser {
	
	private Artist artist;
	private List<Track> topTracks = new ArrayList<Track>();
	
	public ArtistTopTracksParserImpl(InputStream source) throws ApplicationException {
		loadTopTrack(source);
	}

	private void loadTopTrack(InputStream source) 
	throws ApplicationException {
		ArtistTopTracksHandler handler = new ArtistTopTracksHandler();
		parseFromStream(source, handler);
		artist = handler.getArtist();
		topTracks = handler.getTopTracks();
	}
	
	@Override
	public Artist getArtist() {
		return artist;
	}

	@Override
	public List<Track> getTopTracks() {
		return topTracks;
	}
	
}