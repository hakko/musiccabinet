package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.ArtistPlayCount;

public interface GroupWeeklyArtistChartParser {

	List<ArtistPlayCount> getArtistPlayCount();
	
}
