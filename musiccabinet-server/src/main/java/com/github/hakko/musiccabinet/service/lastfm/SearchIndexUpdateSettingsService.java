package com.github.hakko.musiccabinet.service.lastfm;

/*
 * Exposes common settings that all update services should abide.
 */
public class SearchIndexUpdateSettingsService {

	private boolean onlyUpdateNewArtists = false;

	public boolean isOnlyUpdateNewArtists() {
		return onlyUpdateNewArtists;
	}

	public void setOnlyUpdateNewArtists(boolean onlyUpdateNewArtists) {
		this.onlyUpdateNewArtists = onlyUpdateNewArtists;
	}

}
