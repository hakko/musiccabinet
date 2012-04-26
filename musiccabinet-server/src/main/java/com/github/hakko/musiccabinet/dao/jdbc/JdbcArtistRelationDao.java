package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_SIMILAR;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.ArtistRelationDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;

public class JdbcArtistRelationDao implements ArtistRelationDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public void createArtistRelations(Artist sourceArtist, List<ArtistRelation> artistRelations) {
		if (artistRelations.size() > 0) {
			clearImportTable();
			batchInsert(sourceArtist, artistRelations);
			updateLibrary();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("delete from music.artistrelation_import");
	}

	private void batchInsert(Artist sourceArtist, List<ArtistRelation> ArtistRelations) {
		int sourceArtistId = jdbcTemplate.queryForInt("select * from music.get_artist_id(?)",
				sourceArtist.getName());
		
		String sql = "insert into music.artistrelation_import (source_id, target_artist_name, weight) values (?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("source_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("target_artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("weight", Types.FLOAT));
		
		for (ArtistRelation ar : ArtistRelations) {
			batchUpdate.update(new Object[]{
					sourceArtistId, ar.getTarget().getName(), ar.getMatch()});
		}
		batchUpdate.flush();
	}

	private void updateLibrary() {
		jdbcTemplate.execute("select music.update_artistrelation_from_import()");
	}
	
	@Override
	public List<ArtistRelation> getArtistRelations(Artist sourceArtist) {
		final int sourceArtistId = jdbcTemplate.queryForInt(
				"select * from music.get_artist_id(?)", sourceArtist.getName());
		
		String sql = "select artist_name_capitalization, weight"
			+ " from music.artistrelation" 
			+ " inner join music.artist on music.artistrelation.target_id = music.artist.id"
			+ " where music.artistrelation.source_id = ?";
		
		List<ArtistRelation> artistRelations = jdbcTemplate.query(sql, 
				new Object[]{sourceArtistId}, new RowMapper<ArtistRelation>() {
			@Override
			public ArtistRelation mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				String artistName = rs.getString(1);
				float match = rs.getFloat(2);
				return new ArtistRelation(new Artist(artistName), match);
			}
		});
		
		return artistRelations;
	}

	@Override
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	@Override
	public List<Artist> getArtistsWithoutRelations() {
		String sql = "select artist_name_capitalization from music.artist where id in ("
				+ " select distinct t.artist_id from library.musicfile mf"
				+ " inner join music.track t on mf.track_id = t.id"
				+ " where not exists ("
				+ " select 1 from music.artistrelation where source_id = t.artist_id)"
				+ " and not exists ("
				+ " select 1 from library.webservice_history where artist_id = t.artist_id "
				+ " and calltype_id = " + ARTIST_GET_SIMILAR.getDatabaseId() + "))";
		
		List<Artist> artists = jdbcTemplate.query(sql, new RowMapper<Artist>() {
			@Override
			public Artist mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new Artist(rs.getString(1));
			}
		});
		
		return artists;
	}

}