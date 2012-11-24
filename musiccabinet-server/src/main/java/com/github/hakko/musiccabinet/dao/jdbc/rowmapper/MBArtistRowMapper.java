package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.MBArtist;

public class MBArtistRowMapper implements RowMapper<MBArtist> {

	@Override
	public MBArtist mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new MBArtist(rs.getInt(1), rs.getString(2), rs.getString(3), 
				rs.getString(4), rs.getShort(5), rs.getBoolean(6));
	}

}
