package com.github.hakko.musiccabinet.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.hakko.musiccabinet.dao.MusicDirectoryDao;
import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.dao.PlaylistGeneratorDao;
import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;

/*
 * Exposes methods to generate playlists of relevant tracks, based on either an artist,
 * a single track, or one or more genres.
 */
public class PlaylistGeneratorService {

	protected PlaylistGeneratorDao playlistGeneratorDao;
	protected MusicDirectoryDao musicDirectoryDao;
	protected MusicFileDao musicFileDao;
	protected TrackRelationService trackRelationService;

	private static final Logger LOG = Logger.getLogger(PlaylistGeneratorService.class);
	
	/*
	 * Just make sure previous log file gets erased.
	 */
	public PlaylistGeneratorService() {
		LOG.info("MusicCabinet playlist service started at " + new Date() + ".");
	}
	
	/*
	 * TODO
	 * 
	 * We currently use a pre-calculated table (library.artisttoptrackplaycount)
	 * when searching for relevant artist tracks.
	 * 
	 * This should really be changed to using a materialized view, when time/
	 * Postgresql allows for it.
	 * 
	 * Until then, clear the table before removing data it refers to, and update
	 * it manually when new data has been added.
	 */
	public void updateSearchIndex() {
		playlistGeneratorDao.updateSearchIndex();
	}

	public boolean isSearchIndexCreated() {
		return playlistGeneratorDao.isSearchIndexCreated();
	}
	
	public List<String> getPlaylistForArtist(String path) throws ApplicationException {
		int artistId = musicDirectoryDao.getArtistId(path);
		List<PlaylistItem> result = playlistGeneratorDao.getPlaylistForArtist(artistId);
		Collections.shuffle(result);
		distributeArtists(result);
		List<String> trackPaths = new ArrayList<String>();
		for (PlaylistItem pli : result) {
			trackPaths.add(pli.getPath());
		}
		return trackPaths;
	}

	// Currently not used from UI, it's way too slow.
	public List<String> getPlaylistForTrack(String path) throws ApplicationException {
		// opposed from artist relations, track relations are not pre-fetched.
		trackRelationService.updateTrackRelation(path);
		int trackId = musicFileDao.getTrackId(path);
		List<PlaylistItem> result = playlistGeneratorDao.getPlaylistForTrack(trackId);
		Collections.shuffle(result);
		distributeArtists(result);
		List<String> trackPaths = new ArrayList<String>();
		for (PlaylistItem pli : result) {
			trackPaths.add(pli.getPath());
		}
		return trackPaths;
	}
	
	public List<String> getTopTracksForArtist(String path) throws ApplicationException {
		int artistId = musicDirectoryDao.getArtistId(path);
		return playlistGeneratorDao.getTopTracksForArtist(artistId);
	}
	
	public List<String> getTopTracksForTags(String[] tags) {
		List<String> result = playlistGeneratorDao.getPlaylistForTags(tags);
		Collections.shuffle(result);
		return result;
	}
	
	/*
	 * Approach: iterate over list of playlist items.
	 * If we find an element that is preceded with an element with the same artist (A),
	 * try finding first position to bubble it to by iterating forward, looking for
	 * the first occurrence of three items in a row with an artist different than (A).
	 * Then swap item (A) with the middle one in the group of three.
	 * 
	 * Using this with small lists (like AAABBB) won't work, it could end up as ABABBA.
	 * This is intentional, always having ABABAB isn't ideal.
	 */
	protected void distributeArtists(List<PlaylistItem> list) {
		int size = list.size();
		for (int i = 1; i < size; i++) {
			Artist artist = list.get(i).getArtist();
			if (list.get(i - 1).getArtist().equals(artist)) {
				for (int j = i + 5; j < i + 5 + size; j++) {
					int prev = (j - 1 + size) % size;
					int pos = j % size;
					int next = (j + 1) % size;
					if (!list.get(prev).getArtist().equals(artist) &&
						!list.get(pos).getArtist().equals(artist) &&
						!list.get(next).getArtist().equals(artist)) {
						Collections.swap(list, i, pos);
						break;
					}
				}
			}
		}
	}

	// Spring setters
	
	public void setPlaylistGeneratorDao(PlaylistGeneratorDao playlistGeneratorDao) {
		this.playlistGeneratorDao = playlistGeneratorDao;
	}

	public void setMusicDirectoryDao(MusicDirectoryDao musicDirectoryDao) {
		this.musicDirectoryDao = musicDirectoryDao;
	}

	public void setMusicFileDao(MusicFileDao musicFileDao) {
		this.musicFileDao = musicFileDao;
	}

	public void setTrackRelationService(TrackRelationService trackRelationService) {
		this.trackRelationService = trackRelationService;
	}
	
}