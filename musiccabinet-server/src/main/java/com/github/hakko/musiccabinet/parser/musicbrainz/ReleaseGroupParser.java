package com.github.hakko.musiccabinet.parser.musicbrainz;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;

public interface ReleaseGroupParser {

	List<MBAlbum> getAlbums();
	
}
