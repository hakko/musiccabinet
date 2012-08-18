package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;

public class TagOccurrenceRowMapper implements RowMapper<TagOccurrence> {

	@Override
	public TagOccurrence mapRow(ResultSet rs, int rowNum) throws SQLException {
		String tag = rs.getString(1);
		String correctedTag = rs.getString(2);
		int occurrence = rs.getInt(3);
		boolean use = rs.getBoolean(4);
		return new TagOccurrence(tag, correctedTag, occurrence, use);
	}

}