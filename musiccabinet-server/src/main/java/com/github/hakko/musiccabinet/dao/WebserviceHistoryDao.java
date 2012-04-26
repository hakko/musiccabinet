package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public interface WebserviceHistoryDao {

	// Log that a certain WebserviceInvocation happened
	// (to prevent the same question to be asked again shortly)
	void logWebserviceInvocation(WebserviceInvocation webserviceInvocation);

	// Log that a certain WebserviceInvocation happened,
	// but failed in a way that couldn't be recovered (not just temporary
	// offline or so, probably a weird artist tag with loads of guest artists etc).
	// Quarantine it to prevent it from being invoked again in a month.
	void quarantineWebserviceInvocation(WebserviceInvocation webserviceInvocation);
	
	boolean isWebserviceInvocationAllowed(WebserviceInvocation webserviceInvocation);
	
	List<Artist> getArtistsScheduledForUpdate(WebserviceInvocation.Calltype callType);
	
}
