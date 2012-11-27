package com.github.hakko.musiccabinet.parser.musicbrainz;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.MBRelease;

public interface ReleaseParser {

	List<MBRelease> getReleases();
	int getTotalReleases();
	
}
