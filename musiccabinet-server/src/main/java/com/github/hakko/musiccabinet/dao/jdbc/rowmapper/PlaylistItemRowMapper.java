package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;

public class PlaylistItemRowMapper implements RowMapper<PlaylistItem> {

	/*
	 * Expects a ResultSet of (artist_id, track_id).
	 */
	@Override
	public PlaylistItem mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new PlaylistItem(rs.getInt(1), rs.getInt(2));
	}

}
