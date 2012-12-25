package com.github.hakko.musiccabinet.domain.model.music;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class SearchCriteria {

	// filetag criteria
	private String artist;
	private String albumArtist;
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
	// TODO:
	private Short trackRankFrom;
	private Short trackRankTo;
	
	// file header criteria
	private Short durationFrom;
	private Short durationTo;
	private Set<Integer> filetypes = new HashSet<>();

	// file criteria
	private String directory;
	private Short modifiedDays;
	
	// external
	private String searchQuery;
	private String artistGenre;
	private Short topTrackRank;
	private String lastFmUsername;
	private boolean onlyStarredByUser;
	private Short playedLastDays;
	private Short playCountFrom;
	private Short playCountTo;

	public boolean hasFileHeaderCriteria() {
		return durationFrom != null || durationTo != null || filetypes != null;
	}

	public boolean hasFileCriteria() {
		return directory != null || modifiedDays != null;
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

	public Set<Integer> getFiletypes() {
		return filetypes;
	}

	public void setFiletypes(Set<Integer> filetypes) {
		this.filetypes = filetypes;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public Short getModifiedDays() {
		return modifiedDays;
	}

	public void setModifiedDays(Short modifiedDays) {
		this.modifiedDays = modifiedDays;
	}

	public String getArtistGenre() {
		return artistGenre;
	}

	public void setArtistGenre(String artistGenre) {
		this.artistGenre = artistGenre;
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public Short getTopTrackRank() {
		return topTrackRank;
	}

	public void setTopTrackRank(Short topTrackRank) {
		this.topTrackRank = topTrackRank;
	}

	public String getLastFmUsername() {
		return lastFmUsername;
	}

	public void setLastFmUsername(String lastFmUsername) {
		this.lastFmUsername = lastFmUsername;
	}

	public boolean isOnlyStarredByUser() {
		return onlyStarredByUser;
	}

	public void setOnlyStarredByUser(boolean onlyStarredByUser) {
		this.onlyStarredByUser = onlyStarredByUser;
	}

	public Short getPlayedLastDays() {
		return playedLastDays;
	}

	public void setPlayedLastDays(Short playedLastDays) {
		this.playedLastDays = playedLastDays;
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

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
	
}