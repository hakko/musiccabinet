package com.github.hakko.musiccabinet.service;

import java.util.HashSet;
import java.util.Set;

import com.github.hakko.musiccabinet.dao.ArtistRelationDao;
import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistSimilarityParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistSimilarityParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ArtistSimilarityClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to creating relations between artists.
 * 
 * Using this data, we expose recommendations through {@link ArtistRecommendationService}
 */
public class ArtistRelationService extends SearchIndexUpdateService {

	protected ArtistSimilarityClient artistSimilarityClient;
	protected ArtistRelationDao artistRelationDao;
	protected WebserviceHistoryDao webserviceHistoryDao;

	private static final Logger LOG = Logger.getLogger(ArtistRelationService.class);
	
	@Override
	protected void updateSearchIndex() throws ApplicationException {
		Set<Artist> artists = new HashSet<Artist>();
		artists.addAll(artistRelationDao.getArtistsWithoutRelations());
		artists.addAll(webserviceHistoryDao.getArtistsWithOldestInvocations(
				Calltype.ARTIST_GET_SIMILAR));

		setTotalOperations(artists.size());
		
		for (Artist artist : artists) {
			try {
				WSResponse wsResponse = artistSimilarityClient.getArtistSimilarity(artist);
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					ArtistSimilarityParser asParser = 
						new ArtistSimilarityParserImpl(stringUtil.getInputStream());
					artistRelationDao.createArtistRelations(asParser.getArtist(), 
							asParser.getArtistRelations());
				}
			} catch (ApplicationException e) {
				LOG.warn("Fetching artist relations for " + artist.getName() + " failed.", e);
			}
			addFinishedOperation();
		}
	}

	@Override
	public String getUpdateDescription() {
		return "artist relations";
	}
	
	// Spring setters
	
	public void setArtistSimilarityClient(ArtistSimilarityClient artistSimilarityClient) {
		this.artistSimilarityClient = artistSimilarityClient;
	}

	public void setArtistRelationDao(ArtistRelationDao artistRelationDao) {
		this.artistRelationDao = artistRelationDao;
	}

	public void setWebserviceHistoryDao(WebserviceHistoryDao webserviceHistoryDao) {
		this.webserviceHistoryDao = webserviceHistoryDao;
	}

}