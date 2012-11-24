package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;

public interface MusicBrainzAlbumDao {

	boolean hasDiscography();
	void createAlbums(List<MBAlbum> albums);
	List<MBAlbum> getAlbums(int artistId);
	List<MBAlbum> getMissingAlbums(String artistName, int typeMask, String lastFmUsername, int playedWithinLastDays, int offset);

}