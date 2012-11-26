package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.MB_ARTIST_QUERY;
import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.MB_RELEASE_GROUPS;

import java.sql.Types;
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
			"select a.id, a.artist_name_capitalization, mba.mbid, mba.country_code,"
			+ " mba.start_year, mba.active from music.mb_artist mba"
			+ " inner join music.artist a on mba.artist_id = a.id"
			+ " where a.id = " + artistId, new MBArtistRowMapper());
	}
	
	@Override
	public int getMissingAndOutdatedArtistsCount() {
		int missingArtists = jdbcTemplate.queryForInt(
			"select count(*) from library.artist la"
			+ " inner join music.artist ma on la.artist_id = ma.id"
			+ " where la.hasalbums and ma.artist_name != 'VARIOUS ARTISTS'"
			+ " and not exists (select 1 from music.mb_artist where artist_id = ma.id)");

		int outdatedArtists = jdbcTemplate.queryForInt(String.format(
			"select count(*) from music.mb_artist mba"
			+ " inner join music.artist ma on mba.artist_id = ma.id"
			+ " left outer join library.webservice_history h on h.artist_id = ma.id"
			+ " and h.calltype_id = %d where "
			+ " age(coalesce(invocation_time, to_timestamp(0))) > '%d days'::interval",
			MB_RELEASE_GROUPS.getDatabaseId(), MB_RELEASE_GROUPS.getDaysToCache()));
		
		return missingArtists + outdatedArtists;
	}

	@Override
	public List<Artist> getMissingArtists() {
		return jdbcTemplate.query(
				"select ma.id, ma.artist_name_capitalization from library.artist la"
				+ " inner join music.artist ma on la.artist_id = ma.id where hasalbums"
				+ " and not exists (select 1 from music.mb_artist mba where mba.artist_id = ma.id)"
				+ " and not exists (select 1 from library.webservice_history h where h.artist_id = ma.id"
				+ "  and h.calltype_id = " + MB_ARTIST_QUERY.getDatabaseId() + ")"
				+ " and ma.artist_name != 'VARIOUS ARTISTS' order by ma.artist_name limit 3000",
				new ArtistRowMapper());
	}

	@Override
	public List<MBArtist> getOutdatedArtists() {
		return jdbcTemplate.query(String.format(
				"select ma.id, ma.artist_name_capitalization, mba.mbid, null, null, null"
			+ " from music.mb_artist mba inner join music.artist ma on mba.artist_id = ma.id"
			+ " left outer join library.webservice_history h on h.artist_id = ma.id"
			+ " and h.calltype_id = %d where "
			+ " age(coalesce(invocation_time, to_timestamp(0))) > '%d days'::interval"
			+ " limit 3000",
			MB_RELEASE_GROUPS.getDatabaseId(), MB_RELEASE_GROUPS.getDaysToCache()),
			new MBArtistRowMapper());
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