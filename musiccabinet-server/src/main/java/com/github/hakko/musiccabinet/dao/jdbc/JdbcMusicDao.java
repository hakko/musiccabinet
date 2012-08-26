package com.github.hakko.musiccabinet.dao.jdbc;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcMusicDao implements MusicDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	public int getArtistId(String artistName) {
		return jdbcTemplate.queryForInt(
			"select * from music.get_artist_id(?)", artistName);
	}
	
	public int getArtistId(Artist artist) {
		return getArtistId(artist.getName());
	}
	
	public Artist getArtist(String artistName) {
		String sql = "select artist_name_capitalization from music.artist where artist_name = upper(?)";
		return new Artist(jdbcTemplate.queryForObject(sql, String.class, artistName));
	}

	public int getAlbumId(String artistName, String albumName) {
		return jdbcTemplate.queryForInt(
				"select * from music.get_album_id(?,?)",
				artistName, albumName);
	}
	
	public int getAlbumId(Album album) {
		return getAlbumId(album.getArtist().getName(), album.getName());
	}
	
	public int getTrackId(String artistName, String trackName) {
		return jdbcTemplate.queryForInt(
			"select * from music.get_track_id(?,?)", 
			artistName, trackName);
	}
	
	public int getTrackId(Track track) {
		return getTrackId(track.getArtist().getName(), track.getName());
	}
	

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	// Spring setters
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

}