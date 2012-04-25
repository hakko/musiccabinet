package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Track {

	private Artist artist;
	private String name;
	
	public Track() {
		setArtist(new Artist());
	}
	
	public Track(Artist artist, String trackName) {
		setArtist(artist);
		setName(trackName);
	}
	
	public Track(String artistName, String trackName) {
		setArtist(new Artist(artistName));
		setName(trackName);
	}
	
	public Artist getArtist() {
		return artist;
	}

	public final void setArtist(Artist artist) {
		if (artist == null) {
			throw new IllegalArgumentException("Track artist cannot be set to null.");
		}
		this.artist = artist;
	}
	
	public String getName() {
		return name;
	}
	
	public final void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Track name cannot be set to null.");
		}
		this.name = name;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(artist)
		.append(name)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  Track t = (Track) o;
          return new EqualsBuilder()
          .append(artist, t.artist)
          .append(name, t.name)
          .isEquals();
	}
	
	@Override
	public String toString() {
		return "track " + name + " by " + artist;
	}

}