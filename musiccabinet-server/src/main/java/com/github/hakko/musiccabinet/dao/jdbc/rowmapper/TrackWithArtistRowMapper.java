package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.Track;

public class TrackWithArtistRowMapper implements RowMapper<Track> {

	@Override
	public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
		String artistName = rs.getString(1);
		String trackName = rs.getString(2);

		return new Track(artistName, trackName);
	}

}