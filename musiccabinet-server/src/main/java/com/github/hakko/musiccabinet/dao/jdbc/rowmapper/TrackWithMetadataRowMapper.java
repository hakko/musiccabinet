package com.github.hakko.musiccabinet.dao.jdbc.rowmapper;

import static java.io.File.separatorChar;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.library.MetaData.Mediatype;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class TrackWithMetadataRowMapper implements RowMapper<Track> {

	@Override
	public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
		String trackName = rs.getString(1);
		MetaData md = new MetaData();
		md.setAlbum(rs.getString(2));
		md.setArtist(rs.getString(3));
		md.setAlbumArtist(rs.getString(4));
		md.setComposer(rs.getString(5));
		md.setTrackNr(rs.getShort(6));
		md.setTrackNrs(rs.getShort(7));
		md.setDiscNr(rs.getShort(8));
		md.setDiscNrs(rs.getShort(9));
		md.setYear(rs.getShort(10));
		md.setHasLyrics(rs.getBoolean(11));
		md.setBitrate(rs.getShort(12));
		md.setVbr(rs.getBoolean(13));
		md.setDuration(rs.getShort(14));
		md.setMediaType(Mediatype.values()[rs.getShort(15)]);
		md.setPath(rs.getString(16) + separatorChar + rs.getString(17));
		md.setSize(rs.getInt(18));
		md.setModified(rs.getTimestamp(19).getTime());
		int trackId = rs.getInt(20);
		md.setAlbumId(rs.getInt(21));
		md.setArtistId(rs.getInt(22));

		return new Track(trackId, trackName, md);
	}

}