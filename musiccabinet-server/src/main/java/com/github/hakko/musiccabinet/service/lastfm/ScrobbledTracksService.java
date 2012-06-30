package com.github.hakko.musiccabinet.service.lastfm;

import com.github.hakko.musiccabinet.dao.TrackPlayCountDao;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.ScrobbledTracksParser;
import com.github.hakko.musiccabinet.parser.lastfm.ScrobbledTracksParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ScrobbledTracksClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Exposes methods related to the scrobbled tracks (i.e listened tracks that have been
 * submitted to last.fm)
 */
public class ScrobbledTracksService extends SearchIndexUpdateService {

	protected ScrobbledTracksClient client;
	protected TrackPlayCountDao trackPlayCountDao;
	
	private String lastFMUsername;

	@Override
	protected void updateSearchIndex() throws ApplicationException {
		short page = 0, totalPages = 0;
		do {
			WSResponse wsResponse = client.getLibraryTracks(page, getLastFMUsername());
			if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
				StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
				ScrobbledTracksParser parser = new ScrobbledTracksParserImpl(
						stringUtil.getInputStream());
				totalPages = parser.getTotalPages();
				trackPlayCountDao.createTrackPlayCounts(parser.getTrackPlayCounts());
				setTotalOperations(totalPages);
				addFinishedOperation();
			}
		} while (++page < totalPages);
	}

	@Override
	public String getUpdateDescription() {
		return "scrobbled tracks statistics";
	}
	
	public String getLastFMUsername() {
		return lastFMUsername;
	}

	public void setLastFMUsername(String lastFMUsername) {
		this.lastFMUsername = lastFMUsername;
	}

	// Spring setters
	
	public void setScrobbledTracksClient(ScrobbledTracksClient client) {
		this.client = client;
	}

	public void setTrackPlayCountDao(TrackPlayCountDao trackPlayCountDao) {
		this.trackPlayCountDao = trackPlayCountDao;
	}
	
}