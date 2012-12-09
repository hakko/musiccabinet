package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.Track;

public class TrackRowMapper implements RowMapper<Track> {

	@Override
	public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
		Track track = new Track();
		track.setId(rs.getInt(1));
		track.setName(rs.getString(2));
		
		return track;
	}

}