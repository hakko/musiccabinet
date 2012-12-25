package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil.getIdParameters;
import static java.lang.String.format;
import static org.apache.commons.lang.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang.StringUtils.replace;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
				+ " order by la.hasalbums desc, ma.artist_name"
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
				+ " order by malb.album_name"
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
				+ " order by mt.track_name"
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
	public List<Integer> getTrackIds(SearchCriteria searchCriteria, int offset, int limit) {
		StringBuilder select = new StringBuilder("select lt.id from library.track lt"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id");
		StringBuilder where = new StringBuilder(" where true");
		List<Object> args = new ArrayList<>();

		addFileTagCriteria(select, where, args, searchCriteria);
		addFileHeaderCriteria(select, where, args, searchCriteria);
		addFileCriteria(select, where, args, searchCriteria);
		addExternalCriteria(select, where, args, searchCriteria);
		select.append(where);
		select.append(" order by ft.album_id desc, ft.disc_nr, ft.track_nr");
		select.append(format(" offset %d limit %d", offset, limit));
		
		return jdbcTemplate.queryForList(select.toString(), args.toArray(), Integer.class);
	}
	
	private void addFileTagCriteria(StringBuilder select, StringBuilder where, 
			List<Object> args, SearchCriteria criteria) {
		if (criteria.getArtist() != null) {
			select.append(" inner join music.artist tag_artist on ft.artist_id = tag_artist.id");
			where.append(" and tag_artist.artist_name like upper(?)");
			args.add(sqlContains(criteria.getArtist()));
		}
		if (criteria.getAlbumArtist() != null) {
			select.append(" inner join music.artist tag_albumartist on ft.album_artist_id = tag_albumartist.id");
			where.append(" and tag_albumartist.artist_name like upper(?)");
			args.add(sqlContains(criteria.getAlbumArtist()));
		}
		if (criteria.getComposer() != null) {
			select.append(" inner join music.artist tag_composer on ft.composer_id = tag_composer.id");
			where.append(" and tag_composer.artist_name like upper(?)");
			args.add(sqlContains(criteria.getComposer()));
		}
		if (criteria.getAlbum() != null) {
			select.append(" inner join music.album tag_album on ft.album_id = tag_album.id");
			where.append(" and tag_album.album_name like upper(?)");
			args.add(sqlContains(criteria.getAlbum()));
		}
		if (criteria.getTitle() != null) {
			select.append(" inner join music.track tag_track on ft.track_id = tag_track.id");
			where.append(" and tag_track.track_name like upper(?)");
			args.add(sqlContains(criteria.getTitle()));
		}
		if (criteria.getTrackNrFrom() != null) {
			where.append(" and ft.track_nr >= " + criteria.getTrackNrFrom());
		}
		if (criteria.getTrackNrTo() != null) {
			where.append(" and ft.track_nr <= " + criteria.getTrackNrTo());
		}
		if (criteria.getDiscNrFrom() != null) {
			where.append(" and ft.disc_nr >= " + criteria.getDiscNrFrom());
		}
		if (criteria.getDiscNrTo() != null) {
			where.append(" and ft.disc_nr <= " + criteria.getDiscNrTo());
		}
		if (criteria.getYearFrom() != null) {
			where.append(" and ft.year >= " + criteria.getYearFrom());
		}
		if (criteria.getYearTo() != null) {
			where.append(" and ft.year <= " + criteria.getYearTo());
		}
		if (criteria.getTrackGenre() != null) {
			select.append(" inner join music.tag tag_tag on ft.tag_id = tag_tag.id");
			where.append(" and tag_tag.tag_name = lower(?)");
			args.add(criteria.getTrackGenre());
		}
	}

	private void addFileHeaderCriteria(StringBuilder select, StringBuilder where, 
			List<Object> args, SearchCriteria criteria) {
		if (criteria.hasFileHeaderCriteria()) {
			select.append(" inner join library.fileheader fh on fh.file_id = lt.file_id");
		}
		if (criteria.getDurationFrom() != null) {
			where.append(" and fh.duration >= " + criteria.getDurationFrom());
		}
		if (criteria.getDurationTo() != null) {
			where.append(" and fh.duration <= " + criteria.getDurationTo());
		}
		if (criteria.getFiletypes() != null && !criteria.getFiletypes().isEmpty()) {
			where.append(format(" and fh.type_id in (%s)", 
					getIdParameters(criteria.getFiletypes())));
		}
	}

	private void addFileCriteria(StringBuilder select, StringBuilder where,
			List<Object> args, SearchCriteria criteria) {
		if (criteria.hasFileCriteria()) {
			select.append(" inner join library.file f on f.id = lt.file_id");
		}
		if (criteria.getModifiedDays() != null) {
			where.append(format(" and age(modified) < '%d days'::interval", 
					criteria.getModifiedDays()));
		}
		if (criteria.getDirectory() != null) {
			select.append(" inner join library.directory d on f.directory_id = d.id");
			where.append(" and upper(d.path) like upper(?)");
			args.add(sqlStartsWith(replace(criteria.getDirectory(), "\\", "\\\\")));
		}
	}

	private void addExternalCriteria(StringBuilder select, StringBuilder where, 
			List<Object> args, SearchCriteria criteria) {
		if (criteria.getSearchQuery() != null) {
			where.append(" and lt.track_name_search like ?");
			args.add(getNameQuery(criteria.getSearchQuery()));
		}
		if (criteria.getArtistGenre() != null) {
			where.append(" and exists (select 1 from music.artisttoptag toptag" 
					+ " inner join music.tag toptagtag on toptag.tag_id = toptagtag.id"
					+ " where toptag.artist_id = ft.artist_id and toptag.tag_count > 25"
					+ " and toptagtag.tag_name = lower(?))");
			args.add(criteria.getArtistGenre());
		}
		if (criteria.getTopTrackRank() != null) {
			where.append(format(" and exists (select 1 from music.artisttoptrack toptrack"
					+ " where toptrack.artist_id = ft.artist_id and toptrack.track_id = ft.track_id"
					+ " and toptrack.rank <= %d)", criteria.getTopTrackRank()));
		}
		if (criteria.isOnlyStarredByUser()) {
			where.append(" and exists (select 1 from library.starredtrack st"
					+ " inner join music.lastfmuser u on st.lastfmuser_id = u.id"
					+ " where u.lastfm_user = upper(?) and st.track_id = ft.track_id)");
			args.add(criteria.getLastFmUsername());
		}
		if (criteria.getPlayedLastDays() != null) {
			where.append(format(" and exists (select 1 from library.playcount pld"
					+ " inner join music.lastfmuser u on pld.lastfmuser_id = u.id"
					+ " where u.lastfm_user = upper(?) and pld.track_id = ft.track_id"
					+ " and age(invocation_time) < '%d days'::interval)", 
					criteria.getPlayedLastDays()));
			args.add(criteria.getLastFmUsername());
		}
		if (criteria.getPlayCountFrom() != null || criteria.getPlayCountTo() != null) {
			select.append(" left outer join (select track_id, count(track_id) as playcount"
					+ " from library.playcount group by track_id) pc on lt.track_id = pc.track_id");
			where.append(format(" and playcount between %d and %d",
					defaultIfNull(criteria.getPlayCountFrom(), 0),
					defaultIfNull(criteria.getPlayCountTo(), Short.MAX_VALUE)));
		}
	}
	
	private String sqlContains(String input) {
		return '%' + input + '%';
	}

	private String sqlStartsWith(String input) {
		return input + '%';
	}
	
	@Override
	public List<String> getFileTypes() {
		String sql = "select extension from library.fileheader_type order by id";
		
		return jdbcTemplate.queryForList(sql, String.class);
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