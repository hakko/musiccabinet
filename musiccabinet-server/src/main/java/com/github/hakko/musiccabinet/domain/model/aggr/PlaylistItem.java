package com.github.hakko.musiccabinet.domain.model.aggr;

public class PlaylistItem {

	private final int artistId;
	private final int trackId;
	
	public PlaylistItem(int artistId, int trackId) {
		this.artistId = artistId;
		this.trackId = trackId;
	}

	public int getArtistId() {
		return artistId;
	}

	public int getTrackId() {
		return trackId;
	}
	
}
