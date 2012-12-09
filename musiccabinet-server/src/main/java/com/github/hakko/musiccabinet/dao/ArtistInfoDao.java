package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;

public interface ArtistInfoDao {

	void createArtistInfo(List<ArtistInfo> artistInfo);
	ArtistInfo getArtistInfo(int artistId);
	ArtistInfo getDetailedArtistInfo(int artistId);
	ArtistInfo getArtistInfo(Artist artist);
	
	void setBioSummary(int artistId, String biosummary);
	
}
