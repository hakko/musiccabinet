package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;

public interface ArtistTopTagsParser {

	Artist getArtist();
	List<Tag> getTopTags();
	
}
