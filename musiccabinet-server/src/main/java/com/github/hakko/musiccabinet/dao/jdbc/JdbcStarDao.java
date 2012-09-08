package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.jdbc.JdbcNameSearchDao.getNameQuery;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.StarDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public class JdbcStarDao implements StarDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void starArtist(LastFmUser lastFmUser, int artistId) {
		String sql = "insert into library.starredartist (lastfmuser_id, artist_id)"
				+ " select ?,? where not exists (select 1 from library.starredartist"
				+ " where lastfmuser_id = ? and artist_id = ?)";

		jdbcTemplate.update(sql, lastFmUser.getId(), artistId, lastFmUser.getId(), artistId);
	}

	@Override
	public void unstarArtist(LastFmUser lastFmUser, int artistId) {
		String sql = "delete from library.starredartist where lastfmuser_id = ? and artist_id = ?";
		
		jdbcTemplate.update(sql, lastFmUser.getId(), artistId);
	}

	@Override
	public List<Integer> getStarredArtistIds(LastFmUser lastFmUser, int offset, int limit, String query) {
		String sql = "select sa.artist_id from library.starredartist sa"
				+ " inner join library.artist la on sa.artist_id = la.artist_id"
				+ " where sa.lastfmuser_id = ?"
				+ (query == null ? "" : " and la.artist_name_search like ?")
				+ " order by added desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUser.getId(), offset, limit} : 
				new Object[]{lastFmUser.getId(), getNameQuery(query), offset, limit};
		return jdbcTemplate.queryForList(sql, params, Integer.class);
	}

	@Override
	public void starAlbum(LastFmUser lastFmUser, int albumId) {
		String sql = "insert into library.starredalbum (lastfmuser_id, album_id)"
				+ " select ?,? where not exists (select 1 from library.starredalbum"
				+ " where lastfmuser_id = ? and album_id = ?)";

		jdbcTemplate.update(sql, lastFmUser.getId(), albumId, lastFmUser.getId(), albumId);
	}

	@Override
	public void unstarAlbum(LastFmUser lastFmUser, int albumId) {
		String sql = "delete from library.starredalbum where lastfmuser_id = ? and album_id = ?";
		
		jdbcTemplate.update(sql, lastFmUser.getId(), albumId);
	}

	@Override
	public List<Integer> getStarredAlbumIds(LastFmUser lastFmUser, int offset, int limit, String query) {
		String sql = "select sa.album_id from library.starredalbum sa"
				+ " inner join library.album la on sa.album_id = la.album_id"
				+ " where sa.lastfmuser_id = ?"
				+ (query == null ? "" : " and la.album_name_search like ?")
				+ " order by added desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUser.getId(), offset, limit} : 
				new Object[]{lastFmUser.getId(), getNameQuery(query), offset, limit};
		return jdbcTemplate.queryForList(sql, params, Integer.class);
	}

	@Override
	public void starTrack(LastFmUser lastFmUser, int trackId) {
		String sql = "insert into library.starredtrack (lastfmuser_id, album_id, track_id)"
				+ " select ?, lt.album_id, lt.track_id from library.track lt"
				+ " where lt.id = ? and not exists ("
				+ " select 1 from library.starredtrack st"
				+ " inner join library.track lt"
				+ "  on st.album_id = lt.album_id and st.track_id = lt.track_id"
				+ "  where st.lastfmuser_id = ? and lt.id = ?)";

		jdbcTemplate.update(sql, lastFmUser.getId(), trackId, lastFmUser.getId(), trackId);
	}

	@Override
	public void unstarTrack(LastFmUser lastFmUser, int trackId) {
		String sql = "delete from library.starredtrack st"
				+ " using library.track lt"
				+ " where lt.album_id = st.album_id and lt.track_id = st.track_id"
				+ " and lt.id = " + trackId + " and lastfmuser_id = " + lastFmUser.getId();
		
		jdbcTemplate.update(sql);
	}

	@Override
	public List<Integer> getStarredTrackIds(LastFmUser lastFmUser, int offset, int limit, String query) {
		String sql = "select lt.id from library.starredtrack st"
				+ " inner join library.track lt on st.album_id = lt.album_id"
				+ "  and st.track_id = lt.track_id"
				+ " where st.lastfmuser_id = ?"
				+ (query == null ? "" : " and lt.track_name_search like ?")
				+ " order by added desc offset ? limit ?";
		
		Object[] params = query == null ? 
				new Object[]{lastFmUser.getId(), offset, limit} : 
				new Object[]{lastFmUser.getId(), getNameQuery(query), offset, limit};
		return jdbcTemplate.queryForList(sql, params, Integer.class);
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