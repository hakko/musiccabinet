package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.UserRecommendedArtistsDao;
import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists;
import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists.RecommendedArtist;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class JdbcUserRecommendedArtistsDao implements UserRecommendedArtistsDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void createUserRecommendedArtists(List<UserRecommendedArtists> userRecommendedArtists) {
		if (userRecommendedArtists.size() > 0) {
			clearImportTable();
			for (UserRecommendedArtists uta : userRecommendedArtists) {
				batchInsert(uta.getArtists(), uta.getUser());
			}
			updateUserTopArtists();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.userrecommendedartist_import");
	}

	private void batchInsert(List<RecommendedArtist> artists, LastFmUser user) {
		String sql = "insert into music.userrecommendedartist_import"
				+ " (lastfm_user, artist_name, rank, contextartist1_name, contextartist2_name)"
				+ " values (?,?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("lastfm_user", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("rank", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("contextartist1_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("contextartist2_name", Types.VARCHAR));
		
		for (int i = 0; i < artists.size(); i++) {
			RecommendedArtist rec = artists.get(i);
			batchUpdate.update(new Object[]{user.getLastFmUsername(), rec.getArtist().getName(), 
					i, name(rec.getContextArtist1()), name(rec.getContextArtist2())});
		}
		batchUpdate.flush();
	}
	
	private String name(Artist artist) {
		return artist == null ? null : artist.getName();
	}

	private void updateUserTopArtists() {
		jdbcTemplate.execute("select library.update_userrecartists()");
	}
		
	@Override
	public List<RecommendedArtist> getUserRecommendedArtists(String lastFmUsername) {
		String sql = "select a.artist_name_capitalization,"
				+ " ca1.artist_name_capitalization, ca2.artist_name_capitalization"
				+ " from music.userrecommendedartist ura"
				+ " inner join music.artist a on ura.artist_id = a.id"
				+ " inner join music.artist ca1 on ura.contextartist1_id = ca1.id"
				+ " inner join music.artist ca2 on ura.contextartist2_id = ca2.id"
				+ " inner join music.lastfmuser u on ura.lastfmuser_id = u.id"
				+ " where u.lastfm_user = upper(?)"
				+ " order by rank";

		return jdbcTemplate.query(sql, new Object[]{lastFmUsername}, new RowMapper<RecommendedArtist>() {
			@Override
			public RecommendedArtist mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new RecommendedArtist(rs.getString(1), rs.getString(2), rs.getString(3));
			}
		});
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