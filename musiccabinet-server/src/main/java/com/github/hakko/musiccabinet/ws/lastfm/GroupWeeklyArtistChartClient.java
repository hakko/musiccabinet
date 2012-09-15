package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.GROUP_WEEKLY_ARTIST_CHART;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class GroupWeeklyArtistChartClient extends AbstractWSGetClient {

	public static final String METHOD = "group.getweeklyartistchart";
	
	public WSResponse getWeeklyArtistChart(LastFmGroup lastFmGroup) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(GROUP_WEEKLY_ARTIST_CHART, lastFmGroup);

		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_GROUP, lastFmGroup.getName()));
		
		return executeWSRequest(webserviceInvocation, params);
	}
	
}