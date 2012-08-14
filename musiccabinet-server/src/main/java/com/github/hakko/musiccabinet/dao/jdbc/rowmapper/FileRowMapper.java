package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.library.File;

public class FileRowMapper implements RowMapper<File> {

	@Override
	public File mapRow(ResultSet rs, int rowNum) throws SQLException {
		String directory = rs.getString(1);
		String filename = rs.getString(2);
		DateTime modified = new DateTime(rs.getTimestamp(3).getTime());
		int size = rs.getInt(4);
		return new File(directory, filename, modified, size);
	}

}