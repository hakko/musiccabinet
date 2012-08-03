package com.github.hakko.musiccabinet.service;

import com.github.hakko.musiccabinet.dao.LastFmUserDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.AuthSessionParser;
import com.github.hakko.musiccabinet.parser.lastfm.AuthSessionParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.AuthSessionClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

public class LastFmService {

	/* API key needed to identify this project when communicating with Last.fm. */
	private static final String API_KEY_RESOURCE = "last.fm/api.key";
	public static final String API_KEY = new ResourceUtil(API_KEY_RESOURCE).getContent();

	private AuthSessionClient authSessionClient;
	private LastFmUserDao lastFmUserDao;

	private static final Logger LOG = Logger.getLogger(LastFmService.class);

	public LastFmUser identifyLastFmUser(String token) throws ApplicationException {
		LOG.debug("identifyLastFmUser(" + token + ")");
		WSResponse wsResponse = authSessionClient.getAuthSession(token);
		if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
			StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
			AuthSessionParser authSessionParser = 
					new AuthSessionParserImpl(stringUtil.getInputStream());
			return authSessionParser.getLastFmUser();
		} else {
			LOG.debug("wsResponse: " + wsResponse.getResponseBody());
			throw new ApplicationException("Could not get session key for user! (code "
					+ wsResponse.getErrorCode() + ", " + wsResponse.getErrorMessage() + ")");
		}
	}
	
	public LastFmUser getLastFmUser(String lastFmUsername) {
		return lastFmUserDao.getLastFmUser(lastFmUsername);
	}
	
	public void createOrUpdateLastFmUser(LastFmUser lastFmUser) {
		lastFmUserDao.createOrUpdateLastFmUser(lastFmUser);
	}

	// Spring setters

	public void setAuthSessionClient(AuthSessionClient authSessionClient) {
		this.authSessionClient = authSessionClient;
	}

	public void setLastFmUserDao(LastFmUserDao lastFmUserDao) {
		this.lastFmUserDao = lastFmUserDao;
	}
	
}