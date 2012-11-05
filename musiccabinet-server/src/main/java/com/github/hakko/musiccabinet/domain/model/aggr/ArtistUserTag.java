package com.github.hakko.musiccabinet.domain.model.aggr;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class ArtistUserTag {

	private Artist artist;
	private LastFmUser lastFmUser;
	private TagOccurrence tagOccurrence;

	public ArtistUserTag(Artist artist, LastFmUser lastFmUser,
			TagOccurrence tagOccurrence) {
		this.artist = artist;
		this.lastFmUser = lastFmUser;
		this.tagOccurrence = tagOccurrence;
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public LastFmUser getLastFmUser() {
		return lastFmUser;
	}

	public void setLastFmUser(LastFmUser lastFmUser) {
		this.lastFmUser = lastFmUser;
	}

	public TagOccurrence getTagOccurrence() {
		return tagOccurrence;
	}

	public void setTagOccurrence(TagOccurrence tagOccurrence) {
		this.tagOccurrence = tagOccurrence;
	}

	@Override
	public String toString() {
		return String.format("%s=%s (%d), for %s", artist.getName(),
				tagOccurrence.getTag(), tagOccurrence.getOccurrence(),
				lastFmUser.getLastFmUsername());
	}

}