package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_INFO;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.dao.ArtistInfoDao;
import com.github.hakko.musiccabinet.dao.MusicDirectoryDao;
import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistInfoParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistInfoParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ArtistInfoClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to updating/getting info for artists.
 * 
 */
public class ArtistInfoService extends SearchIndexUpdateService {

	protected ArtistInfoClient artistInfoClient;
	protected ArtistInfoDao artistInfoDao;
	protected WebserviceHistoryDao webserviceHistoryDao;

	protected MusicDirectoryDao musicDirectoryDao;
	
	private static final int BATCH_SIZE = 1000;
	
	private static final Logger LOG = Logger.getLogger(ArtistInfoService.class);
	
	public ArtistInfo getArtistInfo(String path) throws ApplicationException {
		ArtistInfo artistInfo = null;
		Integer artistId = musicDirectoryDao.getArtistId(path);
		if (artistId != null) {
			artistInfo = artistInfoDao.getArtistInfo(artistId);
			if (artistInfo == null) {
				LOG.info("No artist info found for " + path);
			}
		}
		return artistInfo;
	}
	
	public void setBioSummary(String path, String biosummary) throws ApplicationException {
		Integer artistId = musicDirectoryDao.getArtistId(path);
		if (artistId != null) {
			webserviceHistoryDao.blockWebserviceInvocation(artistId, ARTIST_GET_INFO);
			artistInfoDao.setBioSummary(artistId, biosummary);
		}
	}
	
	@Override
	protected void updateSearchIndex() throws ApplicationException {
		List<Artist> artists = webserviceHistoryDao.
				getArtistsScheduledForUpdate(ARTIST_GET_INFO);
		
		List<ArtistInfo> artistInfos = new ArrayList<>(artists.size());
		setTotalOperations(artists.size());
		
		for (Artist artist : artists) {
			try {
				WSResponse wsResponse = artistInfoClient.getArtistInfo(artist);
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					ArtistInfoParser aiParser = 
						new ArtistInfoParserImpl(stringUtil.getInputStream());
					if (aiParser.getArtistInfo() != null) {
						artistInfos.add(aiParser.getArtistInfo());
					} else {
						LOG.warn("Artist info response for " + artist 
								+ " not parsed correctly. Response was " 
								+ wsResponse.getResponseBody());
					}
					
					if (artistInfos.size() == BATCH_SIZE) {
						artistInfoDao.createArtistInfo(artistInfos);
						artistInfos.clear();
					}
				}
			} catch (ApplicationException e) {
				LOG.warn("Fetching artist info for " + artist.getName() + " failed.", e);
			}
			addFinishedOperation();
		}

		artistInfoDao.createArtistInfo(artistInfos);
	}

	@Override
	public String getUpdateDescription() {
		return "artist biographies";
	}
	
	// Spring setters
	
	public void setArtistInfoClient(ArtistInfoClient artistInfoClient) {
		this.artistInfoClient = artistInfoClient;
	}

	public void setArtistInfoDao(ArtistInfoDao artistInfoDao) {
		this.artistInfoDao = artistInfoDao;
	}

	public void setWebserviceHistoryDao(WebserviceHistoryDao webserviceHistoryDao) {
		this.webserviceHistoryDao = webserviceHistoryDao;
	}
	
	public void setMusicDirectoryDao(MusicDirectoryDao musicDirectoryDao) {
		this.musicDirectoryDao = musicDirectoryDao;
	}

}