package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.ArtistRecommendationDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistRecommendationRowMapper;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

public class JdbcArtistRecommendationDao implements ArtistRecommendationDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public List<ArtistRecommendation> getRecommendedArtistsInLibrary(
			int artistId, int amount, boolean onlyAlbumArtists) {
		String sql = "select ma.id, ma.artist_name_capitalization, ai.largeimageurl"
			+ " from music.artistinfo ai"
			+ " inner join music.artist ma on ai.artist_id = ma.id"
			+ " inner join library.artist la on la.artist_id = ma.id "
			+ " inner join music.artistrelation ar on ar.target_id = ma.id"
			+ " where ar.source_id = " + artistId
			+ (onlyAlbumArtists ? " and la.hasalbums" : "")
			+ " order by weight desc limit " + amount;

		return jdbcTemplate.query(sql, new ArtistRecommendationRowMapper());
	}

	@Override
	public List<String> getRecommendedArtistsNotInLibrary(
			int artistId, int amount, boolean onlyAlbumArtists) {
		String sql = "select a.artist_name_capitalization from music.artistrelation ar"
			+ " inner join music.artist a on ar.target_id = a.id"
			+ " where ar.source_id = " + artistId + " and not exists"
			+ " (select 1 from library.artist where artist_id = ar.target_id"
			+ (onlyAlbumArtists ? " and hasalbums" : "")
			+ " ) order by ar.weight desc limit " + amount;

		return jdbcTemplate.queryForList(sql, String.class);
	}

	@Override
	public int getNumberOfRelatedSongs(int artistId) {
		String sql = "select count(*) from library.artisttoptrackplaycount attpc"
			+ " inner join music.artistrelation ar"
			+ " on attpc.artist_id = ar.target_id and ar.source_id = " + artistId;
		
		return jdbcTemplate.queryForInt(sql);
	}

	@Override
	public List<ArtistRecommendation> getRecommendedArtistsFromGenre(
			String tagName, int offset, int length, boolean onlyAlbumArtists) {
		String sql = "select ma.id, ma.artist_name_capitalization, ai.largeimageurl"
				+ " from music.artistinfo ai"
				+ " inner join music.artist ma on ai.artist_id = ma.id"
				+ " inner join library.artist la on la.artist_id = ma.id "
				+ " inner join music.artisttoptag att on att.artist_id = ma.id"
				+ " inner join music.tag t on t.id = att.tag_id"
				+ " where t.tag_name = ? and att.tag_count > 25"
				+ (onlyAlbumArtists ? " and la.hasalbums" : "")
				+ " order by (att.tag_count-1)/10 desc, ai.listeners desc"
				+ " limit " + length + " offset " + offset;

		return jdbcTemplate.query(sql, new Object[]{tagName}, new ArtistRecommendationRowMapper());
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