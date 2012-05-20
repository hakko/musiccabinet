package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;

public interface UserTopArtistsParser {

	List<Artist> getArtists();
	
}
