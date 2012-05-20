package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.UserTopArtistsDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.UserTopArtists;
import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class JdbcUserTopArtistsDao implements UserTopArtistsDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

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
		jdbcTemplate.execute("delete from library.usertopartist_import");
	}

	private void batchInsert(List<Artist> artists, LastFmUser user, Period period) {
		String sql = "insert into library.usertopartist_import (lastfm_user, artist_name, rank, days) values (?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("lastfm_user", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("rank", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("days", Types.INTEGER));
		
		for (int i = 0; i < artists.size(); i++) {
			batchUpdate.update(new Object[]{user.getLastFmUser(), 
					artists.get(i).getName(), i, period.getDays()});
		}
		batchUpdate.flush();
	}

	private void updateUserTopArtists() {
		jdbcTemplate.execute("select library.update_usertopartists()");
	}
	
	@Override
	public List<ArtistRecommendation> getUserTopArtists(LastFmUser user, Period period, int offset, int limit) {
		String sql = 
				"select a.artist_name_capitalization, ai.largeimageurl, md.paths"
				+ " from library.usertopartist uta"
				+ " inner join library.lastfmuser u on uta.lastfmuser_id = u.id"
				+ " inner join music.artist a on uta.artist_id = a.id"
				+ " inner join music.artistinfo ai on a.id = ai.artist_id"
				+ " inner join (select artist_id, array_agg(path) as paths"
				+ "		from library.musicdirectory"
				+ "		where album_id is null group by artist_id) md"
				+ "		on a.id = md.artist_id"
				+ " where u.lastfm_user = upper(?) and uta.days = ?"
				+ " order by rank"
				+ " offset ? limit ?";

		List<ArtistRecommendation> artistRecommendations = jdbcTemplate.query(sql,
				new Object[]{user.getLastFmUser(), period.getDays(), offset, limit},
				new RowMapper<ArtistRecommendation>() {
			@Override
			public ArtistRecommendation mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				String artistName = rs.getString(1);
				String imageUrl = rs.getString(2);
				List<String> paths = Arrays.asList((String[]) rs.getArray(3).getArray());
				return new ArtistRecommendation(artistName, imageUrl, paths);
			}
		});

		return artistRecommendations;
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

}