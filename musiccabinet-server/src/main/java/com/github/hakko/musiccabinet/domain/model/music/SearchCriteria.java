package com.github.hakko.musiccabinet.domain.model.music;

import java.util.Set;

import com.github.hakko.musiccabinet.domain.model.library.MetaData.Mediatype;

public class SearchCriteria {

	private String artist;
	private String albumArtist;
	private String artistGenre;
	private String composer;
	private String album;
	private String title;
	private Short trackNrFrom;
	private Short trackNrTo;
	private Short discNrFrom;
	private Short discNrTo;
	private Short yearFrom;
	private Short yearTo;
	private String trackGenre;
	private Boolean coverArtEmbedded;
	private Long sizeFrom;
	private Long sizeTo;
	private Short durationFrom;
	private Short durationTo;
	private Set<Mediatype> mediaTypes;
	private Boolean vbr;
	private Short playCountFrom;
	private Short playCountTo;
	private Short trackRankFrom;
	private Short trackRankTo;
	
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

	public String getArtistGenre() {
		return artistGenre;
	}

	public void setArtistGenre(String artistGenre) {
		this.artistGenre = artistGenre;
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

	public Short getTrackNrFrom() {
		return trackNrFrom;
	}

	public void setTrackNrFrom(Short trackNrFrom) {
		this.trackNrFrom = trackNrFrom;
	}

	public Short getTrackNrTo() {
		return trackNrTo;
	}

	public void setTrackNrTo(Short trackNrTo) {
		this.trackNrTo = trackNrTo;
	}

	public Short getDiscNrFrom() {
		return discNrFrom;
	}

	public void setDiscNrFrom(Short discNrFrom) {
		this.discNrFrom = discNrFrom;
	}

	public Short getDiscNrTo() {
		return discNrTo;
	}

	public void setDiscNrTo(Short discNrTo) {
		this.discNrTo = discNrTo;
	}

	public Short getYearFrom() {
		return yearFrom;
	}

	public void setYearFrom(Short yearFrom) {
		this.yearFrom = yearFrom;
	}

	public Short getYearTo() {
		return yearTo;
	}

	public void setYearTo(Short yearTo) {
		this.yearTo = yearTo;
	}

	public String getTrackGenre() {
		return trackGenre;
	}

	public void setTrackGenre(String trackGenre) {
		this.trackGenre = trackGenre;
	}

	public Boolean getCoverArtEmbedded() {
		return coverArtEmbedded;
	}

	public void setCoverArtEmbedded(Boolean coverArtEmbedded) {
		this.coverArtEmbedded = coverArtEmbedded;
	}

	public Long getSizeFrom() {
		return sizeFrom;
	}

	public void setSizeFrom(Long sizeFrom) {
		this.sizeFrom = sizeFrom;
	}

	public Long getSizeTo() {
		return sizeTo;
	}

	public void setSizeTo(Long sizeTo) {
		this.sizeTo = sizeTo;
	}

	public Short getDurationFrom() {
		return durationFrom;
	}

	public void setDurationFrom(Short durationFrom) {
		this.durationFrom = durationFrom;
	}

	public Short getDurationTo() {
		return durationTo;
	}

	public void setDurationTo(Short durationTo) {
		this.durationTo = durationTo;
	}

	public Set<Mediatype> getMediaTypes() {
		return mediaTypes;
	}

	public void setMediaTypes(Set<Mediatype> mediaTypes) {
		this.mediaTypes = mediaTypes;
	}

	public Boolean getVbr() {
		return vbr;
	}

	public void setVbr(Boolean vbr) {
		this.vbr = vbr;
	}

	public Short getPlayCountFrom() {
		return playCountFrom;
	}

	public void setPlayCountFrom(Short playCountFrom) {
		this.playCountFrom = playCountFrom;
	}

	public Short getPlayCountTo() {
		return playCountTo;
	}

	public void setPlayCountTo(Short playCountTo) {
		this.playCountTo = playCountTo;
	}

	public Short getTrackRankFrom() {
		return trackRankFrom;
	}

	public void setTrackRankFrom(Short trackRankFrom) {
		this.trackRankFrom = trackRankFrom;
	}

	public Short getTrackRankTo() {
		return trackRankTo;
	}

	public void setTrackRankTo(Short trackRankTo) {
		this.trackRankTo = trackRankTo;
	}
	
}