package com.github.hakko.musiccabinet.dao.jdbc;

import static java.io.File.separatorChar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcLibraryBrowserDao implements LibraryBrowserDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	@Override
	public List<Artist> getArtists() {
		String sql = "select ma.id, ma.artist_name_capitalization from music.artist ma"
				+ " inner join library.artist la on la.artist_id = ma.id";
		
		return jdbcTemplate.query(sql, new RowMapper<Artist>() {
			@Override
			public Artist mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new Artist(rs.getInt(1), rs.getString(2));
			}
		});
	}

	@Override
	public List<Album> getAlbums(int artistId) {
		String sql = "select ma.id, ma.album_name_capitalization, la.year,"
				+ " f1.path, f1.filename, f2.path, f2.filename, ai.largeimageurl"
				+ " from music.album ma"
				+ " inner join library.album la on la.album_id = ma.id "
				+ " left outer join (select f.id, f.filename, d.path from library.file f inner join library.directory d on f.directory_id = d.id) f1 on f1.id = la.coverartfile_id"
				+ " left outer join (select f.id, f.filename, d.path from library.file f inner join library.directory d on f.directory_id = d.id) f2 on f2.id = la.embeddedcoverartfile_id"
				+ " left outer join music.albuminfo ai on la.album_id = ai.album_id"
				+ " where ma.artist_id = ?";

		return jdbcTemplate.query(sql, new Object[]{artistId}, new RowMapper<Album>() {
			@Override
			public Album mapRow(ResultSet rs, int rowNum) throws SQLException {
				int albumId = rs.getInt(1);
				String albumName = rs.getString(2);
				short year = rs.getShort(3);
				String coverArtFile = getFileName(rs.getString(4), rs.getString(5));
				String coverArtEmbeddedFile = getFileName(rs.getString(6), rs.getString(7));
				String coverArtURL = rs.getString(8);
				return new Album(albumId, albumName, year, coverArtFile,
						coverArtEmbeddedFile, coverArtURL);
			}
		});
	}
	
	private String getFileName(String directory, String filename) {
		return directory == null || filename == null ? null :
			directory + separatorChar + filename;
	}

	@Override
	public List<Track> getTracks(int albumId) {
		String sql = "select mt.track_name_capitalization, ma.artist_name_capitalization,"
				+ " ft.track_nr, ft.track_nrs, ft.disc_nr, ft.disc_nrs, ft.year,"
				+ " fh.bitrate, fh.vbr, fh.duration "
				+ " from music.track mt"
				+ " inner join library.track lt on lt.track_id = mt.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " inner join library.fileheader fh on fh.file_id = lt.file_id"
				+ " inner join music.artist ma on ft.artist_id = ma.id"
				+ " where lt.album_id = ?";
		
		return jdbcTemplate.query(sql, new Object[]{albumId}, new RowMapper<Track>() {
			@Override
			public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
				String trackName = rs.getString(1);
				MetaData md = new MetaData();
				md.setArtist(rs.getString(2));
				md.setTrackNr(rs.getShort(3));
				md.setTrackNrs(rs.getShort(4));
				md.setDiscNr(rs.getShort(5));
				md.setDiscNrs(rs.getShort(6));
				md.setYear(rs.getShort(7));
				md.setBitrate(rs.getShort(8));
				md.setVbr(rs.getBoolean(9));
				md.setDuration(rs.getShort(10));
				return new Track(trackName, md);
			}
		});
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

}