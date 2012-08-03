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

import com.github.hakko.musiccabinet.dao.TrackRelationDao;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;

public class JdbcTrackRelationDao implements TrackRelationDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void createTrackRelations(Track sourceTrack, List<TrackRelation> trackRelations) {
		if (trackRelations.size() > 0) {
			clearImportTable();
			batchInsert(sourceTrack, trackRelations);
			updateLibrary();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.trackrelation_import");
	}

	private void batchInsert(Track sourceTrack, List<TrackRelation> trackRelations) {
		int sourceTrackId = jdbcTemplate.queryForInt("select * from music.get_track_id(?,?)",
				sourceTrack.getArtist().getName(), sourceTrack.getName());
		
		String sql = "insert into music.trackrelation_import (source_id, target_artist_name, target_track_name, weight) values (?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("source_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("target_artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("target_track_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("weight", Types.FLOAT));
		
		for (TrackRelation tr : trackRelations) {
			batchUpdate.update(new Object[]{
					sourceTrackId, tr.getTarget().getArtist().getName(),
					tr.getTarget().getName(), tr.getMatch()});
		}
		batchUpdate.flush();
	}

	private void updateLibrary() {
		jdbcTemplate.execute("select music.update_trackrelation()");
	}
	
	@Override
	public List<TrackRelation> getTrackRelations(Track sourceTrack) {
		final int sourceTrackId = jdbcTemplate.queryForInt(
				"select * from music.get_track_id(?,?)", 
				sourceTrack.getArtist().getName(), sourceTrack.getName());
		
		String sql = "select artist_name_capitalization, track_name_capitalization, weight"
			+ " from music.trackrelation" 
			+ " inner join music.track on music.trackrelation.target_id = music.track.id"
			+ " inner join music.artist on music.track.artist_id = music.artist.id"
			+ " where music.trackrelation.source_id = ?";
		
		List<TrackRelation> trackRelations = jdbcTemplate.query(sql, 
				new Object[]{sourceTrackId}, new RowMapper<TrackRelation>() {
			@Override
			public TrackRelation mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				String artistName = rs.getString(1);
				String trackName = rs.getString(2);
				float weight = rs.getFloat(3);
				return new TrackRelation(new Track(artistName, trackName), weight);
			}
		});
		
		return trackRelations;
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