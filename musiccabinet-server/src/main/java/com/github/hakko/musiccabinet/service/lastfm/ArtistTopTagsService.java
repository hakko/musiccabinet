package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_TOP_TAGS;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.hakko.musiccabinet.dao.ArtistTopTagsDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTagsParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTagsParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ArtistTopTagsClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to creating artist top tags.
 */
public class ArtistTopTagsService extends SearchIndexUpdateService {

	protected ArtistTopTagsClient artistTopTagsClient;
	protected ArtistTopTagsDao artistTopTagsDao;
	protected WebserviceHistoryService webserviceHistoryService;

	private static final Logger LOG = Logger.getLogger(ArtistTopTagsService.class);
	
	public List<Tag> getTopTags(int artistId, int limit) throws ApplicationException {
		return artistTopTagsDao.getTopTags(artistId, limit);
	}

	@Override
	protected void updateSearchIndex() throws ApplicationException {
		Set<String> artistNames = webserviceHistoryService.
				getArtistNamesScheduledForUpdate(ARTIST_GET_TOP_TAGS);
		
		setTotalOperations(artistNames.size());
		
		for (String artistName : artistNames) {
			try {
				WSResponse wsResponse = artistTopTagsClient.getTopTags(new Artist(artistName));
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					ArtistTopTagsParser attParser =
						new ArtistTopTagsParserImpl(stringUtil.getInputStream());
					removeTagsWithLowTagCount(attParser.getTopTags());
					artistTopTagsDao.createTopTags(attParser.getArtist(), 
							attParser.getTopTags());
				}
			} catch (ApplicationException e) {
				LOG.warn("Fetching top tags for " + artistName + " failed.", e);
			}
			addFinishedOperation();
		}
	}

	@Override
	public String getUpdateDescription() {
		return "artist tags";
	}
	
	/*
	 * The top tag response from Last.fm contains tags with very low relevance.
	 * Don't bother to save them to database, they won't really affect the radio
	 * selection algorithm anyway.
	 */
	protected void removeTagsWithLowTagCount(List<Tag> tags) {
		for (Iterator<Tag> it = tags.iterator(); it.hasNext();) {
			Tag tag = it.next();
			if (tag.getCount() < 10) {
				it.remove();
			}
		}
	}

	// Spring setters
	
	public void setArtistTopTagsClient(ArtistTopTagsClient artistTopTagsClient) {
		this.artistTopTagsClient = artistTopTagsClient;
	}

	public void setArtistTopTagsDao(ArtistTopTagsDao artistTopTagsDao) {
		this.artistTopTagsDao = artistTopTagsDao;
	}

	public void setWebserviceHistoryService(
			WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}
	
}