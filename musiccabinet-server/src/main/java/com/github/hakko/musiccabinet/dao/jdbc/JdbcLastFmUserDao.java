package com.github.hakko.musiccabinet.dao.jdbc;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.LastFmUserDao;

public class JdbcLastFmUserDao implements LastFmUserDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public int getLastFmUserId(String lastFmUser) {
		int userId = jdbcTemplate.queryForInt(
				"select * from library.get_lastfmuser_id(?)", 
				new Object[]{lastFmUser});
		return userId;
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

}