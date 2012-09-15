package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public interface LastFmDao {

	int getLastFmUserId(String lastFmUsername);
	LastFmUser getLastFmUser(String lastFmUsername);

	void createOrUpdateLastFmUser(LastFmUser lastFmUser);
	
	int getLastFmGroupId(String lastFmGroupName);
	List<LastFmGroup> getLastFmGroups();
	void setLastFmGroups(List<LastFmGroup> lastFmGroups);
	
}