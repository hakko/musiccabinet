package com.github.hakko.musiccabinet.dao;

import java.util.List;
import java.util.Map;

import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;

public interface TagDao {

	void createTags(List<String> tags);
	void createTagCorrections(Map<String, String> tagCorrections);
	List<TagOccurrence> getAvailableTags();
	List<String> getTopTags();
	void setTopTags(List<String> topTags);
	List<TagOccurrence> getTopTagsOccurrence();
	
}
