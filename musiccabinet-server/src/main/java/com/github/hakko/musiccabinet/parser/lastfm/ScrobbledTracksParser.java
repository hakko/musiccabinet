package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.TrackPlayCount;

public interface ScrobbledTracksParser {

	List<TrackPlayCount> getTrackPlayCounts();
	short getPage();
	short getTotalPages();
	
}
