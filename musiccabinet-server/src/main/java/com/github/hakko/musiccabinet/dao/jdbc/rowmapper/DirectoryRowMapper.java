package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.library.Directory;

public class DirectoryRowMapper implements RowMapper<Directory> {

	@Override
	public Directory mapRow(ResultSet rs, int rowNum) throws SQLException {
		int id = rs.getInt(1);
		String path = rs.getString(2);
		return new Directory(id, path);
	}

}