package com.github.hakko.musiccabinet.dao.jdbc;

import static java.lang.String.format;

import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.ArtistTopTagsDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.TagNameCountRowMapper;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;

public class JdbcArtistTopTagsDao implements ArtistTopTagsDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	@Override
	public void createTopTags(Artist artist, List<Tag> tags) {
		if (tags.size() > 0) {
			clearImportTable();
			batchInsert(artist, tags);
			updateTopTags();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.artisttoptag_import");
	}

	private void batchInsert(Artist artist, List<Tag> tags) {
		int sourceArtistId = jdbcTemplate.queryForInt(
				"select * from music.get_artist_id(?)", artist.getName());
		
		String sql = "insert into music.artisttoptag_import (artist_id, tag_name, tag_count) values (?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("tag_count", Types.SMALLINT));
		
		for (Tag tag : tags) {
			batchUpdate.update(new Object[]{sourceArtistId, tag.getName(), tag.getCount()});
		}
		batchUpdate.flush();
	}

	private void updateTopTags() {
		jdbcTemplate.execute("select music.update_artisttoptag()");
	}
	
	@Override
	public List<Tag> getTopTags(int artistId) {
		String sql = "select tag_name, tag_count"
			+ " from music.artisttoptag att" 
			+ " inner join music.tag tag on att.tag_id = tag.id"
			+ " where att.artist_id = " + artistId + " order by att.tag_count desc";
		
		return jdbcTemplate.query(sql, new TagNameCountRowMapper());
	}

	@Override
	public List<Tag> getTopTags(int artistId, int limit) {
		String sql = "select t.tag_name, att.tag_count"
				+ " from music.artisttoptag att" 
				+ " inner join music.tag t on att.tag_id = t.id"
				+ " inner join library.toptag tt on tt.tag_id = t.id"
				+ " where att.artist_id = " + artistId + " and tag_count > 0"
				+ " order by att.tag_count desc limit " + limit;
			
		return jdbcTemplate.query(sql, new TagNameCountRowMapper());
	}

	@Override
	public void updateTopTag(int artistId, String tagName, int tagCount) {
		int tagId = jdbcTemplate.queryForInt(
				"select id from music.tag where tag_name = ?", tagName);
		
		jdbcTemplate.update(format("update music.artisttoptag att set tag_count = %d"
				+ " from music.tag t where att.tag_id = t.id"
				+ " and att.artist_id = %d and t.id = %d", 
				tagCount, artistId, tagId));

		jdbcTemplate.update(format("insert into music.artisttoptag (artist_id, tag_id, tag_count)"
				+ " select %d, %d, %d where not exists (select 1 from music.artisttoptag "
				+ " where artist_id = %d and tag_id = %d)", 
				artistId, tagId, tagCount, artistId, tagId));
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