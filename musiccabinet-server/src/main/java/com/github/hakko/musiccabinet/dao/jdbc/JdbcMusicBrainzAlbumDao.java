package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.MusicBrainzAlbumDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.MBAlbumRowMapper;
import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;

public class JdbcMusicBrainzAlbumDao implements MusicBrainzAlbumDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void createAlbums(List<MBAlbum> albums) {
		if (albums.size() > 0) {
			clearImportTable();
			batchInsert(albums);
			updateLibrary();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.mb_album_import");
	}
	
	private void batchInsert(List<MBAlbum> albums) {
		String sql = "insert into music.mb_album_import"
				+ " (artist_id, album_name, mbid, type_id, release_year) values (?,?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("album_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("mbid", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("type_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("release_year", Types.SMALLINT));
		
		for (MBAlbum album : albums) {
			batchUpdate.update(new Object[]{album.getArtist().getId(), album.getTitle(),
					album.getMbid(), album.getPrimaryAlbumTypeId(), album.getReleaseYear()});
		}
		batchUpdate.flush();

	}

	private void updateLibrary() {
		jdbcTemplate.execute("select music.update_mbalbum()");
	}
	
	@Override
	public List<MBAlbum> getAlbums(int artistId) {
		return jdbcTemplate.query(
				"select art.artist_name_capitalization, alb.album_name_capitalization,"
				+ " mba.release_year, mba.type_id from music.mb_album mba"
				+ " inner join music.album alb on mba.album_id = alb.id"
				+ " inner join music.artist art on alb.artist_id = art.id"
				+ " where art.id = " + artistId + " order by mba.release_year", 
				new MBAlbumRowMapper());
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