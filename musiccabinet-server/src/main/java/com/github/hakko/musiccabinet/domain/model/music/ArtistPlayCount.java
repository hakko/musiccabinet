package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ArtistPlayCount {

	private Artist artist;
	private int playCount;
	
	public ArtistPlayCount(String artistName, int playCount) {
		this.artist = new Artist(artistName);
		this.playCount = playCount;
	}

	public ArtistPlayCount(String artistName) {
		this.artist = new Artist(artistName);
	}
	
	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(artist)
		.append(playCount)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  ArtistPlayCount apc = (ArtistPlayCount) o;
          return new EqualsBuilder()
          .append(artist, apc.artist)
          .append(playCount, apc.playCount)
          .isEquals();
	}

	@Override
	public String toString() {
		return artist + " (" + playCount + ")";
	}

}