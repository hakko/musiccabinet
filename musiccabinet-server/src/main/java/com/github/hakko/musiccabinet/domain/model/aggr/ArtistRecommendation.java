package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.Arrays;
import java.util.List;

/*
 * ArtistRecommendation doesn't map to a single database table but is rather aggregated
 * from music.artist, music.artistinfo and relational tables.
 */
public class ArtistRecommendation {

	private String artistName;
	private String imageUrl;
	private List<String> paths;
	
	public ArtistRecommendation(String artistName, String path) {
		this(artistName, null, Arrays.asList(path));
	}

	public ArtistRecommendation(String artistName, String imageUrl, List<String> paths) {
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.paths = paths;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}
	
}