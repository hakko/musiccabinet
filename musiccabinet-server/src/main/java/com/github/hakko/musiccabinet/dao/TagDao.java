package com.github.hakko.musiccabinet.dao;

import java.util.List;
import java.util.Map;

import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;
import com.github.hakko.musiccabinet.domain.model.aggr.TagTopArtists;
import com.github.hakko.musiccabinet.domain.model.music.Tag;

public interface TagDao {

	void createTags(List<String> tags);
	List<Tag> getTags();
	void createTagCorrections(Map<String, String> tagCorrections);
	Map<String, String> getCorrectedTags();
	List<TagOccurrence> getAvailableTags();
	List<String> getTopTags();
	void setTopTags(List<String> topTags);
	List<TagOccurrence> getTopTagsOccurrence();

	void createTopArtists(List<TagTopArtists> tagTopArtists);
	List<Tag> getTagsWithoutTopArtists();

}