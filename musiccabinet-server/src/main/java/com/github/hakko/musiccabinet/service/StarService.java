package com.github.hakko.musiccabinet.service;

import static java.lang.Integer.MAX_VALUE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.hakko.musiccabinet.dao.LastFmUserDao;
import com.github.hakko.musiccabinet.dao.StarDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public class StarService {

	private Map<Integer, Set<Integer>> starredArtists = new HashMap<Integer, Set<Integer>>();
	private Map<Integer, Set<Integer>> starredAlbums = new HashMap<Integer, Set<Integer>>();
	private Map<Integer, Set<Integer>> starredTracks = new HashMap<Integer, Set<Integer>>();

	private Map<String, LastFmUser> cachedUsers = new HashMap<>();
	
	private StarDao starDao;
	private LastFmUserDao lastFmUserDao;
	
	public void starArtist(String lastFmUsername, int artistId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starArtist(lastFmUser, artistId);
		getStarredArtists(lastFmUser).add(artistId);
	}
	
	public void unstarArtist(String lastFmUsername, int artistId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarArtist(lastFmUser, artistId);
		getStarredArtists(lastFmUser).remove(artistId);
	}
	
	protected Set<Integer> getStarredArtists(LastFmUser lastFmUser) {
		if (!starredArtists.containsKey(lastFmUser.getId())) {
			starredArtists.put(lastFmUser.getId(), new HashSet<>(
					starDao.getStarredArtistIds(lastFmUser, 0, MAX_VALUE, null)));
		}
		return starredArtists.get(lastFmUser.getId());
	}

	
	public void starAlbum(String lastFmUsername, int albumId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starAlbum(lastFmUser, albumId);
		getStarredAlbums(lastFmUser).add(albumId);
	}
	
	public void unstarAlbum(String lastFmUsername, int albumId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarAlbum(lastFmUser, albumId);
		getStarredAlbums(lastFmUser).remove(albumId);
	}
	
	protected Set<Integer> getStarredAlbums(LastFmUser lastFmUser) {
		if (!starredAlbums.containsKey(lastFmUser.getId())) {
			starredAlbums.put(lastFmUser.getId(), new HashSet<>(
					starDao.getStarredAlbumIds(lastFmUser, 0, MAX_VALUE, null)));
		}
		return starredAlbums.get(lastFmUser.getId());
	}
	
	public void starTrack(String lastFmUsername, int trackId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.starTrack(lastFmUser, trackId);
		getStarredTracks(lastFmUser).add(trackId);
	}
	
	public void unstarTrack(String lastFmUsername, int trackId) {
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		starDao.unstarTrack(lastFmUser, trackId);
		getStarredTracks(lastFmUser).remove(trackId);
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
		LastFmUser lastFmUser = lastFmUserDao.getLastFmUser(lastFmUsername);
		cachedUsers.put(lastFmUsername, lastFmUser);
		return lastFmUser;
	}

	public boolean isArtistStarred(String lastFmUsername, int artistId) {
		if (lastFmUsername == null) {
			return false;
		}
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		return getStarredArtists(lastFmUser).contains(artistId);
	}

	public boolean[] getStarredAlbumsMask(String lastFmUsername, List<Integer> albumIds) {
		boolean[] mask = new boolean[albumIds.size()];
		if (lastFmUsername == null) {
			return mask;
		}
		LastFmUser lastFmUser = getLastFmUser(lastFmUsername);
		for (int i = 0; i < mask.length; i++) {
			mask[i] = getStarredAlbums(lastFmUser).contains(albumIds.get(i));
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
	
	// Spring setters
	
	public void setStarDao(StarDao starDao) {
		this.starDao = starDao;
	}

	public void setLastFmUserDao(LastFmUserDao lastFmUserDao) {
		this.lastFmUserDao = lastFmUserDao;
	}

}