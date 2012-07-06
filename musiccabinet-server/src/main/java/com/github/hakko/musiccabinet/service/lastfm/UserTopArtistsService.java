package com.github.hakko.musiccabinet.service.lastfm;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.dao.UserTopArtistsDao;
import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.UserTopArtists;
import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.UserTopArtistsParser;
import com.github.hakko.musiccabinet.parser.lastfm.UserTopArtistsParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.UserTopArtistsClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to updating/getting top artists for users.
 * 
 */
public class UserTopArtistsService extends SearchIndexUpdateService {

	protected UserTopArtistsClient userTopArtistsClient;
	protected UserTopArtistsDao userTopArtistsDao;
	protected WebserviceHistoryDao webserviceHistoryDao;

	private List<LastFmUser> users = new ArrayList<>();
	
	private static final Logger LOG = Logger.getLogger(UserTopArtistsService.class);
	
	public void setUsers(List<LastFmUser> users) {
		this.users = users;
	}
	
	@Override
	protected void updateSearchIndex() throws ApplicationException {
		setTotalOperations(users.size() * Period.values().length);

		List<UserTopArtists> userTopArtists = new ArrayList<>();
		
		for (LastFmUser user : users) {
			for (Period period : Period.values()) {
				try {
					WSResponse wsResponse = userTopArtistsClient.getUserTopArtists(user, period);
					if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
						StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
						UserTopArtistsParser parser =
								new UserTopArtistsParserImpl(stringUtil.getInputStream());
						userTopArtists.add(new UserTopArtists(user, period, parser.getArtists()));
					}
				} catch (ApplicationException e) {
					LOG.warn("Fetching top artist for " + user.getLastFmUser() 
							+ ", " + period.getDescription() + " failed.", e);
				}
				addFinishedOperation();
			}
		}
		
		userTopArtistsDao.createUserTopArtists(userTopArtists);
	}

	@Override
	public String getUpdateDescription() {
		return "user top artist periods";
	}

	public List<ArtistRecommendation> getUserTopArtists(LastFmUser user, Period period, int offset, int limit) {
		return userTopArtistsDao.getUserTopArtists(user, period, offset, limit);
	}

	// Spring setters

	public void setUserTopArtistsClient(UserTopArtistsClient userTopArtistsClient) {
		this.userTopArtistsClient = userTopArtistsClient;
	}

	public void setUserTopArtistsDao(UserTopArtistsDao userTopArtistsDao) {
		this.userTopArtistsDao = userTopArtistsDao;
	}
	
	public void setWebserviceHistoryDao(WebserviceHistoryDao webserviceHistoryDao) {
		this.webserviceHistoryDao = webserviceHistoryDao;
	}

}