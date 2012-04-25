package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface ArtistTopTracksParser {

	Artist getArtist();
	List<Track> getTopTracks();
	
}
