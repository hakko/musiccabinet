package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;
import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.aggr.ArtistUserTag;
import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TagUpdateClientTest extends AbstractWSImplementationTest {

	private final LastFmUser lastFmUser = new LastFmUser("userName", "sessionKey");
	private final Artist artist = new Artist("artistName");
	private final String tag = "pop";

	@Test
	public void validateParametersForAddingTag() throws ApplicationException {
		final ArtistUserTag artistUserTagAdd = new ArtistUserTag(artist,
				lastFmUser, new TagOccurrence(tag, null, 100, true));

		new TagUpdateClient() {
			@Override
			protected WSResponse executeWSRequest(List<NameValuePair> params)
					throws ApplicationException {
				assertHasParameter(params, PARAM_METHOD, ADD_METHOD);
				assertHasParameter(params, PARAM_ARTIST, artist.getName());
				assertHasParameter(params, PARAM_TAGS, "pop");
				assertHasParameter(params, PARAM_SK, lastFmUser.getSessionKey());

				return null;
			}
		}.updateTag(artistUserTagAdd);
	}

	@Test
	public void validateParametersForRemovingTag() throws ApplicationException {
		final ArtistUserTag artistUserTagRemove = new ArtistUserTag(artist,
				lastFmUser, new TagOccurrence(tag, null, 10, false));

		new TagUpdateClient() {
			@Override
			protected WSResponse executeWSRequest(List<NameValuePair> params)
					throws ApplicationException {
				assertHasParameter(params, PARAM_METHOD, REMOVE_METHOD);
				assertHasParameter(params, PARAM_ARTIST, artist.getName());
				assertHasParameter(params, PARAM_TAG, "pop");
				assertHasParameter(params, PARAM_SK, lastFmUser.getSessionKey());

				return null;
			}
		}.updateTag(artistUserTagRemove);
	}

}