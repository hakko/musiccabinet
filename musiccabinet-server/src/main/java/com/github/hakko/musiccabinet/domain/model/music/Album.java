package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Album {

	private Artist artist;
	private int id;
	private String name;
	private short year;
	private String coverArtFile;
	private String coverArtEmbeddedFile;
	private String coverArtURL;
	
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
	
	public Album(int id, String name, short year, String coverArtFile, 
			String coverArtEmbeddedFile, String coverArtURL) {
		this.id = id;
		this.name = name;
		this.year = year;
		this.coverArtFile = coverArtFile;
		this.coverArtEmbeddedFile = coverArtEmbeddedFile;
		this.coverArtURL = coverArtURL;
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
	
	public int getId() {
		return id;
	}
	
	public short getYear() {
		return year;
	}
	
	public String getCoverArtFile() {
		return coverArtFile;
	}

	public String getCoverArtEmbeddedFile() {
		return coverArtEmbeddedFile;
	}

	public String getCoverArtURL() {
		return coverArtURL;
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