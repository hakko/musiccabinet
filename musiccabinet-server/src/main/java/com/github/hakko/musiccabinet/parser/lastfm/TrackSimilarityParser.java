package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;

public interface TrackSimilarityParser {

	/* Source track which similarities are based on. */
	Track getTrack();
	
	/* List of related tracks, with a weight. */
	List<TrackRelation> getTrackRelations();
	
}
