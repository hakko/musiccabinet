package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;

public abstract class AbstractWSImplementationTest {
	
	protected static void assertHasParameter(List<NameValuePair> params, String name, String value) {
		for (NameValuePair nvp : params) {
			if (nvp.getName().equals(name) && nvp.getValue().equals(value)) {
				return;
			}
		}
		Assert.fail("Missing param " + name + "=" + value + "!");
	}
	
}
