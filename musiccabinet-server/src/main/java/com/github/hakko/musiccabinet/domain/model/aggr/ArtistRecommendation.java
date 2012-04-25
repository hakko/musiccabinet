package com.github.hakko.musiccabinet.domain.model.aggr;

/*
 * ArtistRecommendation doesn't map to a single database table but is rather aggregated
 * from music.artist, music.artistinfo and relational tables.
 */
public class ArtistRecommendation {

	private String artistName;
	private String imageUrl;
	private String path;
	
	public ArtistRecommendation(String artistName, String path) {
		this(artistName, null, path);
	}

	public ArtistRecommendation(String artistName, String imageUrl, String path) {
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.path = path;
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}