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
	public List<ArtistRecommendation> getRelatedArtistsInLibrary(
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
	public List<String> getRelatedArtistsNotInLibrary(
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
	public List<ArtistRecommendation> getGenreArtistsInLibrary(
			String tagName, int offset, int length, boolean onlyAlbumArtists) {
		String sql = "select ma.id, ma.artist_name_capitalization, ai.largeimageurl"
				+ " from music.artistinfo ai"
				+ " inner join music.artist ma on ai.artist_id = ma.id"
				+ " inner join library.artist la on la.artist_id = ma.id "
				+ " inner join ("
				+ " select artist_id, max(tag_count) as tag_count from music.artisttoptag att"
				+ " inner join music.tag t on att.tag_id = t.id"
				+ " where coalesce(corrected_id, id) in "
				+ " (select id from music.tag where tag_name = ?)"
				+ " group by att.artist_id, coalesce(t.corrected_id, t.id)"
				+ " ) tag on tag.artist_id = ma.id and tag.tag_count > 25"
				+ (onlyAlbumArtists ? " where la.hasalbums" : "")
				+ " order by (tag.tag_count-1)/10 desc, ai.listeners desc"
				+ " limit " + length + " offset " + offset;

		return jdbcTemplate.query(sql, new Object[]{tagName}, new ArtistRecommendationRowMapper());
	}

	@Override
	public List<String> getGenreArtistsNotInLibrary(
			String tagName, int amount, boolean onlyAlbumArtists) {
		String sql = "select a.artist_name_capitalization from music.tagtopartist tta"
			+ " inner join music.artist a on tta.artist_id = a.id"
			+ " inner join music.tag t on tta.tag_id = t.id"
			+ " where t.tag_name = ? and not exists"
			+ " (select 1 from library.artist where artist_id = tta.artist_id"
			+ (onlyAlbumArtists ? " and hasalbums" : "")
			+ " ) order by tta.rank asc limit " + amount;

		return jdbcTemplate.queryForList(sql, new Object[]{tagName}, String.class);
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