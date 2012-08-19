package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.Tag;

public class TagRowMapper implements RowMapper<Tag> {

	@Override
	public Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new Tag(rs.getInt(1), rs.getString(2));
	}

}