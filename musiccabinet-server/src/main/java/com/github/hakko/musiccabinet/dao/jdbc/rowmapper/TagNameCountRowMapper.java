package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.Tag;

public class TagNameCountRowMapper implements RowMapper<Tag> {

	@Override
	public Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
		String tagName = rs.getString(1);
		short tagCount = rs.getShort(2);
		return new Tag(tagName, tagCount);
	}

}