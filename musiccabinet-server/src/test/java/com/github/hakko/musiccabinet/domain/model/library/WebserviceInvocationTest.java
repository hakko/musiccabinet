package com.github.hakko.musiccabinet.domain.model.library;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

public class WebserviceInvocationTest {

	@Test
	public void validateEnum() {
		Set<Integer> databaseIds = new HashSet<Integer>();
		
		for (WebserviceInvocation.Calltype callType : WebserviceInvocation.Calltype.values()) {
			Assert.assertFalse(databaseIds.contains(callType.getDatabaseId()));
			databaseIds.add(callType.getDatabaseId());
			Assert.assertNotNull(WebserviceInvocation.Calltype.valueOf(callType.name()));
		}
		
	}
	
}