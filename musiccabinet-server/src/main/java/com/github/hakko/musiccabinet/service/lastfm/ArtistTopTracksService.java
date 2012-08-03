package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_TOP_TRACKS;

import java.util.Set;

import com.github.hakko.musiccabinet.dao.ArtistTopTracksDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ArtistTopTracksClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to creating artist top tracks.
 */
public class ArtistTopTracksService extends SearchIndexUpdateService {

	protected ArtistTopTracksClient artistTopTracksClient;
	protected ArtistTopTracksDao artistTopTracksDao;
	protected WebserviceHistoryService webserviceHistoryService;

	private static final Logger LOG = Logger.getLogger(ArtistTopTracksService.class);

	@Override
	protected void updateSearchIndex() throws ApplicationException {
		Set<String> artistNames = webserviceHistoryService.
				getArtistNamesScheduledForUpdate(ARTIST_GET_TOP_TRACKS);
		setTotalOperations(artistNames.size());
		
		for (String artistName : artistNames) {
			try {
				WSResponse wsResponse = artistTopTracksClient.getTopTracks(new Artist(artistName));
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					ArtistTopTracksParser attParser =
						new ArtistTopTracksParserImpl(stringUtil.getInputStream());
					artistTopTracksDao.createTopTracks(attParser.getArtist(), 
							attParser.getTopTracks());
				}
			} catch (ApplicationException e) {
				LOG.warn("Fetching top tracks for " + artistName + " failed.", e);
			}
			addFinishedOperation();
		}
	}

	@Override
	public String getUpdateDescription() {
		return "artist top tracks";
	}
	
	// Spring setters
	
	public void setArtistTopTracksClient(ArtistTopTracksClient artistTopTracksClient) {
		this.artistTopTracksClient = artistTopTracksClient;
	}

	public void setArtistTopTracksDao(ArtistTopTracksDao artistTopTracksDao) {
		this.artistTopTracksDao = artistTopTracksDao;
	}

	public void setWebserviceHistoryService(
			WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}
	
}