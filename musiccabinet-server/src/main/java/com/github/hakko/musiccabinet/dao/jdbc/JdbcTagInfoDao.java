package com.github.hakko.musiccabinet.dao.jdbc;

import static java.sql.Types.VARCHAR;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.TagInfoDao;
import com.github.hakko.musiccabinet.domain.model.music.TagInfo;

public class JdbcTagInfoDao implements TagInfoDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void createTagInfo(List<TagInfo> tagInfos) {
		String sql = "insert into music.taginfo_import (tag_name, summary, content) values (?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("tag_name", VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("summary", VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("content", VARCHAR));
		
		for (TagInfo ti : tagInfos) {
			batchUpdate.update(new Object[]{ti.getTagName(), 
					ti.getSummary(), ti.getContent()});
		}
		batchUpdate.flush();

		jdbcTemplate.execute("select music.update_taginfo()");
	}

	@Override
	public String getTagInfo(String tagName) {
		String sql = "select ti.summary from music.tag t"
				+ " left outer join music.taginfo ti on ti.tag_id = t.id"
				+ " where t.tag_name = ?";

		return jdbcTemplate.queryForObject(sql, new Object[]{tagName}, String.class);
	}

	@Override
	public List<String> getTagsWithInfo() {
		String sql = "select t.tag_name from music.taginfo ti"
				+ " inner join music.tag t on ti.tag_id = t.id";
		return jdbcTemplate.queryForList(sql, String.class);
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