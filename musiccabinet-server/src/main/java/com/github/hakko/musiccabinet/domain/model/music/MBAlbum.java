package com.github.hakko.musiccabinet.domain.model.music;

import static org.apache.commons.lang.StringUtils.upperCase;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/*
 * Represents an album (or more specifically, a release group) from MusicBrainz.
 * 
 * Contains title, mbid, first release year, and primary album type
 * (constant set of values from MusicBrainz).
 */
public class MBAlbum {

	private Artist artist;
	private String title;
	private String mbid;
	private short firstReleaseYear;
	private AlbumType primaryAlbumType;

	public enum AlbumType {
		SINGLE, EP, ALBUM;
		
		public static AlbumType getAlbumType(String type) {
			String uType = upperCase(type);
			for (AlbumType albumType : values()) {
				if (albumType.name().equals(uType)) {
					return albumType;
				}
			}
			return null;
		}
	}

	public MBAlbum() {
		
	}
	
	public MBAlbum(String title, String mbid, short releaseYear, String primaryAlbumType) {
		setTitle(title);
		setMbid(mbid);
		setFirstReleaseYear(releaseYear);
		setPrimaryAlbumType(primaryAlbumType);
	}
	
	public MBAlbum(String artistName, String title, short releaseYear, int albumTypeId) {
		setArtist(new Artist(artistName));
		setTitle(title);
		setFirstReleaseYear(releaseYear);
		primaryAlbumType = AlbumType.values()[albumTypeId];
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public short getFirstReleaseYear() {
		return firstReleaseYear;
	}

	public void setFirstReleaseYear(short firstReleaseYear) {
		this.firstReleaseYear = firstReleaseYear;
	}
	
	public int getPrimaryAlbumTypeId() {
		return primaryAlbumType.ordinal();
	}

	public String getPrimaryAlbumTypeName() {
		return primaryAlbumType.name();
	}

	public void setPrimaryAlbumType(String primaryAlbumType) {
		this.primaryAlbumType = AlbumType.valueOf(primaryAlbumType);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(title)
		.append(mbid)
		.append(firstReleaseYear)
		.append(primaryAlbumType)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		MBAlbum a = (MBAlbum) o;
		return new EqualsBuilder()
		.append(a.title, title)
		.append(a.mbid, mbid)
		.append(a.firstReleaseYear, firstReleaseYear)
		.append(a.primaryAlbumType, primaryAlbumType)
		.isEquals();
	}
	
	@Override
	public String toString() {
		return String.format("Album %s (%s, %d), %s", title, primaryAlbumType, 
				firstReleaseYear, mbid);
	}

}