package com.github.hakko.musiccabinet.domain.model.music;

public class LibraryArtist {

	private final int artistId;
	private final String artistName;
	
	public LibraryArtist(int artistId, String artistName) {
		this.artistId = artistId;
		this.artistName = artistName;
	}

	public int getArtistId() {
		return artistId;
	}

	public String getArtistName() {
		return artistName;
	}
	
}
