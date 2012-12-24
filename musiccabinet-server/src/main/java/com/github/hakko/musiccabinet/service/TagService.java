package com.github.hakko.musiccabinet.service;

import java.util.List;
import java.util.Map;

import com.github.hakko.musiccabinet.dao.TagDao;
import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;

/*
 * Exposes methods related to tags:
 * 
 * - get a filtered list of available, relevant tags for library
 * - set/get top tags (the ones used for radio / tag cloud)
 * 
 * - get top tags, including their popularity.
 * 
 * - get unique set of tags that are found in library files
 */
public class TagService {

	protected TagDao tagDao;
	
	public List<TagOccurrence> getAvailableTags() {
		return tagDao.getAvailableTags();
	}
	
	public void setTopTags(List<String> topTags) {
		tagDao.setTopTags(topTags);
	}
	
	public List<String> getTopTags() {
		return tagDao.getTopTags();
	}

	public List<TagOccurrence> getTopTagsOccurrence() {
		return tagDao.getTopTagsOccurrence();
	}
	
	public void createTagCorrections(Map<String, String> tagCorrections) {
		tagDao.createTagCorrections(tagCorrections);
	}
	
	public List<String> getFileTags() {
		return tagDao.getFileTags();
	}
	
	// Spring setters

	public void setTagDao(TagDao tagDao) {
		this.tagDao = tagDao;
	}
	
}