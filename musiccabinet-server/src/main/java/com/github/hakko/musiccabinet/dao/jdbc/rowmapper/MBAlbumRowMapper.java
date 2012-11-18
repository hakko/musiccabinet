package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;

public class MBAlbumRowMapper implements RowMapper<MBAlbum> {

	@Override
	public MBAlbum mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new MBAlbum(rs.getString(1), rs.getString(2), rs.getShort(3), rs.getInt(4));
	}

}