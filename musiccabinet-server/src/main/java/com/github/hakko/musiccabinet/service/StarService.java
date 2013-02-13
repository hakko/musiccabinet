package com.github.hakko.musiccabinet.service;

import static java.lang.Integer.MAX_VALUE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.StarDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.LastFmSettingsService;
import com.github.hakko.musiccabinet.ws.lastfm.TrackLoveClient;
import com.github.hakko.musiccabinet.ws.lastfm.TrackUnLoveClient;

public class StarService {

	private Map<Integer, Set<Integer>> starredArtists = new HashMap<Integer, Set<Integer>>();
	private Map<Integer, Set<Integer>> starredAlbums = new HashMap<Integer, Set<Integer>>();
	private Map<Integer, Set<Integer>> starredTracks = new HashMap<Integer, Set<Integer>>();

	private Map<String, LastFmUser> cachedUsers = new HashMap<>();
	
	private StarDao starDao;
	private LastFmDao lastFmDao;
	private MusicDao musicDao;
	private LibraryBrowserDao browserDao;
	private LastFmSettingsService lastFmSettingsService;
	private TrackLoveClient trackLoveClient;
	private TrackUnLoveClient trackUnLoveClient;
	
	public void starArtist(String lastFmUsername, int artistId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starArtist(lastFmUser, artistId);
		getStarredArtistIds(lastFmUser).add(artistId);
	}
	
	public void unstarArtist(String lastFmUsername, int artistId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarArtist(lastFmUser, artistId);
		getStarredArtistIds(lastFmUser).remove(artistId);
	}
	
	protected Set<Integer> getStarredArtistIds(LastFmUser lastFmUser) {
		if (!starredArtists.containsKey(lastFmUser.getId())) {
			starredArtists.put(lastFmUser.getId(), new HashSet<>(
					starDao.getStarredArtistIds(lastFmUser, 0, MAX_VALUE, null)));
		}
		return starredArtists.get(lastFmUser.getId());
	}

	public List<Artist> getStarredArtists(String lastFmUsername) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		return musicDao.getArtists(getStarredArtistIds(lastFmUser));
	}
	
	public void starAlbum(String lastFmUsername, int albumId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starAlbum(lastFmUser, albumId);
		getStarredAlbumIds(lastFmUser).add(albumId);
	}
	
	public void unstarAlbum(String lastFmUsername, int albumId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarAlbum(lastFmUser, albumId);
		getStarredAlbumIds(lastFmUser).remove(albumId);
	}
	
	protected Set<Integer> getStarredAlbumIds(LastFmUser lastFmUser) {
		if (!starredAlbums.containsKey(lastFmUser.getId())) {
			starredAlbums.put(lastFmUser.getId(), new HashSet<>(
					starDao.getStarredAlbumIds(lastFmUser, 0, MAX_VALUE, null)));
		}
		return starredAlbums.get(lastFmUser.getId());
	}

	public void starTrack(String lastFmUsername, int trackId) throws ApplicationException {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starTrack(lastFmUser, trackId);
		getStarredTracks(lastFmUser).add(trackId);
		if (lastFmSettingsService.isSyncStarredAndLovedTracks()) {
			trackLoveClient.love(browserDao.getTrack(trackId), lastFmUser);
		}
	}
	
	public void unstarTrack(String lastFmUsername, int trackId) throws ApplicationException {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarTrack(lastFmUser, trackId);
		getStarredTracks(lastFmUser).remove(trackId);
		if (lastFmSettingsService.isSyncStarredAndLovedTracks()) {
			trackUnLoveClient.unlove(browserDao.getTrack(trackId), lastFmUser);
		}
	}
	
	protected Set<Integer> getStarredTracks(LastFmUser lastFmUser) {
		if (!starredTracks.containsKey(lastFmUser.getId())) {
			starredTracks.put(lastFmUser.getId(), new HashSet<>(
					starDao.getStarredTrackIds(lastFmUser, 0, MAX_VALUE, null)));
		}
		return starredTracks.get(lastFmUser.getId());
	}

	protected LastFmUser getLastFmUser(String lastFmUsername) {
		if (cachedUsers.containsKey(lastFmUsername)) {
			return cachedUsers.get(lastFmUsername);
		}
		LastFmUser lastFmUser = lastFmDao.getLastFmUser(lastFmUsername);
		cachedUsers.put(lastFmUsername, lastFmUser);
		return lastFmUser;
	}

	public boolean isArtistStarred(String lastFmUsername, int artistId) {
		if (lastFmUsername == null) {
			return false;
		}
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		return getStarredArtistIds(lastFmUser).contains(artistId);
	}

	public boolean[] getStarredAlbumsMask(String lastFmUsername, List<Integer> albumIds) {
		boolean[] mask = new boolean[albumIds.size()];
		if (lastFmUsername == null) {
			return mask;
		}
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		for (int i = 0; i < mask.length; i++) {
			mask[i] = getStarredAlbumIds(lastFmUser).contains(albumIds.get(i));
		}
		return mask;
	}

	public boolean[] getStarredTracksMask(String lastFmUsername, List<Integer> trackIds) {
		boolean[] mask = new boolean[trackIds.size()];
		if (lastFmUsername == null) {
			return mask;
		}
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		for (int i = 0; i < mask.length; i++) {
			mask[i] = getStarredTracks(lastFmUser).contains(trackIds.get(i));
		}
		return mask;
	}

	public void clearTrackCache() {
		starredTracks.clear();
	}

	// Spring setters
	
	public void setStarDao(StarDao starDao) {
		this.starDao = starDao;
	}

	public void setLastFmDao(LastFmDao lastFmDao) {
		this.lastFmDao = lastFmDao;
	}

	public void setMusicDao(MusicDao musicDao) {
		this.musicDao = musicDao;
	}

	public void setLibraryBrowserDao(LibraryBrowserDao browserDao) {
		this.browserDao = browserDao;
	}

	public void setLastFmSettingsService(LastFmSettingsService lastFmSettingsService) {
		this.lastFmSettingsService = lastFmSettingsService;
	}

	public void setTrackLoveClient(TrackLoveClient trackLoveClient) {
		this.trackLoveClient = trackLoveClient;
	}

	public void setTrackUnLoveClient(TrackUnLoveClient trackUnLoveClient) {
		this.trackUnLoveClient = trackUnLoveClient;
	}

}