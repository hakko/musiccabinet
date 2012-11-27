package com.github.hakko.musiccabinet.parser.musicbrainz;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;

/*
 * Not used (@see ReleaseParser)
 */
public interface ReleaseGroupParser {

	List<MBAlbum> getAlbums();
	int getTotalAlbums();
	
}
