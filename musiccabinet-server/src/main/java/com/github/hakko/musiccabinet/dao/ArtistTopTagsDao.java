package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;

public interface ArtistTopTagsDao {

	void createTopTags(Artist artist, List<Tag> tags);
	List<Tag> getTopTags(Artist artist);

}