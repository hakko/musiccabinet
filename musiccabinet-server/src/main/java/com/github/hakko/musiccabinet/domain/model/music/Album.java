package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Album {

	private Artist artist;
	private String name;
	
	public Album() {
		setArtist(new Artist());
	}

	public Album(String albumName) {
		setArtist(new Artist());
		setName(albumName);
	}

	public Album(Artist artist, String albumName) {
		setArtist(artist);
		setName(albumName);
	}
	
	public Album(String artistName, String albumName) {
		setArtist(new Artist(artistName));
		setName(albumName);
	}
	
	public Artist getArtist() {
		return artist;
	}

	public final void setArtist(Artist artist) {
		if (artist == null) {
			throw new IllegalArgumentException("Album artist cannot be set to null.");
		}
		this.artist = artist;
	}
	
	public String getName() {
		return name;
	}
	
	public final void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Album name cannot be set to null.");
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

		  Album t = (Album) o;
          return new EqualsBuilder()
          .append(artist, t.artist)
          .append(name, t.name)
          .isEquals();
	}
	
	@Override
	public String toString() {
		return "album " + name + " by " + artist;
	}

}