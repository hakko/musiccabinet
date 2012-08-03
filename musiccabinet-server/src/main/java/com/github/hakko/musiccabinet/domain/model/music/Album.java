package com.github.hakko.musiccabinet.domain.model.music;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Album {

	private Artist artist;
	private int id;
	private String name;
	private short year;
	private String coverArtPath;
	private boolean coverArtEmbedded;
	private String coverArtURL;
	private List<Integer> trackIds;
	
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
	
	public Album(int artistId, String artistName, int id, String name, short year, String coverArtFile,
			boolean coverArtEmbedded, String coverArtURL, List<Integer> trackIds) {
		this.artist = new Artist(artistId, artistName);
		this.id = id;
		this.name = name;
		this.year = year;
		this.coverArtPath = coverArtFile;
		this.coverArtEmbedded = coverArtEmbedded;
		this.coverArtURL = coverArtURL;
		this.trackIds = trackIds;
	}
	
	public Album(Artist artist, int id, String name) {
		this.artist = artist;
		this.id = id;
		this.name = name;
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
	
	public String getCoverArtPath() {
		return coverArtPath;
	}
	
	public void setCoverArtPath(String coverArtPath) {
		this.coverArtPath = coverArtPath;
	}

	public boolean isCoverArtEmbedded() {
		return coverArtEmbedded;
	}

	public String getCoverArtURL() {
		return coverArtURL;
	}
	
	public void setCoverArtURL(String coverArtURL) {
		this.coverArtURL = coverArtURL;
	}

	public List<Integer> getTrackIds() {
		return trackIds;
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