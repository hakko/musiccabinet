package com.github.hakko.musiccabinet.domain.model.music;

import static org.apache.commons.lang.StringUtils.upperCase;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/*
 * Represents an album (or more specifically, the earliest encountered release
 * from a release group) from MusicBrainz.
 * 
 */
public class MBAlbum {

	private Artist artist;
	private String title;
	private String mbid;
	private short firstReleaseYear;
	private AlbumType albumType;
	private String format;
	
	public enum AlbumType {
		
		SINGLE ("Single"), EP ("EP"), ALBUM ("Album");

		private final String description;
		
		private AlbumType(String description) {
			this.description = description;
		}
		
		public static AlbumType getAlbumType(String type) {
			String uType = upperCase(type);
			for (AlbumType albumType : values()) {
				if (albumType.name().equals(uType)) {
					return albumType;
				}
			}
			return null;
		}
		
		public String getDescription() {
			return description;
		}
		
	}

	public MBAlbum() {
		
	}
	
	public MBAlbum(String title, String mbid, short releaseYear, String primaryAlbumType) {
		setTitle(title);
		setMbid(mbid);
		setFirstReleaseYear(releaseYear);
		setAlbumType(primaryAlbumType);
	}
	
	public MBAlbum(String artistName, String title, short releaseYear,
			int albumTypeId, String format) {
		setArtist(new Artist(artistName));
		setTitle(title);
		setFirstReleaseYear(releaseYear);
		setAlbumType(albumTypeId);
		setFormat(format);
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
	
	public int getAlbumTypeId() {
		return albumType.ordinal();
	}

	public String getAlbumTypeName() {
		return albumType.getDescription();
	}

	public void setAlbumType(String albumType) {
		this.albumType = AlbumType.valueOf(albumType);
	}

	public void setAlbumType(int albumTypeId) {
		this.albumType = AlbumType.values()[albumTypeId];
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(title)
		.append(mbid)
		.append(firstReleaseYear)
		.append(albumType)
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
		.append(a.albumType, albumType)
		.isEquals();
	}
	
	@Override
	public String toString() {
		return String.format("Album %s (%s, %d), %s", title, albumType, 
				firstReleaseYear, mbid);
	}

}