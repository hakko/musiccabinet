package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.UserLovedTracksDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.TrackWithArtistRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.UserStarredTrackRowMapper;
import com.github.hakko.musiccabinet.domain.model.aggr.UserLovedTracks;
import com.github.hakko.musiccabinet.domain.model.aggr.UserStarredTrack;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcUserLovedTracksDao implements UserLovedTracksDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void createLovedTracks(List<UserLovedTracks> userLovedTracks) {
		if (userLovedTracks.size() > 0) {
			clearImportTable();
			for (UserLovedTracks ult : userLovedTracks) {
				batchInsert(ult.getLastFmUsername(), ult.getLovedTracks());
			}
			updateLibrary();
		}
	}
	
	private void batchInsert(String lastFmUsername, List<Track> lovedTracks) {
		String sql = "insert into music.lovedtrack_import"
				+ " (lastfm_user, artist_name, track_name) values (?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("lastfm_user", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("track_name", Types.VARCHAR));
		
		for (Track track : lovedTracks) {
			batchUpdate.update(new Object[]{lastFmUsername, 
					track.getArtist().getName(), track.getName()});
		}
		batchUpdate.flush();
	}

	private void updateLibrary() {
		jdbcTemplate.execute("select music.update_userlovedtracks()");
	}

	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.lovedtrack_import");
	}

	@Override
	public List<Track> getLovedTracks(String lastFmUsername) {
		String sql = "select a.artist_name_capitalization, t.track_name_capitalization"
				+ " from music.lovedtrack lt"
				+ " inner join music.track t on lt.track_id = t.id"
				+ " inner join music.artist a on t.artist_id = a.id"
				+ " inner join music.lastfmuser u on lt.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)";

		return jdbcTemplate.query(sql, new Object[]{lastFmUsername}, 
				new TrackWithArtistRowMapper());
	}

	@Override
	public List<UserStarredTrack> getStarredButNotLovedTracks() {
		String sql = "select u.lastfm_user_capitalization, u.session_key,"
				+ " a.artist_name_capitalization, t.track_name_capitalization"
				+ " from library.starredtrack st"
				+ " inner join music.lastfmuser u on st.lastfmuser_id = u.id"
				+ " inner join music.track t on st.track_id = t.id"
				+ " inner join music.artist a on t.artist_id = a.id"
				+ " where not exists (select 1 from music.lovedtrack lt"
				+ "	 where lt.lastfmuser_id = st.lastfmuser_id and lt.track_id = st.track_id)";

		return jdbcTemplate.query(sql, new UserStarredTrackRowMapper());
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