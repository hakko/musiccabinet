package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class AlbumInfo {

	private Album album;
	private String smallImageUrl;
	private String mediumImageUrl;
	private String largeImageUrl;
	private String extraLargeImageUrl;
	private int listeners;
	private int playCount;
	
	public AlbumInfo() {
		
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
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

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(album.getName())
		.append(smallImageUrl)
		.append(mediumImageUrl)
		.append(largeImageUrl)
		.append(extraLargeImageUrl)
		.append(listeners)
		.append(playCount)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		AlbumInfo ai = (AlbumInfo) o;
		return new EqualsBuilder()
		.append(album, ai.album)
		.append(smallImageUrl, ai.smallImageUrl)
		.append(mediumImageUrl, ai.mediumImageUrl)
		.append(largeImageUrl, ai.largeImageUrl)
		.append(extraLargeImageUrl, ai.extraLargeImageUrl)
		.append(listeners, ai.listeners)
		.append(playCount, ai.playCount)
		.isEquals();
	}

}