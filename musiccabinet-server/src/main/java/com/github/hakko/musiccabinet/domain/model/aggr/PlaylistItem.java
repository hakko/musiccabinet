package com.github.hakko.musiccabinet.domain.model.aggr;

import com.github.hakko.musiccabinet.domain.model.music.Artist;

/*
 * Subsonic uses file paths as primary key (for artists, albums, tracks),
 * so we normally just pass paths as result set from queries.
 * 
 * Internally, we need artist/track name as well for distributing tracks
 * in a generated playlist, to avoid having multiple tracks/artist in a row.
 * 
 * PlaylistItem doesn't map to a single database table. It is aggregated
 * from music.artist, music.track and library.musicfile.
 */
public class PlaylistItem {

	private Artist artist;
	private String path;
	
	public PlaylistItem(String artistName, String path) {
		this.artist = new Artist(artistName);
		this.path = path;
	}

	public Artist getArtist() {
		return artist;
	}
	
	public String getPath() {
		return path;
	}
	
}