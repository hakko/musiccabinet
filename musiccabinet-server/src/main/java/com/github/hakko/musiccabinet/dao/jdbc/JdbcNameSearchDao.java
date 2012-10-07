package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.dao.NameSearchDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.AlbumRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistRowMapper;
import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.SearchCriteria;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcNameSearchDao implements NameSearchDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public NameSearchResult<Artist> getArtists(String userQuery, int offset, int limit) {
		String sql = "select ma.id, ma.artist_name_capitalization"
				+ " from library.artist la"
				+ " inner join music.artist ma on la.artist_id = ma.id" 
				+ " where la.artist_name_search like ?"
				+ " order by la.artist_name_search"
				+ " offset ? limit ?";
		List<Artist> artists = jdbcTemplate.query(sql, 
				new Object[]{getNameQuery(userQuery), offset, limit}, new ArtistRowMapper());
		return new NameSearchResult<>(artists, offset);
	}
	
	@Override
	public NameSearchResult<Album> getAlbums(String userQuery, int offset, int limit) {
		String sql = "select mart.id, mart.artist_name_capitalization,"
				+ " malb.id, malb.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, null, array[]::int[]"
				+ " from library.album la"
				+ " inner join music.album malb on la.album_id = malb.id"
				+ " inner join music.artist mart on malb.artist_id = mart.id" 
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " where la.album_name_search like ?"
				+ " order by la.album_name_search"
				+ " offset ? limit ?";
		List<Album> albums = jdbcTemplate.query(sql, 
				new Object[]{getNameQuery(userQuery), offset, limit}, new AlbumRowMapper());
		
		return new NameSearchResult<>(albums, offset);
	}

	@Override
	public NameSearchResult<Track> getTracks(String userQuery, int offset, int limit) {
		String sql = "select mart.id, mart.artist_name_capitalization,"
				+ " malb.id, malb.album_name_capitalization,"
				+ " lt.id, mt.track_name_capitalization from library.track lt"
				+ " inner join music.track mt on lt.track_id = mt.id"
				+ " inner join music.album malb on lt.album_id = malb.id"
				+ " inner join music.artist mart on mt.artist_id = mart.id" 
				+ " where lt.track_name_search like ?"
				+ " order by lt.track_name_search"
				+ " offset ? limit ?";
		List<Track> albums = jdbcTemplate.query(sql, 
				new Object[]{getNameQuery(userQuery), offset, limit}, new RowMapper<Track>() {
			@Override
			public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
				MetaData metaData = new MetaData();
				metaData.setArtistId(rs.getInt(1));
				metaData.setArtist(rs.getString(2));
				metaData.setAlbumId(rs.getInt(3));
				metaData.setAlbum(rs.getString(4));
				return new Track(rs.getInt(5), rs.getString(6), metaData);
			}
		});
		
		return new NameSearchResult<>(albums, offset);
	}

	/*
	 * Tables library.artist, library.album and library.track all has one column,
	 * used for searching for artist/album/track names. The column contains:
	 * - for an artist, artist name
	 * - for an album, artist name + album name
	 * - for a track, artist name + album name + track name
	 * 
	 * The words are sorted, so "Express Yourself" from album "Like A Prayer" by Madonna
	 * is stored as A EXPRESS LIKE MADONNA PRAYER YOURSELF
	 * 
	 * When a user searches for "express yourself madonna" or "madonna express yourself", 
	 * we internally translate this to an SQL query:
	 * track_name_search like '%EXPRESS%MADONNA%YOURSELF%'.
	 * 
	 * This method returns such a name search query, for a user query and a search type. 
	 */
	protected static String getNameQuery(String userQuery) {
		userQuery = StringUtils.replaceChars(userQuery, '%', ' ');
		userQuery = StringUtils.replaceChars(userQuery, '*', ' ');
		userQuery = StringUtils.replaceChars(userQuery, '"', ' ');
		String[] words = StringUtils.split(userQuery.toUpperCase(), ' ');
		Arrays.sort(words);
		String likeQuery = '%' + StringUtils.join(words, '%') + '%';

		return likeQuery;
	}

	@Override
	public List<Track> getTracks(SearchCriteria searchCriteria, int offset, int limit) {
		String sql = "select mart.id, mart.artist_name_capitalization,"
				+ " malb.id, malb.album_name_capitalization,"
				+ " lt.id, mt.track_name_capitalization from library.track lt"
				+ " inner join music.track mt on lt.track_id = mt.id"
				+ " inner join music.album malb on lt.album_id = malb.id"
				+ " inner join music.artist mart on mt.artist_id = mart.id" ;
		
		return null;
	}
	
	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	// Spring setters
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

}