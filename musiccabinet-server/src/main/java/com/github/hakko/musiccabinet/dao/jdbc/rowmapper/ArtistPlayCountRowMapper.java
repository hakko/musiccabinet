package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.ArtistPlayCount;

public class ArtistPlayCountRowMapper implements RowMapper<ArtistPlayCount> {

	@Override
	public ArtistPlayCount mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new ArtistPlayCount(rs.getString(1), rs.getInt(2));
	}

}