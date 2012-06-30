package com.github.hakko.musiccabinet.service.lastfm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.hakko.musiccabinet.dao.TagDao;
import com.github.hakko.musiccabinet.dao.TagInfoDao;
import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;
import com.github.hakko.musiccabinet.domain.model.music.TagInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.TagInfoParser;
import com.github.hakko.musiccabinet.parser.lastfm.TagInfoParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.TagInfoClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Exposes methods related to tags:
 * 
 * - get a filtered list of available, relevant tags for library
 * - set/get top tags (the ones used for radio / tag cloud)
 * 
 * - get top tags, including their popularity.
 */
public class TagInfoService extends SearchIndexUpdateService {

	protected TagInfoClient tagInfoClient;
	protected TagInfoDao tagInfoDao;
	protected TagDao tagDao;
	
	@Override
	protected void updateSearchIndex() throws ApplicationException {
		
		List<TagInfo> tagInfos = new ArrayList<TagInfo>();
		Set<String> tags = getTagsForUpdate();
		
		setTotalOperations(tags.size());
		
		for (String tag : tags) {
			WSResponse wsResponse = tagInfoClient.getTagInfo(tag);
			if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
				StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
				TagInfoParser tiParser = new TagInfoParserImpl(stringUtil.getInputStream());
				tagInfos.add(tiParser.getTagInfo());
			}
			addFinishedOperation();
		}
		tagInfoDao.createTagInfo(tagInfos);
	}
	

	@Override
	public String getUpdateDescription() {
		return "tag descriptions";
	}
	
	public String getTagInfo(String tagName) {
		return tagInfoDao.getTagInfo(tagName);
	}

	protected Set<String> getTagsForUpdate() {
		Set<String> tags = new HashSet<String>();
		for (TagOccurrence tagOccurrence : tagDao.getAvailableTags()) {
			tags.add(tagOccurrence.getTag());
		}
		tags.removeAll(tagInfoDao.getTagsWithInfo());
		
		return tags;
	}
	
	// Spring setters

	public void setTagInfoClient(TagInfoClient tagInfoClient) {
		this.tagInfoClient = tagInfoClient;
	}

	public void setTagInfoDao(TagInfoDao tagInfoDao) {
		this.tagInfoDao = tagInfoDao;
	}

	public void setTagDao(TagDao tagDao) {
		this.tagDao = tagDao;
	}
	
}