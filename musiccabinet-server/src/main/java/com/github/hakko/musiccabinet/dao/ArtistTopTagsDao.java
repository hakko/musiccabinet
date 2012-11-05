package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;

public interface ArtistTopTagsDao {

	void createTopTags(Artist artist, List<Tag> tags);
	List<Tag> getTopTags(int artistId);
	List<Tag> getTopTags(int artistId, int limit);
	
	void updateTopTag(int artistId, TagOccurrence tagOccurrence);

}