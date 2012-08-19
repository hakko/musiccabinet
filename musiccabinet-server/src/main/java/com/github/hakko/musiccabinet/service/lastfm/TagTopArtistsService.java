package com.github.hakko.musiccabinet.service.lastfm;

import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.dao.TagDao;
import com.github.hakko.musiccabinet.domain.model.aggr.TagTopArtists;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.TagTopArtistsParser;
import com.github.hakko.musiccabinet.parser.lastfm.TagTopArtistsParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.TagTopArtistsClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Search index update service for top artists for tags.
 * 
 */
public class TagTopArtistsService extends SearchIndexUpdateService {

	protected TagTopArtistsClient tagTopArtistsClient;
	protected TagDao tagDao;
	protected WebserviceHistoryService webserviceHistoryService;
	
	private static final Logger LOG = Logger.getLogger(TagTopArtistsService.class);
	
	@Override
	protected void updateSearchIndex() throws ApplicationException {
		List<TagTopArtists> topArtists = new ArrayList<>();
		List<Tag> tags = tagDao.getTagsWithoutTopArtists();

		setTotalOperations(tags.size());
		
		for (Tag tag : tags) {
			try {
				WSResponse wsResponse = tagTopArtistsClient.getTopArtists(tag);
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					TagTopArtistsParser parser =
							new TagTopArtistsParserImpl(stringUtil.getInputStream());
					topArtists.add(new TagTopArtists(tag.getName(), parser.getArtists()));
				}
			} catch (ApplicationException e) {
				LOG.warn("Fetching top artist for " + tag.getName() + " failed.", e);
			}
			addFinishedOperation();
		}
		
		tagDao.createTopArtists(topArtists);
	}

	@Override
	public String getUpdateDescription() {
		return "tag top artists";
	}

	// Spring setters

	public void setTagTopArtistsClient(TagTopArtistsClient tagTopArtistsClient) {
		this.tagTopArtistsClient = tagTopArtistsClient;
	}

	public void setTagDao(TagDao tagDao) {
		this.tagDao = tagDao;
	}

	public void setWebserviceHistoryService(WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}

}