package com.github.hakko.musiccabinet.service.lastfm;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.dao.UserRecommendedArtistsDao;
import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.UserRecommendedArtistsParser;
import com.github.hakko.musiccabinet.parser.lastfm.UserRecommendedArtistsParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.UserRecommendedArtistsClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Search index update service for user recommended artists.
 * 
 */
public class UserRecommendedArtistsService extends SearchIndexUpdateService {

	protected UserRecommendedArtistsClient userRecommendedArtistsClient;
	protected UserRecommendedArtistsDao dao;
	protected WebserviceHistoryService webserviceHistoryService;
	protected LastFmSettingsService lastFmSettingsService;
	
	private static final Logger LOG = Logger.getLogger(UserRecommendedArtistsService.class);
	
	@Override
	protected void updateSearchIndex() throws ApplicationException {
		List<LastFmUser> users = lastFmSettingsService.getLastFmUsers();
		setTotalOperations(users.size());

		List<UserRecommendedArtists> artists = new ArrayList<>();
		
		for (LastFmUser user : users) {
			try {
				WSResponse wsResponse = userRecommendedArtistsClient.
						getUserRecommendedArtists(user.getLastFmUsername());
				LOG.debug(wsResponse);
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					UserRecommendedArtistsParser parser =
							new UserRecommendedArtistsParserImpl(stringUtil.getInputStream());
					artists.add(new UserRecommendedArtists(user, parser.getArtists()));
				}
			} catch (ApplicationException e) {
				LOG.warn("Fetching top artist for " + user.getLastFmUsername() + " failed.", e);
			}
			addFinishedOperation();
		}
		
		dao.createUserRecommendedArtists(artists);
	}

	@Override
	public String getUpdateDescription() {
		return "user recommended artists";
	}
	
	// Spring setters

	public void setUserRecommendedArtistsClient(UserRecommendedArtistsClient userRecommendedArtistsClient) {
		this.userRecommendedArtistsClient = userRecommendedArtistsClient;
	}

	public void setUserRecommendedArtistsDao(UserRecommendedArtistsDao userRecommendedArtistsDao) {
		this.dao = userRecommendedArtistsDao;
	}

	public void setWebserviceHistoryService(WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}

	public void setLastFmSettingsService(LastFmSettingsService lastFmSettingsService) {
		this.lastFmSettingsService = lastFmSettingsService;
	}

}