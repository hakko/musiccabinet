package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.Types;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.LibraryDeletionDao;
import com.github.hakko.musiccabinet.domain.model.library.File;

public class JdbcLibraryDeletionDao implements LibraryDeletionDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void clearImport() {
		jdbcTemplate.execute("truncate library.directory_delete");
		jdbcTemplate.execute("truncate library.file_delete");
	}

	@Override
	public void deleteSubdirectories(String directory, Set<String> subDirectories) {
		String sql = "insert into library.directory_delete (path) values (?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));
		
		for (String subDirectory : subDirectories) {
			batchUpdate.update(new Object[]{subDirectory});
		}
		batchUpdate.flush();
	}

	@Override
	public void deleteFiles(String directory, Set<File> files) {
		String sql = "insert into library.file_delete (path, filename) values (?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.declareParameter(new SqlParameter("path", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("filename", Types.VARCHAR));
		
		for (File file : files) {
			batchUpdate.update(new Object[]{file.getDirectory(), file.getFilename()});
		}
		batchUpdate.flush();
	}

	@Override
	public void updateLibrary() {
		jdbcTemplate.execute("select library.delete_from_library()");
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