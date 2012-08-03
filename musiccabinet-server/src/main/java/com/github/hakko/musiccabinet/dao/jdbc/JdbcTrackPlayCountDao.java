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

import com.github.hakko.musiccabinet.dao.TrackPlayCountDao;
import com.github.hakko.musiccabinet.domain.model.library.TrackPlayCount;

public class JdbcTrackPlayCountDao implements TrackPlayCountDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void createTrackPlayCounts(List<TrackPlayCount> trackPlayCounts) {
		clearImportTable();
		batchInsert(trackPlayCounts);
		updateLibrary();
	}

	private void clearImportTable() {
		jdbcTemplate.execute("truncate library.trackplaycount_import");
	}
	
	private void batchInsert(List<TrackPlayCount> trackPlayCounts) {
		String sql = "insert into library.trackplaycount_import (artist_name, track_name, play_count) values (?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("track_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("play_count", Types.INTEGER));
		
		for (TrackPlayCount tpc : trackPlayCounts) {
			batchUpdate.update(new Object[]{tpc.getTrack().getArtist().getName(), 
					tpc.getTrack().getName(), tpc.getPlayCount()});
		}
		batchUpdate.flush();
	}

	private void updateLibrary() {
		jdbcTemplate.execute("select library.update_trackplaycount()");
	}

	@Override
	public List<TrackPlayCount> getTrackPlayCounts() {
		String sql = "select artist_name, track_name, play_count"
			+ " from library.trackplaycount" 
			+ " inner join music.track on library.trackplaycount.track_id = music.track.id"
			+ " inner join music.artist on music.track.artist_id = music.artist.id";
		
		List<TrackPlayCount> trackPlayCounts = jdbcTemplate.query(sql, 
				new RowMapper<TrackPlayCount>() {
			@Override
			public TrackPlayCount mapRow(ResultSet rs, int rowNum) throws SQLException {
				String artistName = rs.getString(1);
				String trackName = rs.getString(2);
				int playCount = rs.getInt(3);
				return new TrackPlayCount(artistName, trackName, playCount);
			}
		});
		
		return trackPlayCounts;
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