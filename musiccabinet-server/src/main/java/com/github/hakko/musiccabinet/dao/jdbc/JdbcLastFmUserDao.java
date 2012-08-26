package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.dao.LastFmUserDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public class JdbcLastFmUserDao implements LastFmUserDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	public int getLastFmUserId(String lastFmUsername) {
		String sql = "select * from library.get_lastfmuser_id(?)";
		
		return jdbcTemplate.queryForInt(sql, lastFmUsername);
	}

	@Override
	public LastFmUser getLastFmUser(String lastFmUsername) {
		String sql = "select id, lastfm_user_capitalization, session_key"
				+ " from library.lastfmuser where lastfm_user = upper(?)";
		return jdbcTemplate.queryForObject(sql, new Object[]{lastFmUsername}, new RowMapper<LastFmUser>() {
			@Override
			public LastFmUser mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new LastFmUser(rs.getInt(1), rs.getString(2), rs.getString(3));
			}
		});
	}

	@Override
	public void createOrUpdateLastFmUser(LastFmUser user) {
		user.setId(getLastFmUserId(user.getLastFmUsername()));
		
		String sql = "update library.lastfmuser set lastfm_user_capitalization = ?, session_key = ?	"
				+ " where id = ?";
		
		jdbcTemplate.update(sql, user.getLastFmUsername(), user.getSessionKey(), user.getId());
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