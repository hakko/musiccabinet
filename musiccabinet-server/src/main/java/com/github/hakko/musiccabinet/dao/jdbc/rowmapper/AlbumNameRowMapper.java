package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class AlbumNameRowMapper implements RowMapper<Album> {

	@Override
	public Album mapRow(ResultSet rs, int rowNum) throws SQLException {
		int artistId = rs.getInt(1);
		String artistName = rs.getString(2);
		int albumId = rs.getInt(3);
		String albumName = rs.getString(4);
		return new Album(new Artist(artistId, artistName), albumId, albumName);
	}

}
