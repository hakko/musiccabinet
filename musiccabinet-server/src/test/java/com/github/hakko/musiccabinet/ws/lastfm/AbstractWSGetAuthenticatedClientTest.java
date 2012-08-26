package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.API_SEC;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_API_KEY;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_API_SIG;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_METHOD;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_TOKEN;
import static java.security.MessageDigest.getInstance;
import static org.apache.commons.codec.binary.Hex.encodeHex;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class AbstractWSGetAuthenticatedClientTest {
	
	@Test
	public void addsCalculatedSignature() throws ApplicationException, IOException, NoSuchAlgorithmException {
		
		final String METHOD = "auth.getSession";
		final String TOKEN = "token";
		final String API_KEY = "api_key";

		String signature = 
				PARAM_API_KEY + API_KEY + 
				PARAM_METHOD + METHOD + 
				PARAM_TOKEN + TOKEN + API_SEC;
		final String API_SIG = new String(encodeHex(getInstance("md5").digest(signature.getBytes(UTF8))));

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_TOKEN, TOKEN));
		params.add(new BasicNameValuePair(PARAM_API_KEY, API_KEY));

		TestWSGetAuthenticatedClient testWSClient = new TestWSGetAuthenticatedClient(params);
		testWSClient.testCall();

		Assert.assertNotNull(params);
		
		// should have sorted original parameters
		Assert.assertEquals(PARAM_API_KEY, params.get(0).getName());
		Assert.assertEquals(PARAM_METHOD, params.get(1).getName());
		Assert.assertEquals(PARAM_TOKEN, params.get(2).getName());
		
		// should have added one new element
		Assert.assertEquals(4, params.size());
		Assert.assertEquals(PARAM_API_SIG, params.get(3).getName());
		Assert.assertEquals(API_SIG, params.get(3).getValue());
	}

}