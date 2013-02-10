package com.github.hakko.musiccabinet.service.lastfm;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.dao.UserLovedTracksDao;
import com.github.hakko.musiccabinet.domain.model.aggr.UserLovedTracks;
import com.github.hakko.musiccabinet.domain.model.aggr.UserStarredTrack;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.UserLovedTracksParser;
import com.github.hakko.musiccabinet.parser.lastfm.UserLovedTracksParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.TrackLoveClient;
import com.github.hakko.musiccabinet.ws.lastfm.UserLovedTracksClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to updating/getting loved tracks for users.
 * 
 */
public class UserLovedTracksService extends SearchIndexUpdateService {

	protected WebserviceHistoryService webserviceHistoryService;
	protected LastFmSettingsService lastFmSettingsService;
	protected UserLovedTracksClient userLovedTracksClient;
	protected UserLovedTracksDao userLovedTracksDao;
	protected TrackLoveClient trackLoveClient;

	@Override
	protected void updateSearchIndex() throws ApplicationException {
		List<LastFmUser> users = lastFmSettingsService.getLastFmUsers();
		setTotalOperations(users.size());

		List<Track> lovedTracks;
		List<UserLovedTracks> userLovedTracks = new ArrayList<>();
		
		for (LastFmUser user : users) {
			short page = 0, totalPages = 0;
			lovedTracks = new ArrayList<>();
			do {
				WSResponse wsResponse = userLovedTracksClient.getUserLovedTracks(user, page);
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					UserLovedTracksParser parser = new UserLovedTracksParserImpl(
							stringUtil.getInputStream());
					totalPages = parser.getTotalPages();
					lovedTracks.addAll(parser.getLovedTracks());
					setTotalOperations(totalPages);
					addFinishedOperation();
				}
			} while (++page < totalPages);

			userLovedTracks.add(new UserLovedTracks(user.getLastFmUsername(), lovedTracks));
		}
		
		userLovedTracksDao.createLovedTracks(userLovedTracks);

		loveStarredTracks();
	}

	private void loveStarredTracks() throws ApplicationException {
		for (UserStarredTrack ust : userLovedTracksDao.getStarredButNotLovedTracks()) {
			trackLoveClient.love(ust.getStarredTrack(), ust.getLastFmUser());
		}
	}

	@Override
	public String getUpdateDescription() {
		return "user loved track summaries";
	}

	// Spring setters

	public void setWebserviceHistoryService(
			WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}

	public void setLastFmSettingsService(LastFmSettingsService lastFmSettingsService) {
		this.lastFmSettingsService = lastFmSettingsService;
	}

	public void setUserLovedTracksClient(UserLovedTracksClient userLovedTracksClient) {
		this.userLovedTracksClient = userLovedTracksClient;
	}

	public void setUserLovedTracksDao(UserLovedTracksDao userLovedTracksDao) {
		this.userLovedTracksDao = userLovedTracksDao;
	}

	public void setTrackLoveClient(TrackLoveClient trackLoveClient) {
		this.trackLoveClient = trackLoveClient;
	}

}