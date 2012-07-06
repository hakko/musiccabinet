package com.github.hakko.musiccabinet.domain.model.library;

import static org.apache.commons.lang.math.NumberUtils.isDigits;
import static org.apache.commons.lang.math.NumberUtils.toShort;

public class MetaData {

	private Mediatype mediaType;
	private short bitrate;
	private boolean vbr;
	private short duration;
	
	private String artist;
	private String albumArtist;
	private String composer;
	private String album;
	private String title;
	private Short trackNr;
	private Short trackNrs;
	private Short discNr;
	private Short discNrs;
	private Short year;
	private String genre;
	private boolean isCoverArtEmbedded;
	
	private String artistSort;
	private String albumArtistSort;
	
	public Mediatype getMediaType() {
		return mediaType;
	}

	public void setMediaType(Mediatype mediaType) {
		this.mediaType = mediaType;
	}

	public short getBitrate() {
		return bitrate;
	}

	public void setBitrate(short bitrate) {
		this.bitrate = bitrate;
	}

	public boolean isVbr() {
		return vbr;
	}

	public void setVbr(boolean vbr) {
		this.vbr = vbr;
	}

	public short getDuration() {
		return duration;
	}

	public void setDuration(short duration) {
		this.duration = duration;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbumArtist() {
		return albumArtist;
	}

	public void setAlbumArtist(String albumArtist) {
		this.albumArtist = albumArtist;
	}

	public String getComposer() {
		return composer;
	}

	public void setComposer(String composer) {
		this.composer = composer;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Short getTrackNr() {
		return trackNr;
	}

	public void setTrackNr(Short trackNr) {
		this.trackNr = trackNr;
	}

	public Short getTrackNrs() {
		return trackNrs;
	}

	public void setTrackNrs(Short trackNrs) {
		this.trackNrs = trackNrs;
	}

	public Short getDiscNr() {
		return discNr;
	}

	public void setDiscNr(Short discNr) {
		this.discNr = discNr;
	}

	public Short getDiscNrs() {
		return discNrs;
	}

	public void setDiscNrs(Short discNrs) {
		this.discNrs = discNrs;
	}

	public Short getYear() {
		return year;
	}

	public void setYear(String year) {
		if (year != null && year.length() > 4) {
			year = year.substring(0, 4);
		}
		this.year = isDigits(year) ? toShort(year) : null;
	}
	
	public void setYear(Short year) {
		this.year = year;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public boolean isCoverArtEmbedded() {
		return isCoverArtEmbedded;
	}

	public void setCoverArtEmbedded(boolean isCoverArtEmbedded) {
		this.isCoverArtEmbedded = isCoverArtEmbedded;
	}

	public String getArtistSort() {
		return artistSort;
	}

	public void setArtistSort(String sortArtist) {
		this.artistSort = sortArtist;
	}

	public String getAlbumArtistSort() {
		return albumArtistSort;
	}

	public void setAlbumArtistSort(String albumArtistSort) {
		this.albumArtistSort = albumArtistSort;
	}

	public enum Mediatype { 

		// these are taken from org.jaudiotagger.audio.SupportedFileFormat,
		// and map to library.fileheader_type

	    OGG("OGG"),
	    MP3("MP3"),
	    FLAC("FLAC"),
	    MP4("MP4"),
	    M4A("M4A"),
	    M4P("M4P"),
	    WMA("WMA"),
	    WAV("WAV"),
	    RA("RA"),
	    RM("RM"),
	    M4B("M4B");

	    private String filesuffix;

	    Mediatype(String filesuffix) {
	        this.filesuffix = filesuffix;
	    }

	    public String getFilesuffix() {
	        return filesuffix;
	    }
	    
	}

}