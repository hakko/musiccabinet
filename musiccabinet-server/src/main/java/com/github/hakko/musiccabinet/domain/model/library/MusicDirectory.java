package com.github.hakko.musiccabinet.domain.model.library;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class MusicDirectory {

	private String artistName;
	private String albumName;
	private String path;

	public MusicDirectory(String artistName, String path) {
		this(artistName, null, path);
	}

	public MusicDirectory(String artistName, String albumName, String path) {
		this.artistName = artistName;
		this.albumName = albumName;
		this.path = path;
	}

	public String getArtistName() {
		return artistName;
	}
	
	public String getAlbumName() {
		return albumName;
	}

	public String getPath() {
		return path;
	}
	
	public boolean isRoot() {
		return albumName == null;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(new Artist(artistName))
		.append(albumName)
		.append(path)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  MusicDirectory md = (MusicDirectory) o;
          return new EqualsBuilder()
          .append(new Artist(artistName), new Artist(md.artistName))
          .append(albumName, md.albumName)
          .append(path, md.path)
          .isEquals();
	}
	
}