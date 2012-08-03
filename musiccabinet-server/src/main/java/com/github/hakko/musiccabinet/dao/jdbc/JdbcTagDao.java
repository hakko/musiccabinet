package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil.getParameters;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.TagDao;
import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;

public class JdbcTagDao implements TagDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	public void createTags(List<String> tags) {
		String sql = "insert into music.tag (tag_name) select (?)"
				+ " where not exists (select 1 from music.tag where tag_name = ?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("tag_name", Types.VARCHAR));
		
		for (String tag : tags) {
			batchUpdate.update(new Object[]{tag, tag});
		}
		batchUpdate.flush();
	}
	
	/*
	 * Returns a list of fairly popular and relevant tags that are used for artists
	 * found in library.
	 * 
	 * How it works:
	 * at least (n) artists in library must be tagged with it
	 * the average weight of the tag must be at least (m)
	 * (or)
	 * it must already be used as a top tag.
	 * 
	 * This filters popular tags for the current library, but cuts out tags like
	 * "awesome", "seen live" etc. The latter ones occur frequently, but they normally
	 * have a low rank compared to more descriptive tags such as "pop", "jazz" etc.
	 */
	@Override
	public List<TagOccurrence> getAvailableTags() {
		String sql =
			"select t.tag_name, occ.count, case when tt.tag_id is null then false else true end from music.tag t"
			+ " inner join (select tag_id, count(tag_id) from music.artisttoptag group by tag_id) occ on t.id = occ.tag_id"
			+ " inner join (select tag_id, sum(tag_count) from music.artisttoptag group by tag_id) pop on t.id = pop.tag_id"
			+ " left outer join (select tag_id from library.toptag) tt on t.id = tt.tag_id"
			+ " where (occ.count > 10 and pop.sum/occ.count > 25)"
			+ "  or t.id in (select tag_id from library.toptag)"
			+ " order by t.tag_name";
		
		List<TagOccurrence> availableTags = jdbcTemplate.query(sql, new RowMapper<TagOccurrence>() {
			@Override
			public TagOccurrence mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new TagOccurrence(rs.getString(1), rs.getInt(2), rs.getBoolean(3));
			}
		});
		
		return availableTags;
	}
	
	@Override
	public void setTopTags(final List<String> topTags) {
		assert(topTags != null && topTags.size() > 0);

		jdbcTemplate.update("truncate library.toptag");
		
		String sql = "insert into library.toptag (tag_id)"
			+ " select id from music.tag where tag_name in ("
			+ getParameters(topTags.size()) + ")";
		
		jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int index = 1;
				for (String topTag : topTags) {
					ps.setString(index++, topTag);
				}
			}
		});
	}

	@Override
	public List<String> getTopTags() {
		String sql = 
			"select tag.tag_name from library.toptag tt"
			+ " inner join music.tag tag on tt.tag_id = tag.id"
			+ " order by lower(tag.tag_name)";
		
		return jdbcTemplate.queryForList(sql, String.class);
	}

	/*
	 * Returns top tags (prerequisite), together with an equally distributed weighted
	 * system indicating popularity for each tag.
	 * 
	 * Right now, the popularity comes distributed in the interval 10-40, to support a
	 * tag cloud using the popularity as font size.
	 */
	@Override
	public List<TagOccurrence> getTopTagsOccurrence() {
		String sql = 
				"select tag.tag_name, 10+ntile(30) over (order by pop.sum) from library.toptag tt"
				+ " inner join music.tag tag on tt.tag_id = tag.id"
				+ " inner join (select tag_id, sum(tag_count) from music.artisttoptag att group by tag_id) pop on tag.id = pop.tag_id" 
				+ " order by lower(tag.tag_name)";
			
		List<TagOccurrence> topTags = jdbcTemplate.query(sql, new RowMapper<TagOccurrence>() {
			@Override
			public TagOccurrence mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new TagOccurrence(rs.getString(1), rs.getInt(2), true);
			}
		});

		return topTags;
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