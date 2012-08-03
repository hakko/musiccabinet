package com.github.hakko.musiccabinet.service.lastfm;

import java.util.Set;
import java.util.TreeSet;

import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;

public class WebserviceHistoryService {

	protected WebserviceHistoryDao historyDao;
	protected SearchIndexUpdateSettingsService settingsService;
	
	private boolean onlyUpdateNewArtists = false;

	public boolean isOnlyUpdateNewArtists() {
		return onlyUpdateNewArtists;
	}

	public void setOnlyUpdateNewArtists(boolean onlyUpdateNewArtists) {
		this.onlyUpdateNewArtists = onlyUpdateNewArtists;
	}

	
	public void logWebserviceInvocation(WebserviceInvocation invocation) {
		historyDao.logWebserviceInvocation(invocation);
	}

	public void quarantineWebserviceInvocation(WebserviceInvocation invocation) {
		historyDao.quarantineWebserviceInvocation(invocation);
	}

	public void blockWebserviceInvocation(int artistId, WebserviceInvocation.Calltype callType) {
		historyDao.blockWebserviceInvocation(artistId, callType);
	}
	
	public boolean isWebserviceInvocationAllowed(WebserviceInvocation invocation) {
		return historyDao.isWebserviceInvocationAllowed(invocation);
	}

	public Set<String> getArtistNamesScheduledForUpdate(WebserviceInvocation.Calltype callType) {
		// ordered set, to make sure the iteration order is predictable.
		// there's an upper limit of 3000 artist names returned here, and
		// we want to update artist info / related artists / top tracks etc
		// for the same ones. So that an artist either has all or none meta data.
		Set<String> artistNames = new TreeSet<>(historyDao.getArtistNamesWithNoInvocations(callType));
		if (!onlyUpdateNewArtists) {
			artistNames.addAll(historyDao.getArtistNamesWithOldestInvocations(callType));
		}
		return artistNames;
	}

	// Spring setters
	
	public void setWebserviceHistoryDao(WebserviceHistoryDao webserviceHistoryDao) {
		this.historyDao = webserviceHistoryDao;
	}

	public void setSearchIndexUpdateSettingsService(SearchIndexUpdateSettingsService settingsService) {
		this.settingsService = settingsService;
	}
	
}