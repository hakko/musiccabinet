package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.jdbc.JdbcNameSearchDao.getNameQuery;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil.getIdParameters;
import static com.github.hakko.musiccabinet.service.MusicBrainzService.TYPE_ALBUM;
import static com.github.hakko.musiccabinet.service.MusicBrainzService.TYPE_EP;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;

import com.github.hakko.musiccabinet.dao.MusicBrainzAlbumDao;
import com.github.hakko.musiccabinet.dao.jdbc.rowmapper.MBAlbumRowMapper;
import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.domain.model.music.MBRelease;
import com.github.hakko.musiccabinet.log.Logger;

public class JdbcMusicBrainzAlbumDao implements MusicBrainzAlbumDao, JdbcTemplateDao {

	private JdbcTemplate jdbcTemplate;

	private static final Logger LOG = Logger.getLogger(JdbcMusicBrainzAlbumDao.class);
	
	@Override
	public boolean hasDiscography() {
		String sql = "select exists(select 1 from music.mb_album)";
		return jdbcTemplate.queryForObject(sql, Boolean.class);
	}

	@Override
	public void createAlbums(List<MBRelease> releases) {
		if (releases.size() > 0) {
			clearImportTable();
			batchInsert(releases);
			updateLibrary();
		}
	}
	
	private void clearImportTable() {
		jdbcTemplate.execute("truncate music.mb_album_import");
	}
	
	private void batchInsert(List<MBRelease> releases) {
		String sql = "insert into music.mb_album_import"
				+ " (artist_id, title, type_id, release_year, label_name,"
				+ " label_mbid, format, release_group_mbid) values (?,?,?,?,?,?,?,?)";
		BatchSqlUpdate batchUpdate = new BatchSqlUpdate(jdbcTemplate.getDataSource(), sql);
		batchUpdate.setBatchSize(1000);
		batchUpdate.declareParameter(new SqlParameter("artist_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("title", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("type_id", Types.INTEGER));
		batchUpdate.declareParameter(new SqlParameter("release_year", Types.SMALLINT));
		batchUpdate.declareParameter(new SqlParameter("label_name", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("label_mbid", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("format", Types.VARCHAR));
		batchUpdate.declareParameter(new SqlParameter("release_group_mbid", Types.VARCHAR));
		
		for (MBRelease r : releases) {
			if (r.isValid()) {
				batchUpdate.update(new Object[]{r.getArtistId(), r.getTitle(),
					r.getAlbumType().ordinal(), r.getReleaseYear(), r.getLabelName(), 
					r.getLabelMbid(), r.getFormat(), r.getReleaseGroupMbid()});
			} else {
				LOG.warn("Invalid MusicBrainz release ignored: " + r);
			}
		}
		batchUpdate.flush();

	}

	private void updateLibrary() {
		jdbcTemplate.execute("select music.update_mbalbum()");
	}
	
	@Override
	public List<MBAlbum> getAlbums(int artistId) {
		return jdbcTemplate.query(
				"select art.artist_name_capitalization, alb.album_name_capitalization,"
				+ " mba.first_release_year, mba.type_id from music.mb_album mba"
				+ " inner join music.album alb on mba.album_id = alb.id"
				+ " inner join music.artist art on alb.artist_id = art.id"
				+ " where art.id = " + artistId + " order by mba.first_release_year", 
				new MBAlbumRowMapper());
	}

	@Override
	public List<MBAlbum> getMissingAlbums(String artistName, List<Integer> albumTypes,
			String lastFmUsername, int playedWithinLastDays, int offset) {
		List<Object> params = new ArrayList<>();
		
		if (albumTypes == null || albumTypes.isEmpty()) {
			albumTypes = Arrays.asList(TYPE_ALBUM, TYPE_EP);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select art.artist_name_capitalization, alb.album_name_capitalization,"
				+ " mba.first_release_year, mba.type_id from music.mb_album mba"
				+ " inner join music.album alb on mba.album_id = alb.id"
				+ " inner join music.artist art on alb.artist_id = art.id"
				+ " where not exists (select 1 from library.album where album_id = mba.album_id)");
		
		if (isNotEmpty(artistName)) {
			sb.append(" and art.id in (select artist_id from library.artist"
					+ " where artist_name_search like ?)");
			params.add(getNameQuery(artistName));
		}
		
		if (playedWithinLastDays > 0) {
			sb.append(String.format(
					" and art.id in (select artist_id from library.playcount pc"
					+ " inner join music.lastfmuser u on pc.lastfmuser_id = u.id"
					+ " where age(invocation_time) < '%d days'::interval and u.lastfm_user = upper(?))",
					playedWithinLastDays));
			params.add(lastFmUsername);
		}
		
		sb.append(" and mba.type_id in (" + getIdParameters(albumTypes) + ")");
		sb.append(" order by art.artist_name, mba.first_release_year offset " + offset + " limit 101");
		
		return jdbcTemplate.query(sb.toString(), params.toArray(), new MBAlbumRowMapper());
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