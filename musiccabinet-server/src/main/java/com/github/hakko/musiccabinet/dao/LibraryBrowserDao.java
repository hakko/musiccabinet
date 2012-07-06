package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface LibraryBrowserDao {

	List<Artist> getArtists();
	List<Album> getAlbums(int artistId);
	List<Track> getTracks(int albumId);

}