package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.domain.model.music.MBRelease;

public interface MusicBrainzAlbumDao {

	boolean hasDiscography();
	void createAlbums(List<MBRelease> releases);
	List<MBAlbum> getAlbums(int artistId);
	List<MBAlbum> getMissingAlbums(String artistName, int typeMask, String lastFmUsername, int playedWithinLastDays, int offset);

}