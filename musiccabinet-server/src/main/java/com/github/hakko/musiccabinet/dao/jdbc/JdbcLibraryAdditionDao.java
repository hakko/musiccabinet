package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.Types;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.log.Logger;

public class JdbcLibraryAdditionDao implements LibraryAdditionDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	private static final Logger LOG = Logger.getLogger(JdbcLibraryAdditionDao.class);

	@Override
	public void clearImport() {
		jdbcTemplate.execute("delete from library.directory_import");
		jdbcTemplate.execute("delete from library.file_import");
	}

	@Override
	public void addSubdirectories(String directory, Set<String> subDirectories) {

		String sql = "insert into library.directory_import (parent_path, path) values (?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.declareParameter(new SqlParameter("parent_path", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));
		
		for (String subDirectory : subDirectories) {
			batchUpdate.update(new Object[]{directory, subDirectory});
		}
		batchUpdate.flush();

	}

	@Override
	public void addFiles(String directory, Set<File> files) {

		String sql = "insert into library.file_import (path, filename, modified, size) values (?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("filename", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("modified", Types.TIMESTAMP));
		batchUpdate.declareParameter(new SqlParameter("size", Types.INTEGER));

		boolean metaData = false;
		for (File file : files) {
			batchUpdate.update(new Object[]{file.getDirectory(), file.getFilename(), 
					file.getModified().toDate(), file.getSize()});
			metaData = metaData || file.getMetadata() != null;
		}
		batchUpdate.flush();

		if (metaData) {
			addMetadata(files);
		}
	}
	
	private void addMetadata(Set<File> files) {
		String sql = "insert into library.file_headertag_import (path, filename, extension,"
				+ "bitrate, vbr, duration, artist_name, album_artist_name, composer_name,"
				+ "album_name, track_name, track_nr, track_nrs, disc_nr, disc_nrs, year,"
				+ "tag_name, coverart, artistsort_name, albumartistsort_name) values"
				+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("filename", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("extension", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("bitrate", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("vbr", Types.BOOLEAN));
		batchUpdate.declareParameter(new SqlParameter("duration", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("album_artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("composer_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("album_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("track_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("track_nr", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("track_nrs", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("disc_nr", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("disc_nrs", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("year", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("coverart", Types.BOOLEAN));
		batchUpdate.declareParameter(new SqlParameter("artistsort_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("albumartistsort_name", Types.VARCHAR));
		for (File file : files) {
			MetaData md = file.getMetadata();
			if (md != null) {
				batchUpdate.update(new Object[]{file.getDirectory(), file.getFilename(),
						md.getMediaType().getFilesuffix(), md.getBitrate(), md.isVbr(),
						md.getDuration(), md.getArtist(), md.getAlbumArtist(), 
						md.getComposer(), md.getAlbum(), md.getTitle(), md.getTrackNr(), 
						md.getTrackNrs(), md.getDiscNr(), md.getDiscNrs(), md.getYear(),
						md.getGenre(), md.isCoverArtEmbedded(), md.getArtistSort(),
						md.getAlbumArtistSort()});
			}
		}
		batchUpdate.flush();
	}

	@Override
	public void updateLibrary() {
		jdbcTemplate.execute("select library.add_to_library()");
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

}