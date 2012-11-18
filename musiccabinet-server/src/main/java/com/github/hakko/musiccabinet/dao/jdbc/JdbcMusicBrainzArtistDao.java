package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.MB_ARTIST_QUERY;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.MusicBrainzArtistDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.ArtistRowMapper;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.MBArtistRowMapper;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.MBArtist;

public class JdbcMusicBrainzArtistDao implements MusicBrainzArtistDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void createArtists(List<MBArtist> artists) {
		if (artists.size() > 0) {
			clearImportTable();
			batchInsert(artists);
			updateLibrary();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.mb_artist_import");
	}
	
	private void batchInsert(List<MBArtist> artists) {
		String sql = "insert into music.mb_artist_import (artist_name, mbid, country_code, start_year, active) values (?,?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("mbid", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("county_code", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("start_year", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("active", Types.BOOLEAN));
		
		for (MBArtist artist : artists) {
			batchUpdate.update(new Object[]{artist.getName(), artist.getMbid(),
					artist.getCountryCode(), artist.getStartYear(), artist.isActive()});
		}
		batchUpdate.flush();

	}

	private void updateLibrary() {
		jdbcTemplate.execute("select music.update_mbartist()");
	}
	
	@Override
	public MBArtist getArtist(int artistId) {
		return jdbcTemplate.queryForObject(
			"select a.artist_name_capitalization, mba.mbid, mba.country_code, mba.start_year, mba.active"
			+ " from music.mb_artist mba"
			+ " inner join music.artist a on mba.artist_id = a.id"
			+ " where a.id = " + artistId, new MBArtistRowMapper());
	}
	
	@Override
	public int getMissingArtistsCount() {
		int missing = jdbcTemplate.queryForInt(
			"select count(*) from library.artist la where hasalbums and not exists ("
			+ " select 1 from music.mb_artist mba where mba.artist_id = la.artist_id)");
		
		int outdated = jdbcTemplate.queryForInt(String.format(
			"select count(*) from library.webservice_history where calltype_id = %d"
			+ " and age(invocation_time) > '%d days'::interval",
			MB_ARTIST_QUERY.getDatabaseId(), MB_ARTIST_QUERY.getDaysToCache()));
		
		return missing + outdated;
	}

	@Override
	public List<Artist> getMissingArtists() {
		List<Artist> missing = jdbcTemplate.query(
				"select ma.id, ma.artist_name_capitalization from library.artist la"
				+ " inner join music.artist ma on la.artist_id = ma.id"
				+ " where hasalbums and not exists ("
				+ " select 1 from music.mb_artist mba where mba.artist_id = la.artist_id)",
				new ArtistRowMapper());
		
		List<Artist> outdated = jdbcTemplate.query(String.format(
				"select ma.id, ma.artist_name_capitalization from library.webservice_history h"
				+ " inner join music.artist ma on h.artist_id = ma.id and h.calltype_id = %d"
				+ " and age(invocation_time) > '%d days'::interval",
				MB_ARTIST_QUERY.getDatabaseId(), MB_ARTIST_QUERY.getDaysToCache()),
				new ArtistRowMapper());

		missing.addAll(outdated);
		Collections.sort(missing);
		return missing;
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