package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public interface ArtistInfoDao {

	void createArtistInfo(List<ArtistInfo> artistInfo);
	ArtistInfo getArtistInfo(int artistId) throws ApplicationException;
	ArtistInfo getArtistInfo(Artist artist) throws ApplicationException;
	
}
