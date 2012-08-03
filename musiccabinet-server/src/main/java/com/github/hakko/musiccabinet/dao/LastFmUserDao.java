package com.github.hakko.musiccabinet.dao;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public interface LastFmUserDao {

	int getLastFmUserId(String lastFmUsername);
	LastFmUser getLastFmUser(String lastFmUsername);

	void createOrUpdateLastFmUser(LastFmUser lastFmUser);
	
}