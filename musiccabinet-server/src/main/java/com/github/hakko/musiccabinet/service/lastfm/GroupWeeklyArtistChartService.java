package com.github.hakko.musiccabinet.service.lastfm;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.dao.GroupWeeklyArtistChartDao;
import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.domain.model.aggr.GroupWeeklyArtistChart;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.GroupWeeklyArtistChartParser;
import com.github.hakko.musiccabinet.parser.lastfm.GroupWeeklyArtistChartParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.GroupWeeklyArtistChartClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to updating/getting weekly artist charts for last.fm groups.
 */
public class GroupWeeklyArtistChartService extends SearchIndexUpdateService {

	protected GroupWeeklyArtistChartClient client;
	protected GroupWeeklyArtistChartDao dao;
	protected WebserviceHistoryService webserviceHistoryService;
	protected LastFmDao lastFmDao;
	
	private static final Logger LOG = Logger.getLogger(GroupWeeklyArtistChartService.class);
	
	@Override
	public void updateSearchIndex() throws ApplicationException {
		List<GroupWeeklyArtistChart> artistCharts = new ArrayList<>();
		List<LastFmGroup> groups = lastFmDao.getLastFmGroups();
		
		setTotalOperations(groups.size());
		
		for (LastFmGroup group : groups) {
			try {
				WSResponse wsResponse = client.getWeeklyArtistChart(group);
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					GroupWeeklyArtistChartParser parser =
							new GroupWeeklyArtistChartParserImpl(stringUtil.getInputStream());
					artistCharts.add(new GroupWeeklyArtistChart(
							group.getName(), parser.getArtistPlayCount()));
				}
			} catch (ApplicationException e) {
				LOG.warn("Fetching weekly artist chart for " + group.getName() + " failed.", e);
			}
			addFinishedOperation();
		}
		
		dao.createArtistCharts(artistCharts);
	}

	@Override
	public String getUpdateDescription() {
		return "weekly group artist charts";
	}
	
	public GroupWeeklyArtistChart getWeeklyArtistChart(LastFmGroup group) {
		return dao.getWeeklyArtistChart(group);
	}

	// Spring setters

	public void setGroupWeeklyArtistChartClient(GroupWeeklyArtistChartClient client) {
		this.client = client;
	}
	
	public void setGroupWeeklyArtistChartDao(GroupWeeklyArtistChartDao dao) {
		this.dao = dao;
	}

	public void setWebserviceHistoryService(WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}

	public void setLastFmDao(LastFmDao lastFmDao) {
		this.lastFmDao = lastFmDao;
	}

}