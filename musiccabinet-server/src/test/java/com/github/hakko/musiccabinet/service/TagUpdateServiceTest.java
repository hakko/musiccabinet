package com.github.hakko.musiccabinet.service;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.message.GenericMessage;

import com.github.hakko.musiccabinet.dao.ArtistTopTagsDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistUserTag;
import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.ws.lastfm.TagUpdateClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

public class TagUpdateServiceTest {

	private TagUpdateService tagUpdateService = new TagUpdateService();

	private Artist artist1 = new Artist(1, "artist1");
	private LastFmUser user1 = new LastFmUser("user1", "sessionKey1");
	private ArtistUserTag artist1Addition = new ArtistUserTag(artist1, user1, 
			new TagOccurrence("disco", null, 100, true));
	
	private WSResponse responseOK, responseFail;

	@Before
	public void createTestData() throws ApplicationException {
		responseOK = new WSResponse("<lfm status=\"ok\"></lfm>");
		responseFail = new WSResponse(false, 404, "Not found");
	}
	
	@Test
	public void resendsFailedUpdates() throws ApplicationException {
		tagUpdateService.failedUpdates.add(artist1Addition); // add item to fail queue

		setClientResponse(responseFail);
		tagUpdateService.resendFailedUpdates();
		assertEquals(1, tagUpdateService.failedUpdates.size()); // item still in queue

		setClientResponse(responseOK);
		tagUpdateService.resendFailedUpdates();
		assertEquals(0, tagUpdateService.failedUpdates.size()); // item removed from queue
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void duplicateUpdatesAreRemoved() throws ApplicationException {
		tagUpdateService.failedUpdates.clear();
		setClientResponse(responseFail);

		PollableChannel tagUpdateChannel = mock(PollableChannel.class);
		Message message = new GenericMessage<ArtistUserTag>(artist1Addition);
		when(tagUpdateChannel.receive()).thenReturn(message, message, message, null);
		tagUpdateService.setTagUpdateChannel(tagUpdateChannel);
		ArtistTopTagsDao artistTopTagsDao = mock(ArtistTopTagsDao.class);
		tagUpdateService.setArtistTopTagsDao(artistTopTagsDao);
		
		tagUpdateService.receive();
		tagUpdateService.updateTags();

		// dupes removed, one item updated and put on fail queue
		verify(artistTopTagsDao, times(1)).updateTopTag(
				any(Artist.class), any(TagOccurrence.class));
		assertEquals(1, tagUpdateService.failedUpdates.size()); 
	}
	
	private void setClientResponse(WSResponse response) throws ApplicationException {
		TagUpdateClient tagUpdateClient = mock(TagUpdateClient.class);
		when(tagUpdateClient.updateTag(any(ArtistUserTag.class))).thenReturn(response);
		tagUpdateService.setTagUpdateClient(tagUpdateClient);
	}
	
}