package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.MusicDirectoryDao;
import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;
import com.github.hakko.musiccabinet.log.Logger;

public class JdbcMusicDirectoryDao implements MusicDirectoryDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	private static final Logger LOG = Logger.getLogger(JdbcMusicDirectoryDao.class);
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void clearImport() {
		jdbcTemplate.execute("delete from library.musicdirectory_import");
	}

	@Override
	public void addMusicDirectories(List<MusicDirectory> musicDirectories) {
		String sql = "insert into library.musicdirectory_import (artist_name, album_name, path) values (?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("album_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));

		for (MusicDirectory md : musicDirectories) {
			batchUpdate.update(new Object[]{md.getArtistName(), md.getAlbumName(), md.getPath()});
		}
		batchUpdate.flush();
	}

	@Override
	public void createMusicDirectories() {
		jdbcTemplate.execute("select library.update_musicdirectory_from_import()");
	}
	
	@Override
	public List<MusicDirectory> getMusicDirectories() {
		String sql = "select art.artist_name_capitalization, alb.album_name_capitalization, md.path from library.musicdirectory md"
			+ " inner join music.artist art on md.artist_id = art.id"
			+ " left outer join music.album alb on md.album_id = alb.id";
		
		List<MusicDirectory> musicDirectories = jdbcTemplate.query(sql, 
				new RowMapper<MusicDirectory>() {
					@Override
					public MusicDirectory mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						String artistName = rs.getString(1);
						String albumName = rs.getString(2);
						String path = rs.getString(3);
						return new MusicDirectory(artistName, albumName, path);
					}
				}
		);
		
		return musicDirectories;
	}

	@Override
	public Integer getArtistId(String path) {
		Integer artistId = null;
		try {
			artistId = jdbcTemplate.queryForInt(
				"select artist_id from library.musicdirectory where path = ?", 
				new Object[]{path});
		} catch (DataAccessException e) {
			LOG.warn("Path " + path + " doesn't map to an artist!", e);
		}
		return artistId;
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

}