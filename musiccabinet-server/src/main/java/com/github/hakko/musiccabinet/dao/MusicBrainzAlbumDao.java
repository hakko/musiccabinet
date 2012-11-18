package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;

public interface MusicBrainzAlbumDao {

	void createAlbums(List<MBAlbum> albums);
	List<MBAlbum> getAlbums(int artistId);
	
}