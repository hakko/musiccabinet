package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistRowMapper;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcMusicDao implements MusicDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	public void setArtistId(Artist artist) {
		artist.setId(getArtistId(artist.getName()));
	}
	
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

	public List<Artist> getArtists(Set<Integer> artistIds) {
		if (artistIds.isEmpty()) {
			return new ArrayList<>();
		}
		
		String sql = "select id, artist_name_capitalization from music.artist"
				+ " where id in (" + PostgreSQLUtil.getIdParameters(artistIds) + ")"
				+ " order by artist_name_capitalization";
		
		return jdbcTemplate.query(sql, new ArtistRowMapper());
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