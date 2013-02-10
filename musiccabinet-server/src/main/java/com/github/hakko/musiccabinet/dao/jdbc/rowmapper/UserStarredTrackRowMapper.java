package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.aggr.UserStarredTrack;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class UserStarredTrackRowMapper implements RowMapper<UserStarredTrack> {

	@Override
	public UserStarredTrack mapRow(ResultSet rs, int rowNum) throws SQLException {
		String username = rs.getString(1);
		String sessionKey = rs.getString(2);
		String artistName = rs.getString(3);
		String trackName = rs.getString(4);

		return new UserStarredTrack(new LastFmUser(username, sessionKey),
				new Track(artistName, trackName));
	}

}