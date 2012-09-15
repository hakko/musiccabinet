package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;

public class LastFmGroupRowMapper implements RowMapper<LastFmGroup> {

	@Override
	public LastFmGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new LastFmGroup(rs.getString(1));
	}

}