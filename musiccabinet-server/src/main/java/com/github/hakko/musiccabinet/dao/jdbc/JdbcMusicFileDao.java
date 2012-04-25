package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class JdbcMusicFileDao implements MusicFileDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public void clearImport() {
		jdbcTemplate.execute("delete from library.musicfile_import");
	}
	
	@Override
	public void addMusicFiles(List<MusicFile> musicFiles) {
		String sql = "insert into library.musicfile_import (artist_name, track_name, path, created, last_modified) values (?,?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("track_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("created", Types.TIMESTAMP));
		batchUpdate.declareParameter(new SqlParameter("last_modified", Types.TIMESTAMP));
		
		for (MusicFile mf : musicFiles) {
			batchUpdate.update(new Object[]{mf.getTrack().getArtist().getName(),
					mf.getTrack().getName(), mf.getPath(), 
					getDate(mf.getCreated()), getDate(mf.getLastModified())});
		}
		batchUpdate.flush();
	}
	
	private Date getDate(DateTime dateTime) {
		return dateTime == null ? null : dateTime.toDate();
	}

	@Override
	public void createMusicFiles() {
		jdbcTemplate.execute("select library.update_musicfile_from_import()");
	}
	
	@Override
	public void createMusicFileInternalIds() {
		jdbcTemplate.execute("select library.update_musicfile_external_ids()");
	}


	@Override
	public List<MusicFile> getMusicFiles() {
		String sql = "select artist_name, track_name, path, created, last_modified"
			+ " from library.musicfile" 
			+ " inner join music.track on library.musicfile.track_id = music.track.id"
			+ " inner join music.artist on music.track.artist_id = music.artist.id";
		
		List<MusicFile> musicFiles = jdbcTemplate.query(sql, new RowMapper<MusicFile>() {
			@Override
			public MusicFile mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				String artistName = rs.getString(1);
				String trackName = rs.getString(2);
				String path = rs.getString(3);
				long created = rs.getTimestamp(4).getTime();
				long lastModified = rs.getTimestamp(5).getTime();
				return new MusicFile(artistName, trackName, path, created, lastModified);
			}
		});
		
		return musicFiles;
	}

	@Override
	public int getTrackId(String path) throws ApplicationException {
		String sql = "select mf.track_id from library.musicfile mf"
			+ " where mf.path = ?";
		int trackId;
		try {
			trackId = jdbcTemplate.queryForInt(sql, path);
		} catch (DataAccessException e) {
			throw new ApplicationException("No track found at " + path + "!", e);
		}
		
		return trackId;
	}
	
	@Override
	public Track getTrack(String path) {
		String sql = "select a.artist_name_capitalization, t.track_name_capitalization from library.musicfile mf"
			+ " inner join music.track t on mf.track_id = t.id"
			+ " inner join music.artist a on t.artist_id = a.id"
			+ " where mf.path = ?";
		
		Track track = jdbcTemplate.query(sql, new Object[]{path}, new ResultSetExtractor<Track>() {
			@Override
			public Track extractData(ResultSet rs) throws SQLException {
				if (rs.next()) {
					String artistName = rs.getString(1);
					String trackName = rs.getString(2);
					return new Track(artistName, trackName);
				}
				return null;
			}
		});
		
		return track;
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

}