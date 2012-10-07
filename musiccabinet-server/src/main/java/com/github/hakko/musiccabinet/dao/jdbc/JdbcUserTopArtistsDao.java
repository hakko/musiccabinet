package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.UserTopArtistsDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistRecommendationRowMapper;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.UserTopArtists;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class JdbcUserTopArtistsDao implements UserTopArtistsDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void createUserTopArtists(List<UserTopArtists> userTopArtists) {
		if (userTopArtists.size() > 0) {
			clearImportTable();
			for (UserTopArtists uta : userTopArtists) {
				batchInsert(uta.getArtists(), uta.getUser(), uta.getPeriod());
			}
			updateUserTopArtists();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.usertopartist_import");
	}

	private void batchInsert(List<Artist> artists, LastFmUser user, Period period) {
		String sql = "insert into music.usertopartist_import (lastfm_user, artist_name, rank, days) values (?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("lastfm_user", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("rank", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("days", Types.INTEGER));
		
		for (int i = 0; i < artists.size(); i++) {
			batchUpdate.update(new Object[]{user.getLastFmUsername(), 
					artists.get(i).getName(), i, period.getDays()});
		}
		batchUpdate.flush();
	}

	private void updateUserTopArtists() {
		jdbcTemplate.execute("select library.update_usertopartists()");
	}
	
	@Override
	public List<ArtistRecommendation> getUserTopArtists(LastFmUser user, Period period, int offset, int limit) {
		String sql = "select a.id, a.artist_name_capitalization, ai.largeimageurl"
				+ " from music.artistinfo ai"
				+ " inner join music.artist a on ai.artist_id = a.id"
				+ " inner join music.usertopartist uta on uta.artist_id = a.id"
				+ " inner join music.lastfmuser u on uta.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?) and uta.days = ? and exists"
				+ " (select 1 from library.track lt"
				+ "  inner join music.track mt on lt.track_id = mt.id"
				+ "  where mt.artist_id = a.id)"
				+ " order by rank"
				+ " offset ? limit ?";

		return jdbcTemplate.query(sql,
				new Object[]{user.getLastFmUsername(), period.getDays(), offset, limit},
				new ArtistRecommendationRowMapper());
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