package com.github.hakko.musiccabinet.dao.jdbc;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcMusicDao implements MusicDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public int getArtistId(String artistName) {
		int artistId = jdbcTemplate.queryForInt(
			"select * from music.get_artist_id(?)", 
			new Object[]{artistName});
		return artistId;
	}
	
	public int getArtistId(Artist artist) {
		return getArtistId(artist.getName());
	}
	
	public Artist getArtist(String artistName) {
		String sql = "select artist_name_capitalization from music.artist where artist_name = upper(?)";
		return new Artist(jdbcTemplate.queryForObject(sql, String.class, artistName));
	}

	public int getAlbumId(String artistName, String albumName) {
		int albumId = jdbcTemplate.queryForInt(
				"select * from music.get_album_id(?,?)",
				new Object[]{artistName, albumName});
		return albumId;
	}
	
	public int getAlbumId(Album album) {
		return getAlbumId(album.getArtist().getName(), album.getName());
	}
	
	public int getTrackId(String artistName, String trackName) {
		int trackId = jdbcTemplate.queryForInt(
			"select * from music.get_track_id(?,?)", 
			new Object[]{artistName, trackName});
		return trackId;
	}
	
	public int getTrackId(Track track) {
		return getTrackId(track.getArtist().getName(), track.getName());
	}
	

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

}