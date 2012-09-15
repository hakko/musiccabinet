package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.GroupWeeklyArtistChartDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistPlayCountRowMapper;
import com.github.hakko.musiccabinet.domain.model.aggr.GroupWeeklyArtistChart;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.music.ArtistPlayCount;

public class JdbcGroupWeeklyArtistChartDao implements GroupWeeklyArtistChartDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void createArtistCharts(List<GroupWeeklyArtistChart> artistCharts) {
		if (artistCharts.size() > 0) {
			clearImportTable();
			for (GroupWeeklyArtistChart artistChart : artistCharts) {
				batchInsert(artistChart);
			}
			updateArtistCharts();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.groupweeklyartistchart_import");
	}

	private void batchInsert(GroupWeeklyArtistChart artistChart) {
		String sql = "insert into music.groupweeklyartistchart_import (lastfmgroup_name, artist_name, playcount) values (?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("lastfmgroup_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("playcount", Types.INTEGER));
		
		for (ArtistPlayCount apc : artistChart.getArtistPlayCounts()) {
			batchUpdate.update(new Object[]{artistChart.getGroupName(), 
					apc.getArtist().getName(), apc.getPlayCount()});
		}
		batchUpdate.flush();
	}

	private void updateArtistCharts() {
		jdbcTemplate.execute("select music.update_groupartistchart()");
	}

	@Override
	public GroupWeeklyArtistChart getWeeklyArtistChart(LastFmGroup group) {
		String sql = "select a.artist_name_capitalization, gwac.playcount"
				+ " from music.artist a"
				+ " inner join music.groupweeklyartistchart gwac on gwac.artist_id = a.id"
				+ " inner join music.lastfmgroup g on gwac.lastfmgroup_id = g.id"
				+ " where g.group_name = upper(?)"
				+ " order by playcount desc";
		
		return new GroupWeeklyArtistChart(group.getName(), jdbcTemplate.query(
				sql, new Object[]{group.getName()}, new ArtistPlayCountRowMapper()));
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