package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.jdbc.JdbcNameSearchDao.getNameQuery;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil.getIdParameters;
import static java.io.File.separatorChar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.AlbumRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistRecommendationRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistRowMapper;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.library.MetaData.Mediatype;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcLibraryBrowserDao implements LibraryBrowserDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	@Override
	public boolean hasArtists() {
		String sql = "select exists(select 1 from library.artist)";
		
		try {
			return jdbcTemplate.queryForObject(sql, Boolean.class);
		} catch (DataAccessException e) {
			return false; // database password not supplied etc
		}
	}
	
	@Override
	public List<Artist> getArtists() {
		String sql = "select ma.id, ma.artist_name_capitalization from music.artist ma"
				+ " inner join library.artist la on la.artist_id = ma.id where la.hasalbums";
		
		return jdbcTemplate.query(sql, new ArtistRowMapper());
	}

	@Override
	public List<Artist> getArtists(String tag, int treshold) {
		String sql = "select ma.id, ma.artist_name_capitalization from music.artist ma"
				+ " inner join library.artist la on la.artist_id = ma.id"
				+ " where la.hasalbums and exists (select 1 from"
				+ " music.artisttoptag att"
				+ " inner join music.tag t on att.tag_id = t.id"
				+ " where att.artist_id = ma.id and att.tag_count > ? and"
				+ " coalesce(t.corrected_id, t.id) in (select id from music.tag where tag_name = ?))";

		return jdbcTemplate.query(sql, new Object[]{treshold, tag}, new ArtistRowMapper());
	}

	@Override
	public List<Artist> getArtists(int indexLetter) {
		String sql = "select ma.id, ma.artist_name_capitalization from music.artist ma"
				+ " inner join library.artist la on la.artist_id = ma.id"
				+ (indexLetter < 'A' || indexLetter > 'Z' ? 
						" where ascii(artist_name) < 65 or ascii(artist_name) > 90" : 
						" where ascii(artist_name) = " + indexLetter)
				+ " and la.hasalbums order by artist_name";

		return jdbcTemplate.query(sql, new ArtistRowMapper());
	}

	public List<ArtistRecommendation> getRecentlyPlayedArtists(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select a.id, a.artist_name_capitalization, ai.largeimageurl"
				+ " from music.artistinfo ai"
				+ " inner join music.artist a on ai.artist_id = a.id"
				+ " inner join library.artist la on la.artist_id = a.id"
				+ " inner join ("
				+ " select artist_id, max(invocation_time) as last_invocation_time"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by artist_id"
				+ ") pc on pc.artist_id = a.id"
				+ (query == null ? "" : " where la.artist_name_search like ?") 
				+ " order by last_invocation_time desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUsername, offset, limit} :
				new Object[]{lastFmUsername, getNameQuery(query), offset, limit};
		return jdbcTemplate.query(sql, params, new ArtistRecommendationRowMapper());
	}

	public List<ArtistRecommendation> getMostPlayedArtists(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select a.id, a.artist_name_capitalization, ai.largeimageurl"
				+ " from music.artistinfo ai"
				+ " inner join music.artist a on ai.artist_id = a.id"
				+ " inner join library.artist la on la.artist_id = a.id"
				+ " inner join ("
				+ " select artist_id, count(artist_id) as cnt"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " group by artist_id"
				+ ") pc on pc.artist_id = a.id"
				+ (query == null ? "" : " where la.artist_name_search like ?") 
				+ " order by cnt desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUsername, offset, limit} :
				new Object[]{lastFmUsername, getNameQuery(query), offset, limit};
		return jdbcTemplate.query(sql, params, new ArtistRecommendationRowMapper());
	}

	public List<ArtistRecommendation> getRandomArtists(int limit) {
		String sql = "select a.id, a.artist_name_capitalization, ai.largeimageurl"
				+ " from music.artistinfo ai"
				+ " inner join music.artist a on a.id = ai.artist_id"
				+ " inner join library.artist la on la.artist_id = a.id"
				+ " order by random() limit " + limit;
		
		return jdbcTemplate.query(sql, new ArtistRecommendationRowMapper());
	}

	public List<ArtistRecommendation> getStarredArtists(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select a.id, a.artist_name_capitalization, ai.largeimageurl"
				+ " from music.artistinfo ai"
				+ " inner join music.artist a on ai.artist_id = a.id"
				+ " inner join library.artist la on la.artist_id = a.id"
				+ " inner join library.starredartist sa on sa.artist_id = la.artist_id"
				+ " inner join music.lastfmuser u on sa.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ (query == null ? "" : " and la.artist_name_search like ?") 
				+ " order by sa.added desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUsername, offset, limit} :
				new Object[]{lastFmUsername, getNameQuery(query), offset, limit};
		return jdbcTemplate.query(sql, params, new ArtistRecommendationRowMapper());
	}

	@Override
	public Album getAlbum(int albumId) {
		String sql = "select ma.artist_id, null, ma.id, ma.album_name_capitalization, la.year,"
				+ " f1.path, f1.filename, f2.path, f2.filename, ai.largeimageurl, lt.track_ids"
				+ " from music.album ma"
				+ " inner join library.album la on la.album_id = ma.id "
				+ " inner join (select la2.album_id as album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids"
				+ "		from library.album la2"
				+ "		inner join library.track lt on lt.album_id = la2.album_id"
				+ "     inner join library.filetag ft on ft.file_id = lt.file_id"
				+ "     group by la2.album_id) lt"
				+ "		on lt.album_id = la.album_id"
				+ " left outer join (select f.id, f.filename, d.path from library.file f inner join library.directory d on f.directory_id = d.id) f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join (select f.id, f.filename, d.path from library.file f inner join library.directory d on f.directory_id = d.id) f2 on f2.id = la.coverartfile_id"
				+ " left outer join music.albuminfo ai on la.album_id = ai.album_id"
				+ " where la.album_id = " + albumId;
		
		return jdbcTemplate.queryForObject(sql, new AlbumRowMapper());
	}

	@Override
	public List<Album> getAlbums(int artistId, boolean sortAscending) {
		String sql = "select ma.artist_id, a.artist_name_capitalization, ma.id, ma.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, ai.largeimageurl, tr.track_ids from"
				+ " (select lt.album_id as album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids"
				+ " from library.track lt" 
				+ " inner join music.album ma on lt.album_id = ma.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " where ma.artist_id = " + artistId + " or ft.artist_id = " + artistId 
				+ " group by lt.album_id) tr"
				+ " inner join library.album la on la.album_id = tr.album_id"
				+ " inner join music.album ma on la.album_id = ma.id"
				+ " inner join music.artist a on ma.artist_id = a.id"
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " left outer join music.albuminfo ai on ai.album_id = la.album_id"
				+ " order by (ma.artist_id = " + artistId + ") desc," 
				+ " la.year " + (sortAscending ? "asc" : "desc");

		return jdbcTemplate.query(sql, new AlbumRowMapper());
	}

	@Override
	public List<Album> getRecentlyAddedAlbums(int offset, int limit, String query) {
		String sql = "select ma.artist_id, a.artist_name_capitalization, ma.id, ma.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, ai.largeimageurl, tr.track_ids from"
				+ " (select lt.album_id as album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids, filter.sort_id"
				+ " from library.track lt"
				+ " inner join music.album ma on lt.album_id = ma.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " inner join (select la.album_id, la.id as sort_id "
				+ "  from library.album la "
				+ (query == null ? "" : " where la.album_name_search like ?") 
				+ "  order by la.id desc offset ? limit ?) filter on lt.album_id = filter.album_id"
				+ " group by lt.album_id, filter.sort_id) tr"
				+ " inner join library.album la on la.album_id = tr.album_id"
				+ " inner join music.album ma on la.album_id = ma.id"
				+ " inner join music.artist a on ma.artist_id = a.id"
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " left outer join music.albuminfo ai on ai.album_id = la.album_id"
				+ " order by sort_id desc"; 

		Object[] params = query == null ? 
				new Object[]{offset, limit} : new Object[]{getNameQuery(query), offset, limit};
		return jdbcTemplate.query(sql, params, new AlbumRowMapper());
	}
	
	@Override
	public List<Album> getRecentlyPlayedAlbums(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select ma.artist_id, a.artist_name_capitalization, ma.id, ma.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, ai.largeimageurl, tr.track_ids from"
				+ " (select lt.album_id as album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids, filter.last_invocation_time"
				+ " from library.track lt"
				+ " inner join music.album ma on lt.album_id = ma.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " inner join (select la.album_id, max(invocation_time) as last_invocation_time"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+"  inner join library.album la on pc.album_id = la.album_id"
				+ " where u.lastfm_user = upper(?)"
				+ (query == null ? "" : " and la.album_name_search like ?") 
				+ " group by la.album_id order by last_invocation_time desc offset ? limit ?) filter on lt.album_id = filter.album_id"
				+ " group by lt.album_id, filter.last_invocation_time) tr"
				+ " inner join library.album la on la.album_id = tr.album_id"
				+ " inner join music.album ma on la.album_id = ma.id"
				+ " inner join music.artist a on ma.artist_id = a.id"
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " left outer join music.albuminfo ai on ai.album_id = la.album_id"
				+ " order by last_invocation_time desc"; 

		Object[] params = query == null ? 
				new Object[]{lastFmUsername, offset, limit} : 
				new Object[]{lastFmUsername, getNameQuery(query), offset, limit};

		return jdbcTemplate.query(sql, params, new AlbumRowMapper());
	}

	@Override
	public List<Album> getMostPlayedAlbums(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select ma.artist_id, a.artist_name_capitalization, ma.id, ma.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, ai.largeimageurl, tr.track_ids from"
				+ " (select lt.album_id as album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids, filter.cnt"
				+ " from library.track lt"
				+ " inner join music.album ma on lt.album_id = ma.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " inner join (select la.album_id, count(la.album_id) as cnt"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+"  inner join library.album la on pc.album_id = la.album_id"
				+ " where u.lastfm_user = upper(?)"
				+ (query == null ? "" : " and la.album_name_search like ?") 
				+ " group by la.album_id order by cnt desc offset ? limit ?) filter on lt.album_id = filter.album_id"
				+ " group by lt.album_id, filter.cnt) tr"
				+ " inner join library.album la on la.album_id = tr.album_id"
				+ " inner join music.album ma on la.album_id = ma.id"
				+ " inner join music.artist a on ma.artist_id = a.id"
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " left outer join music.albuminfo ai on ai.album_id = la.album_id"
				+ " order by cnt desc"; 

		Object[] params = query == null ? 
				new Object[]{lastFmUsername, offset, limit} : 
				new Object[]{lastFmUsername, getNameQuery(query), offset, limit};

		return jdbcTemplate.query(sql, params, new AlbumRowMapper());
	}

	@Override
	public List<Album> getRandomAlbums(int limit) {
		String sql = "select ma.artist_id, a.artist_name_capitalization, ma.id, ma.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, ai.largeimageurl, tr.track_ids from"
				+ " (select lt.album_id as album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids"
				+ " from library.track lt" 
				+ " inner join music.album ma on lt.album_id = ma.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " inner join (select album_id from library.album la order by random() limit " 
				+ limit + ") la on la.album_id = ma.id"
				+ " group by lt.album_id) tr"
				+ " inner join library.album la on la.album_id = tr.album_id"
				+ " inner join music.album ma on la.album_id = ma.id"
				+ " inner join music.artist a on ma.artist_id = a.id"
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " left outer join music.albuminfo ai on ai.album_id = la.album_id";

		return jdbcTemplate.query(sql, new AlbumRowMapper());
	}
	
	@Override
	public List<Album> getStarredAlbums(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select ma.artist_id, a.artist_name_capitalization, ma.id, ma.album_name_capitalization, la.year,"
				+ " d1.path, f1.filename, d2.path, f2.filename, ai.largeimageurl, tr.track_ids from"
				+ " (select lt.album_id as album_id, array_agg(lt.id order by coalesce(ft.disc_nr, 1)*100 + coalesce(ft.track_nr, 0)) as track_ids, filter.added"
				+ " from library.track lt"
				+ " inner join music.album ma on lt.album_id = ma.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " inner join (select sa.album_id, sa.added from library.starredalbum sa "
				+ " inner join library.album la on sa.album_id = la.album_id"
				+ " inner join music.lastfmuser u on sa.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ (query == null ? "" : " and la.album_name_search like ?") 
				+ " order by sa.added desc offset ? limit ?) filter on lt.album_id = filter.album_id"
				+ " group by lt.album_id, filter.added) tr"
				+ " inner join library.album la on la.album_id = tr.album_id"
				+ " inner join music.album ma on la.album_id = ma.id"
				+ " inner join music.artist a on ma.artist_id = a.id"
				+ " left outer join library.file f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join library.directory d1 on f1.directory_id = d1.id"
				+ " left outer join library.file f2 on f2.id = la.coverartfile_id"
				+ " left outer join library.directory d2 on f2.directory_id = d2.id"
				+ " left outer join music.albuminfo ai on ai.album_id = la.album_id"
				+ " order by added desc"; 

		Object[] params = query == null ? 
				new Object[]{lastFmUsername, offset, limit} : 
				new Object[]{lastFmUsername, getNameQuery(query), offset, limit};

		return jdbcTemplate.query(sql, params, new AlbumRowMapper());
	}

	private String getFileName(String directory, String filename) {
		return directory == null || filename == null ? null : 
			directory + separatorChar + filename;
	}

	@Override
	public List<Track> getTracks(List<Integer> trackIds) {
		String sql = "select mt.track_name_capitalization, "
				+ " mal.album_name_capitalization,"
				+ " ma.artist_name_capitalization,"
				+ " ft.track_nr, ft.track_nrs, ft.disc_nr, ft.disc_nrs, ft.year,"
				+ " fh.bitrate, fh.vbr, fh.duration, fh.type_id, "
				+ " d.path, f.filename, f.size, f.modified, lt.id, mal.id, ma.id"
				+ " from music.track mt"
				+ " inner join library.track lt on lt.track_id = mt.id"
				+ " inner join library.file f on f.id = lt.file_id"
				+ " inner join library.directory d on f.directory_id = d.id"
				+ " inner join library.filetag ft on ft.file_id = lt.file_id"
				+ " inner join library.fileheader fh on fh.file_id = lt.file_id"
				+ " inner join music.artist ma on ft.artist_id = ma.id"
				+ " inner join music.album mal on lt.album_id = mal.id"
				+ " where lt.id in (" + getIdParameters(trackIds) + ")";
		
		return jdbcTemplate.query(sql, new RowMapper<Track>() {
			@Override
			public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
				String trackName = rs.getString(1);
				MetaData md = new MetaData();
				md.setAlbum(rs.getString(2));
				md.setArtist(rs.getString(3));
				md.setTrackNr(rs.getShort(4));
				md.setTrackNrs(rs.getShort(5));
				md.setDiscNr(rs.getShort(6));
				md.setDiscNrs(rs.getShort(7));
				md.setYear(rs.getShort(8));
				md.setBitrate(rs.getShort(9));
				md.setVbr(rs.getBoolean(10));
				md.setDuration(rs.getShort(11));
				md.setMediaType(Mediatype.values()[rs.getShort(12)]);
				md.setPath(rs.getString(13) + separatorChar + rs.getString(14));
				md.setSize(rs.getInt(15));
				md.setModified(rs.getTimestamp(16).getTime());
				int trackId = rs.getInt(17);
				md.setAlbumId(rs.getInt(18));
				md.setArtistId(rs.getInt(19));
				return new Track(trackId, trackName, md);
			}
		});
	}
	
	@Override
	public List<Integer> getRecentlyPlayedTrackIds(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select lt.id from ("
				+ " select track_id, album_id, max(invocation_time) as last_invocation_time"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?) group by track_id, album_id"
				+ ") pc inner join library.track lt"
				+ " on lt.track_id = pc.track_id and lt.album_id = pc.album_id"
				+ (query == null ? "" : " where lt.track_name_search like ?") 
				+ " order by last_invocation_time desc offset ? limit ?";

		Object[] params = query == null ?
				new Object[]{lastFmUsername, offset, limit} :
				new Object[]{lastFmUsername, getNameQuery(query), offset, limit};
		return jdbcTemplate.queryForList(sql, params, Integer.class);
	}

	@Override
	public List<Integer> getMostPlayedTrackIds(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select lt.id from ("
				+ " select track_id, album_id, count(track_id) as cnt"
				+ " from library.playcount pc"
				+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?) group by track_id, album_id"
				+ ") pc inner join library.track lt"
				+ " on lt.track_id = pc.track_id and lt.album_id = pc.album_id"
				+ (query == null ? "" : " where lt.track_name_search like ?") 
				+ " order by cnt desc offset ? limit ?";

		Object[] params = query == null ?
				new Object[]{lastFmUsername, offset, limit} :
				new Object[]{lastFmUsername, query, offset, limit};
		return jdbcTemplate.queryForList(sql, params, Integer.class);
	}

	@Override
	public List<Integer> getStarredTrackIds(String lastFmUsername, int offset, int limit, String query) {
		String sql = "select lt.id from library.starredtrack st"
				+ " inner join library.track lt on st.album_id = lt.album_id and st.track_id = lt.track_id"
				+ " inner join music.lastfmuser u on st.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ (query == null ? "" : " and lt.track_name_search like ?")
				+ " order by added desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUsername, offset, limit} : 
				new Object[]{lastFmUsername, getNameQuery(query), offset, limit};
		return jdbcTemplate.queryForList(sql, params, Integer.class);
	}
	
	@Override
	public List<Integer> getRandomTrackIds(int limit) {
		String sql = "select id from library.track order by random() limit " + limit;
		
		return jdbcTemplate.queryForList(sql, Integer.class);
	}

	@Override
	public String getCoverArtFileForTrack(int trackId) {
		String sql = "select f1.path, f1.filename, f2.path, f2.filename"
				+ " from library.track lt"
				+ " inner join music.album ma on lt.album_id = ma.id"
				+ " inner join library.album la on la.album_id = ma.id"
				+ " left outer join (select f.id, f.filename, d.path from library.file f inner join library.directory d on f.directory_id = d.id) f1 on f1.id = la.embeddedcoverartfile_id"
				+ " left outer join (select f.id, f.filename, d.path from library.file f inner join library.directory d on f.directory_id = d.id) f2 on f2.id = la.coverartfile_id"
				+ " where lt.id = " + trackId;

		return jdbcTemplate.queryForObject(sql, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String coverArtFile = getFileName(rs.getString(1), rs.getString(2));
				if (coverArtFile == null) {
					coverArtFile = getFileName(rs.getString(3), rs.getString(4));
				}
				return coverArtFile;
			}
		});
	}

	@Override
	public List<Integer> getArtistIndexes() {
		String sql = "select ascii_code from library.artistindex";

		return jdbcTemplate.queryForList(sql, Integer.class);
	}

	@Override
	public LibraryStatistics getStatistics() {
		String sql = "select artist_count, album_count, track_count, bytes, seconds"
				+ " from library.statistics";
		
		return jdbcTemplate.queryForObject(sql, new RowMapper<LibraryStatistics>() {
			@Override
			public LibraryStatistics mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				int artistCount = rs.getInt(1);
				int albumCount = rs.getInt(2);
				int trackCount = rs.getInt(3);
				long bytes = rs.getLong(4);
				int seconds = rs.getInt(5);
				return new LibraryStatistics(artistCount, albumCount, 
						trackCount, bytes, seconds);
			}
		});
	}

	@Override
	public int getTrackId(String absolutePath) {
		String sql = "select lt.id from library.file f"
		+ " inner join library.directory d on f.directory_id = d.id"
		+ " inner join library.track lt on lt.file_id = f.id"
		+ " where d.path = ? and f.filename = ?";
		
		String directory = FilenameUtils.getFullPathNoEndSeparator(absolutePath);
		String filename = FilenameUtils.getName(absolutePath);

		try {
			return jdbcTemplate.queryForInt(sql, directory, filename);
		} catch (DataAccessException e) {
			return getCaseInsensitiveTrackId(absolutePath);
		}
	}

	/*
	 * Fix for platforms like Windows, that alternates between using C: and c:.
	 */
	private int getCaseInsensitiveTrackId(String absolutePath) {
		String sql = "select lt.id from library.file f"
		+ " inner join library.directory d on f.directory_id = d.id"
		+ " inner join library.track lt on lt.file_id = f.id"
		+ " where lower(d.path) = ? and lower(f.filename) = ?";
		
		String directory = FilenameUtils.getFullPathNoEndSeparator(absolutePath.toLowerCase());
		String filename = FilenameUtils.getName(absolutePath.toLowerCase());

		try {
			return jdbcTemplate.queryForInt(sql, directory, filename);
		} catch (DataAccessException e) {
			return -1;
		}
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