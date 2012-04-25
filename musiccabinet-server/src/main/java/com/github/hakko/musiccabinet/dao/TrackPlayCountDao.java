package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.TrackPlayCount;

public interface TrackPlayCountDao {
	
	void createTrackPlayCounts(List<TrackPlayCount> trackPlayCounts);
	List<TrackPlayCount> getTrackPlayCounts();

}
