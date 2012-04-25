package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.TagInfo;

public interface TagInfoDao {

	void createTagInfo(List<TagInfo> tagInfos);
	String getTagInfo(String tagName);
	List<String> getTagsWithInfo();
	
}