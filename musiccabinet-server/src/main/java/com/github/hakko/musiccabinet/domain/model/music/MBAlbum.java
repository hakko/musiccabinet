package com.github.hakko.musiccabinet.domain.model.music;

import static org.apache.commons.lang.StringUtils.upperCase;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/*
 * Album representation from MusicBrainz.
 * 
 * Contains title, mbid, first release year, and primary/secondary album type
 * (constant set of values from MusicBrainz).
 */
public class MBAlbum {

	private Artist artist;
	private String title;
	private String mbid;
	private short releaseYear;
	private AlbumType primaryAlbumType;
	private AlbumType secondaryAlbumType;

	private enum AlbumType {
		SINGLE, EP, ALBUM, COMPILATION, SOUNDTRACK, SPOKENWORD, 
		INTERVIEW, AUDIOBOOK, LIVE, REMIX, OTHER
	}

	public MBAlbum() {
		
	}
	
	public MBAlbum(String title, String mbid, short releaseYear, String primaryAlbumType) {
		setTitle(title);
		setMbid(mbid);
		setReleaseYear(releaseYear);
		setPrimaryAlbumType(primaryAlbumType);
	}
	
	public MBAlbum(String artistName, String title, short releaseYear, int albumTypeId) {
		setArtist(new Artist(artistName));
		setTitle(title);
		setReleaseYear(releaseYear);
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

	public short getReleaseYear() {
		return releaseYear;
	}

	public void setReleaseYear(short releaseYear) {
		this.releaseYear = releaseYear;
	}

	public int getPrimaryAlbumTypeId() {
		return primaryAlbumType.ordinal();
	}

	public String getPrimaryAlbumTypeName() {
		return primaryAlbumType.name();
	}

	public void setPrimaryAlbumType(String primaryAlbumType) {
		this.primaryAlbumType = AlbumType.valueOf(upperCase(primaryAlbumType));
	}

	public int getSecondaryAlbumTypeId() {
		return secondaryAlbumType.ordinal();
	}

	public void setSecondaryAlbumType(String secondaryAlbumType) {
		this.secondaryAlbumType = AlbumType.valueOf(upperCase(secondaryAlbumType));
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(title)
		.append(mbid)
		.append(releaseYear)
		.append(primaryAlbumType)
		.append(secondaryAlbumType)
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
		.append(a.releaseYear, releaseYear)
		.append(a.primaryAlbumType, primaryAlbumType)
		.append(a.secondaryAlbumType, secondaryAlbumType)
		.isEquals();
	}
	
	@Override
	public String toString() {
		return String.format("Album %s (%s/%s, %d), %s", title, primaryAlbumType, 
				secondaryAlbumType, releaseYear, mbid);
	}

}