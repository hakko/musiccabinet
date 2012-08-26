package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.USER_GET_RECOMMENDED_ARTISTS;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.dao.LastFmUserDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class UserRecommendedArtistsClient extends AbstractWSGetClient {

	public static final String METHOD = "user.getrecommendedartists";
	
	private LastFmUserDao lastFmUserDao;
	
	public UserRecommendedArtistsClient() {
		super(WSConfiguration.AUTHENTICATED_LOGGED);
	}
	
	public WSResponse getUserRecommendedArtists(String lastFmUsername) throws ApplicationException {
		LastFmUser user = lastFmUserDao.getLastFmUser(lastFmUsername);
		
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(USER_GET_RECOMMENDED_ARTISTS, user);
		
		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_LIMIT, "100"));
		params.add(new BasicNameValuePair(PARAM_SK, user.getSessionKey()));
		
		return executeWSRequest(webserviceInvocation, params);
	}

	public void setLastFmUserDao(LastFmUserDao lastFmUserDao) {
		this.lastFmUserDao = lastFmUserDao;
	}
	
}