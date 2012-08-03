package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ArtistInfo {

	private Artist artist;
	private String smallImageUrl;
	private String mediumImageUrl;
	private String largeImageUrl;
	private String extraLargeImageUrl;
	private int listeners;
	private int playCount;
	private String bioSummary;
	private String bioContent;
	private boolean isInSearchIndex;
	
	public ArtistInfo() {
	}
	
	public ArtistInfo(Artist artist, String largeImageUrl) {
		setArtist(artist);
		setLargeImageUrl(largeImageUrl);
	}

	public Artist getArtist() {
		return artist;
	}

	public void setArtist(Artist artist) {
		this.artist = artist;
	}

	public String getSmallImageUrl() {
		return smallImageUrl;
	}

	public void setSmallImageUrl(String smallImageUrl) {
		this.smallImageUrl = smallImageUrl;
	}

	public String getMediumImageUrl() {
		return mediumImageUrl;
	}

	public void setMediumImageUrl(String mediumImageUrl) {
		this.mediumImageUrl = mediumImageUrl;
	}

	public String getLargeImageUrl() {
		return largeImageUrl;
	}

	public void setLargeImageUrl(String largeImageUrl) {
		this.largeImageUrl = largeImageUrl;
	}

	public String getExtraLargeImageUrl() {
		return extraLargeImageUrl;
	}

	public void setExtraLargeImageUrl(String extraLargeImageUrl) {
		this.extraLargeImageUrl = extraLargeImageUrl;
	}

	public int getListeners() {
		return listeners;
	}

	public void setListeners(int listeners) {
		this.listeners = listeners;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	public String getBioSummary() {
		return bioSummary;
	}

	public void setBioSummary(String bioSummary) {
		this.bioSummary = bioSummary;
	}

	public String getBioContent() {
		return bioContent;
	}

	public void setBioContent(String bioContent) {
		this.bioContent = bioContent;
	}

	public boolean isInSearchIndex() {
		return isInSearchIndex;
	}

	public void setInSearchIndex(boolean isInSearchIndex) {
		this.isInSearchIndex = isInSearchIndex;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(artist.getName())
		.append(smallImageUrl)
		.append(mediumImageUrl)
		.append(largeImageUrl)
		.append(extraLargeImageUrl)
		.append(listeners)
		.append(playCount)
		.append(bioSummary)
		.append(bioContent)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		ArtistInfo ai = (ArtistInfo) o;
		return new EqualsBuilder()
		.append(artist, ai.artist)
		.append(smallImageUrl, ai.smallImageUrl)
		.append(mediumImageUrl, ai.mediumImageUrl)
		.append(largeImageUrl, ai.largeImageUrl)
		.append(extraLargeImageUrl, ai.extraLargeImageUrl)
		.append(listeners, ai.listeners)
		.append(playCount, ai.playCount)
		.append(bioSummary, ai.bioSummary)
		.append(bioContent, ai.bioContent)
		.isEquals();
	}

}