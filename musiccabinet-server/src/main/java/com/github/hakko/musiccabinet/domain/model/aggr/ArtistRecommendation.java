package com.github.hakko.musiccabinet.domain.model.aggr;

/*
 * ArtistRecommendation doesn't map to a single database table but is rather aggregated
 * from music.artist and music.artistinfo.
 */
public class ArtistRecommendation {

	private String artistName;
	private String imageUrl;
	private int artistId;
	
	public ArtistRecommendation(String artistName, int artistId) {
		this(artistName, null, artistId);
	}

	public ArtistRecommendation(String artistName, String imageUrl, int artistId) {
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.artistId = artistId;
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

	public int getArtistId() {
		return artistId;
	}

	public void setArtistId(int artistId) {
		this.artistId = artistId;
	}
	
}