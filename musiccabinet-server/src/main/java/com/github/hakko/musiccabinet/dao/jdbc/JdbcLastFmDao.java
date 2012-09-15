package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.LastFmGroupRowMapper;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public class JdbcLastFmDao implements LastFmDao, JdbcTemplateDao {

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
	public int getLastFmGroupId(String lastFmGroupName) {
		String sql = "select * from music.get_lastfmgroup_id(?)";
		
		return jdbcTemplate.queryForInt(sql, lastFmGroupName);
	}

	@Override
	public List<LastFmGroup> getLastFmGroups() {
		String sql = "select group_name_capitalization from music.lastfmgroup"
			+ " where enabled order by group_name";

		return jdbcTemplate.query(sql, new LastFmGroupRowMapper());
	}

	@Override
	public void setLastFmGroups(List<LastFmGroup> lastFmGroups) {
		jdbcTemplate.update("truncate music.lastfmgroup_import");
		
		String sql = "insert into music.lastfmgroup_import (group_name) values (?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("group_name", Types.VARCHAR));
		for (LastFmGroup group : lastFmGroups) {
			batchUpdate.update(new Object[]{group.getName()});
		}
		batchUpdate.flush();
		
		jdbcTemplate.execute("select music.update_lastfmgroup()");
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