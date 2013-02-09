package com.github.hakko.musiccabinet.dao.jdbc;

import static java.lang.String.format;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.jdbc.core.JdbcTemplate;

import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class JdbcWebserviceHistoryDao implements JdbcTemplateDao, WebserviceHistoryDao {

	private JdbcTemplate jdbcTemplate;
	private MusicDao musicDao;
	private LastFmDao lastFmDao;
	
	@Override
	public void logWebserviceInvocation(WebserviceInvocation wi) {
		logWebserviceInvocation(wi, new Date());
	}

	@Override
	public void quarantineWebserviceInvocation(WebserviceInvocation wi) {
		Date oneMonthFromNow = new DateTime().plusMonths(1).toDate();
		logWebserviceInvocation(wi, oneMonthFromNow);
	}
	
	@Override
	public void blockWebserviceInvocation(int artistId, WebserviceInvocation.Calltype callType) {
		jdbcTemplate.execute(format("select library.block_webservice(%d, %d)", 
				artistId, callType.getDatabaseId()));
	}

	private void logWebserviceInvocation(WebserviceInvocation wi, Date invocationTime) {
		StringBuilder sql = new StringBuilder(
				"delete from library.webservice_history where calltype_id = "
						+ wi.getCallType().getDatabaseId());

		Integer artistId = null, trackId = null, albumId = null, 
				userId = null, groupId = null, tagId = null;
		if (wi.getTrack() != null) {
			sql.append(" and track_id = " + (trackId = musicDao.getTrackId(wi.getTrack())));
		} else if (wi.getAlbum() != null) {
			sql.append(" and album_id = " + (albumId = musicDao.getAlbumId(wi.getAlbum())));
		} else if (wi.getArtist() != null) {
			sql.append(" and artist_id = " + (artistId = musicDao.getArtistId(wi.getArtist())));
		} else if (wi.getUser() != null) {
			sql.append(" and lastfmuser_id = " + (userId = lastFmDao.getLastFmUserId(wi.getUser().getLastFmUsername())));
		} else if (wi.getGroup() != null) {
			sql.append(" and lastfmgroup_id = " + (groupId = lastFmDao.getLastFmGroupId(wi.getGroup().getName())));
		} else if (wi.getTag() != null) {
			sql.append(" and tag_id = " + (tagId = wi.getTag().getId()));
		}
		if (wi.getPage() != null)
			sql.append(" and page = " + wi.getPage());

		jdbcTemplate.update(sql.toString());
		jdbcTemplate.update("insert into library.webservice_history"
				+ " (artist_id, album_id, track_id, lastfmuser_id, lastfmgroup_id,"
				+ " tag_id, calltype_id, page, invocation_time)"
				+ " values (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
				artistId, albumId, trackId, userId, groupId, tagId, 
				wi.getCallType().getDatabaseId(), wi.getPage(), invocationTime);
	}
	
	/*
	 * Implementation of DAO interface method to check if a certain invocation
	 * would be allowed.
	 * 
	 * Delegates decision to relevant method/query depending on type of invocation.
	 */
	@Override
	public boolean isWebserviceInvocationAllowed(WebserviceInvocation wi) {
		if (wi.getTrack() != null) {
			return isWebserviceInvocationAllowed(wi.getCallType(), wi.getTrack());
		} else if (wi.getAlbum() != null) {
			return isWebserviceInvocationAllowed(wi.getCallType(), wi.getAlbum());
		} else if (wi.getArtist() != null) {
			return isWebserviceInvocationAllowed(wi.getCallType(), wi.getArtist());
		} else if (wi.getUser() != null && wi.getPage() != null) {
			return isWebserviceInvocationAllowed(wi.getCallType(), wi.getUser(), wi.getPage());
		} else if (wi.getUser() != null) {
			return isWebserviceInvocationAllowed(wi.getCallType(), wi.getUser());
		} else if (wi.getGroup() != null) {
			return isWebserviceInvocationAllowed(wi.getCallType(), wi.getGroup());
		} else if (wi.getTag() != null) {
			return isWebserviceInvocationAllowed(wi.getCallType(), wi.getTag());
		} else if (wi.getPage() != null) {
			return isWebserviceInvocationAllowed(wi.getCallType(), wi.getPage());
		} else {
			 // for compiler compliance, actually unreachable
			return false;
		}
	}

	protected boolean isWebserviceInvocationAllowed(Calltype callType, short page) {
		String sql = "select max(invocation_time) from library.webservice_history"
			+ " where calltype_id = " + callType.getDatabaseId() 
			+ " and page = " + page;
		Timestamp lastInvocation = jdbcTemplate.queryForObject(sql, Timestamp.class);
		return isWebserviceInvocationAllowed(callType, lastInvocation);
	}

	protected boolean isWebserviceInvocationAllowed(Calltype callType, Artist artist) {
		String sql = "select max(invocation_time) from library.webservice_history h"
			+ " inner join music.artist a on a.id = h.artist_id"
			+ " where calltype_id = " + callType.getDatabaseId()
			+ " and a.artist_name = upper(?)";
		Timestamp lastInvocation = jdbcTemplate.queryForObject(sql, 
				new Object[]{artist.getName()}, Timestamp.class);
		return isWebserviceInvocationAllowed(callType, lastInvocation);
	}

	protected boolean isWebserviceInvocationAllowed(Calltype callType, Track track) {
		String sql = "select max(invocation_time) from library.webservice_history h"
			+ " inner join music.track t on t.id = h.track_id"
			+ " inner join music.artist a on a.id = t.artist_id"
			+ " where calltype_id = " + callType.getDatabaseId()
			+ " and a.artist_name = upper(?) and t.track_name = upper(?)";
		Timestamp lastInvocation = jdbcTemplate.queryForObject(sql, new Object[]{
				track.getArtist().getName(), track.getName()}, Timestamp.class);
		return isWebserviceInvocationAllowed(callType, lastInvocation);
	}

	protected boolean isWebserviceInvocationAllowed(Calltype callType, Album album) {
		String sql = "select max(invocation_time) from library.webservice_history h"
			+ " inner join music.album alb on alb.id = h.album_id"
			+ " inner join music.artist art on art.id = alb.artist_id"
			+ " where calltype_id = " + callType.getDatabaseId()
			+ " and art.artist_name = upper(?) and alb.album_name = upper(?)";
		Timestamp lastInvocation = jdbcTemplate.queryForObject(sql, new Object[]{
				album.getArtist().getName(), album.getName()}, Timestamp.class);
		return isWebserviceInvocationAllowed(callType, lastInvocation);
	}

	protected boolean isWebserviceInvocationAllowed(Calltype callType, LastFmUser user, short page) {
		String sql = "select max(invocation_time) from library.webservice_history h"
			+ " inner join music.lastfmuser u on h.lastfmuser_id = u.id"
			+ " where calltype_id = " + callType.getDatabaseId()
			+ " and u.lastfm_user = upper(?) and h.page = ?";
		Timestamp lastInvocation = jdbcTemplate.queryForObject(sql, new Object[]{
				user.getLastFmUsername(), page}, Timestamp.class);
		return isWebserviceInvocationAllowed(callType, lastInvocation);
	}

	protected boolean isWebserviceInvocationAllowed(Calltype callType, LastFmUser user) {
		String sql = "select max(invocation_time) from library.webservice_history h"
			+ " inner join music.lastfmuser u on h.lastfmuser_id = u.id"
			+ " where calltype_id = " + callType.getDatabaseId()
			+ " and u.lastfm_user = upper(?)";
		Timestamp lastInvocation = jdbcTemplate.queryForObject(sql, new Object[]{
				user.getLastFmUsername()}, Timestamp.class);
		return isWebserviceInvocationAllowed(callType, lastInvocation);
	}

	protected boolean isWebserviceInvocationAllowed(Calltype callType, LastFmGroup group) {
		String sql = "select max(invocation_time) from library.webservice_history h"
			+ " inner join music.lastfmgroup g on h.lastfmgroup_id = g.id"
			+ " where calltype_id = " + callType.getDatabaseId()
			+ " and g.group_name = upper(?)";
		Timestamp lastInvocation = jdbcTemplate.queryForObject(sql, new Object[]{
				group.getName()}, Timestamp.class);
		return isWebserviceInvocationAllowed(callType, lastInvocation);
	}

	protected boolean isWebserviceInvocationAllowed(Calltype callType, Tag tag) {
		String sql = "select max(invocation_time) from library.webservice_history h"
			+ " where calltype_id = " + callType.getDatabaseId()
			+ " and h.tag_id = " + tag.getId();
		Timestamp lastInvocation = jdbcTemplate.queryForObject(sql, Timestamp.class);
		return isWebserviceInvocationAllowed(callType, lastInvocation);
	}

	private boolean isWebserviceInvocationAllowed(Calltype callType, Timestamp lastInvocation) {
		boolean oldEnough;
		if (lastInvocation == null) {
			oldEnough = true;
		} else if (lastInvocation.getTime() >> 32 == Integer.MAX_VALUE) {
			// checks if invocation_time is close enough to 'infinity'.
			oldEnough = false;
		} else {
			DateTime lastInvocationDateTime = new DateTime(lastInvocation.getTime());
			Days daysBetween = Days.daysBetween(lastInvocationDateTime, new DateTime());
			oldEnough = daysBetween.getDays() > callType.getDaysToCache();
		}
		return oldEnough;
	}

	/*
	 * Web services that are language dependant:
	 * artist.getInfo (5)
	 * tag.getInfo (not logged)
	 */
	@Override
	public void clearLanguageSpecificWebserviceInvocations() {
		jdbcTemplate.update("delete from library.webservice_history"
				+ " where calltype_id in (5)");

		jdbcTemplate.update("delete from music.artistinfo");
		jdbcTemplate.update("delete from music.taginfo");
	}

	/*
	 * Group artists in local library by last update time from last.fm, and return
	 * the 1/30th, but maximum 1000 artists, that were updated longest ago.
	 */
	@Override
	public List<String> getArtistNamesWithOldestInvocations(Calltype callType) {
		String sql = "select a.artist_name_capitalization from ("
			+ "  select artist_id, ntile(30) over (order by invocation_time) "
			+ "   from library.webservice_history where calltype_id = " + callType.getDatabaseId()
			+ "   and artist_id in (select artist_id from library.artist)"
			+ " ) ntile" 
			+ " inner join music.artist a on ntile.artist_id = a.id"
			+ " and ntile.ntile = 1"
			+ " order by artist_id limit 1000";
		
		return jdbcTemplate.queryForList(sql, String.class);
	}

	/*
	 * Return artists found in local library, who's never been looked up from last.fm.
	 * 
	 * Sets an upper limit of 3000 artists, meaning larger libraries will have to run
	 * the search index multiple times to get fully updated.
	 */
	@Override
	public List<String> getArtistNamesWithNoInvocations(Calltype callType) {
		String sql = "select artist_name_capitalization from music.artist where id in ("
				+ " select distinct mt.artist_id from library.track lt"
				+ " inner join music.track mt on lt.track_id = mt.id"
				+ " where not exists ("
				+ " select 1 from library.webservice_history where artist_id = mt.artist_id "
				+ " and calltype_id = " + callType.getDatabaseId() + ")"
				+ " order by mt.artist_id limit 3000)";
		
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

	public void setMusicDao(MusicDao musicDao) {
		this.musicDao = musicDao;
	}

	public void setLastFmDao(LastFmDao lastFmDao) {
		this.lastFmDao = lastFmDao;
	}
	
}