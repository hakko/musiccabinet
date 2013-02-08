package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import static java.io.File.separatorChar;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class FilenameRowMapper implements RowMapper<String> {

	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {
		String directory = rs.getString(1);
		String filename = rs.getString(2);
		return directory + separatorChar + filename;
	}

}