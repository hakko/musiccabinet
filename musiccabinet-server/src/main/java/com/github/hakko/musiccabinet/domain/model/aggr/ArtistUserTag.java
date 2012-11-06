package com.github.hakko.musiccabinet.domain.model.aggr;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class ArtistUserTag {

	private Artist artist;
	private LastFmUser lastFmUser;
	private String tagName;
	private int tagCount;
	private boolean increase;

	public ArtistUserTag(Artist artist, LastFmUser lastFmUser,
			String tagName, int tagCount, boolean increase) {
		this.artist = artist;
		this.lastFmUser = lastFmUser;
		this.tagName = tagName;
		this.tagCount = tagCount;
		this.increase = increase;
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

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public int getTagCount() {
		return tagCount;
	}

	public void setTagCount(int tagCount) {
		this.tagCount = tagCount;
	}

	public boolean isIncrease() {
		return increase;
	}

	public void setIncrease(boolean increase) {
		this.increase = increase;
	}

	@Override
	public String toString() {
		return String.format("%s=%s (%d), for %s", artist.getName(),
				tagName, tagCount, lastFmUser.getLastFmUsername());
	}

}