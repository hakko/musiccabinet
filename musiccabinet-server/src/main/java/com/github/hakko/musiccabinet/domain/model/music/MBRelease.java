package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum.AlbumType;

/*
 * Represents a release from MusicBrainz (a specific version of an album).
 */
public class MBRelease {

	private int artistId; // database id for artist
	private String title;
	private AlbumType type; // Album/EP/Single
	private short releaseYear;
	private String labelMbid;
	private String labelName;
	private String releaseGroupMbid;
	private String format; // CD/Digital/Vinyl/...
	
	public MBRelease() {
		
	}

	public MBRelease(String releaseGroupMbid, String labelMbid, String labelName,
			String title, String type, int releaseYear, String format) {
		this.releaseGroupMbid = releaseGroupMbid;
		this.labelMbid = labelMbid;
		this.labelName = labelName;
		this.title = title;
		this.type = AlbumType.getAlbumType(type);
		this.releaseYear = (short) releaseYear;
		this.format = format;
	}

	public int getArtistId() {
		return artistId;
	}

	public void setArtistId(int artistId) {
		this.artistId = artistId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AlbumType getAlbumType() {
		return type;
	}

	public void setAlbumType(String albumType) {
		this.type = AlbumType.getAlbumType(albumType);
	}

	public short getReleaseYear() {
		return releaseYear;
	}

	public void setReleaseYear(short releaseYear) {
		this.releaseYear = releaseYear;
	}

	public String getLabelMbid() {
		return labelMbid;
	}

	public void setLabelMbid(String labelMbid) {
		this.labelMbid = labelMbid;
	}

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public String getReleaseGroupMbid() {
		return releaseGroupMbid;
	}

	public void setReleaseGroupMbid(String releaseGroupMbid) {
		this.releaseGroupMbid = releaseGroupMbid;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public boolean isValid() {
		return title != null && type != null && releaseGroupMbid != null;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		MBRelease r = (MBRelease) o;
		return new EqualsBuilder()
		.append(r.title, title)
		.append(r.type, type)
		.append(r.releaseYear, releaseYear)
		.append(r.labelMbid, labelMbid)
		.append(r.labelName, labelName)
		.append(r.releaseGroupMbid, releaseGroupMbid)
		.append(r.format, format)
		.isEquals();
	}
	
	@Override
	public String toString() {
		return String.format("Release %s (%s, %s, %d) on %s (%s), group %s", title, 
				type, format, releaseYear, labelName, labelMbid, releaseGroupMbid);
	}
	
}