package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.MBArtist;

public class MBArtistRowMapper implements RowMapper<MBArtist> {

	@Override
	public MBArtist mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new MBArtist(rs.getString(1), rs.getString(2), rs.getString(3), 
				rs.getShort(4), rs.getBoolean(5));
	}

}
