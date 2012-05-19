package com.github.hakko.musiccabinet.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.github.hakko.musiccabinet.dao.ArtistRecommendationDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

public class JdbcArtistRecommendationDao implements ArtistRecommendationDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/*
	 * Return top ten artists
	 */
	@Override
	public List<ArtistRecommendation> getRecommendedArtistsInLibrary(int artistId, int amount) {
		String sql = "select a.artist_name_capitalization, ai.largeimageurl, md.paths from music.artistrelation ar"
			+ " inner join music.artist a on ar.target_id = a.id"
			+ " inner join music.artistinfo ai on ar.target_id = ai.artist_id"
			+ " inner join (select artist_id, array_agg(path) as paths"
			+ "		from library.musicdirectory"
			+ "		where album_id is null group by artist_id) md"
			+ "		on ar.target_id = md.artist_id"
			+ " where ar.source_id = " + artistId + " and exists"
			+ " (select 1 from library.artisttoptrackplaycount where artist_id = ar.target_id)"
			+ " order by weight desc limit " + amount;

		List<ArtistRecommendation> inLibrary = jdbcTemplate.query(sql, 
				new RowMapper<ArtistRecommendation>() {
			@Override
			public ArtistRecommendation mapRow(ResultSet rs, int rowNum) throws SQLException {
				String artistName = rs.getString(1);
				String imageUrl = rs.getString(2);
				List<String> paths = Arrays.asList((String[]) rs.getArray(3).getArray());
				return new ArtistRecommendation(artistName, imageUrl, paths);
			}
		});

		return inLibrary;
	}

	/*
	 * Return recommended artists, i.e related artists not found in library.
	 * 
	 * The query might return artists already in library, if we don't have a
	 * single matching track for top tracks of said artist (only other rare tracks).
	 */
	@Override
	public List<String> getRecommendedArtistsNotInLibrary(int artistId, int amount) {
		String sql = "select a.artist_name_capitalization from music.artistrelation ar"
			+ " inner join music.artist a on ar.target_id = a.id"
			+ " where ar.source_id = " + artistId + " and not exists"
			+ " (select 1 from library.artisttoptrackplaycount where artist_id = ar.target_id)"
			+ " order by ar.weight desc limit " + amount;

		List<String> recommended = jdbcTemplate.query(sql, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}
			
		});
		
		return recommended;
	}

	@Override
	public int getNumberOfRelatedSongs(int artistId) {
		String sql = "select count(*) from library.artisttoptrackplaycount attpc"
			+ " inner join music.artistrelation ar"
			+ " on attpc.artist_id = ar.target_id and ar.source_id = " + artistId;
		
		return jdbcTemplate.queryForInt(sql);
	}

	@Override
	public List<ArtistRecommendation> getRecommendedArtistsFromGenre(String tagName, int offset, int length) {
		String sql = 
				"select a.artist_name_capitalization, ai.largeimageurl, md.paths from music.artisttoptag att"
				+ " inner join music.tag t on t.id = att.tag_id"
				+ " inner join music.artist a on a.id = att.artist_id"
				+ " inner join music.artistinfo ai on ai.artist_id = att.artist_id"
				+ " inner join (select artist_id, array_agg(path) as paths"
				+ "		from library.musicdirectory"
				+ "		where album_id is null group by artist_id) md"
				+ "		on att.artist_id = md.artist_id"
				+ " where t.tag_name = ?"
				+ " order by (att.tag_count-1)/10 desc, ai.listeners desc"
				+ " limit " + length + " offset " + offset;

		List<ArtistRecommendation> genreArtists = jdbcTemplate.query(sql, new Object[]{tagName},
				new RowMapper<ArtistRecommendation>() {
			@Override
			public ArtistRecommendation mapRow(ResultSet rs, int rowNum) throws SQLException {
				String artistName = rs.getString(1);
				String imageUrl = rs.getString(2);
				List<String> paths = Arrays.asList((String[]) rs.getArray(3).getArray());
				return new ArtistRecommendation(artistName, imageUrl, paths);
			}
		});
		
		return genreArtists;
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

}