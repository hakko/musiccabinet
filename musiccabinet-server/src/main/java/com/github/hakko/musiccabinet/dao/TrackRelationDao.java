package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;

public interface TrackRelationDao {

	void createTrackRelations(Track sourceTrack, List<TrackRelation> trackRelations);
	List<TrackRelation> getTrackRelations(Track sourceTrack);
	
}