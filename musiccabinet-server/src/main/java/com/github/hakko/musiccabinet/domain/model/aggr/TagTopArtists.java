package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class TagTopArtists {

	private final String tagName;
	private final List<Artist> artists;

	public TagTopArtists(String tagName, List<Artist> artists) {
		this.tagName = tagName;
		this.artists = artists;
	}

	public String getTagName() {
		return tagName;
	}

	public List<Artist> getArtists() {
		return artists;
	}
	
}