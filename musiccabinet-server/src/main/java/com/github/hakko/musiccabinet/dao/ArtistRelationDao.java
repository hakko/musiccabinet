package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;

public interface ArtistRelationDao {

	void createArtistRelations(Artist sourceArtist, List<ArtistRelation> artistRelations);
	List<ArtistRelation> getArtistRelations(Artist sourceArtist);
	
	List<Artist> getArtistsWithoutRelations();
	
}