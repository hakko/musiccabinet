package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.PlayCountDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcPlayCountDao implements PlayCountDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void addPlayCount(LastFmUser lastFmUser, Track track) {
		String sql = "insert into library.playcount (lastfmuser_id, track_id, album_id, artist_id)"
				+ " select ?,track_id,?,? from library.track where id = ?";
		
		jdbcTemplate.update(sql, lastFmUser.getId(), track.getMetaData().getAlbumId(),
				track.getMetaData().getArtistId(), track.getId());
	}

	@Override
	public List<Integer> getRecentArtists(String lastFmUser, int offset, int limit) {
		String sql = "select artist_id from ("
				+ " select artist_id, max(invocation_time) as last_invocation_time "
				+ " from library.playcount pc"
				+ " inner join library.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by artist_id"
				+ ") pc order by last_invocation_time desc offset ? limit ?";

		return jdbcTemplate.queryForList(sql, Integer.class, lastFmUser, offset, limit);
	}

	@Override
	public List<Integer> getRecentAlbums(String lastFmUser, int offset, int limit) {
		String sql = "select album_id from ("
				+ " select album_id, max(invocation_time) as last_invocation_time "
				+ " from library.playcount pc"
				+ " inner join library.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by album_id"
				+ ") pc order by last_invocation_time desc offset ? limit ?";

		return jdbcTemplate.queryForList(sql, Integer.class, lastFmUser, offset, limit);
	}

	@Override
	public List<Integer> getRecentTracks(String lastFmUser, int offset, int limit) {
		String sql = "select lt.id from ("
				+ " select track_id, album_id, max(invocation_time) as last_invocation_time"
				+ " from library.playcount pc"
				+ " inner join library.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?) group by track_id, album_id"
				+ ") pc inner join library.track lt"
				+ " on lt.track_id = pc.track_id and lt.album_id = pc.album_id"
				+ " order by last_invocation_time desc offset ? limit ?";

		return jdbcTemplate.queryForList(sql, Integer.class, lastFmUser, offset, limit);
	}

	@Override
	public List<Integer> getMostPlayedArtists(String lastFmUser, int offset, int limit) {
		String sql = "select artist_id"
				+ " from library.playcount pc"
				+ " inner join library.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by artist_id"
				+ " order by count(artist_id) desc offset ? limit ?";
		
		return jdbcTemplate.queryForList(sql, Integer.class, lastFmUser, offset, limit);
	}

	@Override
	public List<Integer> getMostPlayedAlbums(String lastFmUser, int offset, int limit) {
		String sql = "select album_id"
				+ " from library.playcount pc"
				+ " inner join library.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by album_id"
				+ " order by count(album_id) desc offset ? limit ?";
		
		return jdbcTemplate.queryForList(sql, Integer.class, lastFmUser, offset, limit);
	}

	@Override
	public List<Integer> getMostPlayedTracks(String lastFmUser, int offset, int limit) {
		String sql = "select lt.id from library.track lt"
				+ " inner join ("
				+ " select track_id, album_id"
				+ " from library.playcount pc"
				+ " inner join library.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by track_id, album_id"
				+ " order by count(track_id) desc) pc"
				+ " on pc.album_id = lt.album_id and pc.track_id = lt.track_id"
				+ " offset ? limit ?";
		
		return jdbcTemplate.queryForList(sql, Integer.class, lastFmUser, offset, limit);
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