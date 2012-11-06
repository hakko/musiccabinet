package com.github.hakko.musiccabinet.service;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.ArtistTopTagsDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistUserTag;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.ws.lastfm.TagUpdateClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class TagUpdateServiceTest {

	@Autowired
	private TagUpdateService injectedService;
	
	private TagUpdateService tagUpdateService = new TagUpdateService();

	private Artist artist1 = new Artist(1, "artist1"), 
			artist2 = new Artist(2, "artist2"),
			artist3 = new Artist(3, "artist3");
	private LastFmUser user1 = new LastFmUser("user1", "sessionKey1");
	private ArtistUserTag 
			artist1Addition = new ArtistUserTag(artist1, user1, "disco", 100, true),
			artist2Removal = new ArtistUserTag(artist2, user1, "disco", 5, false),
			artist3Addition = new ArtistUserTag(artist3, user1, "disco", 50, true);
	
	private WSResponse responseOK, responseFail;

	@Before
	public void createTestData() throws ApplicationException {
		responseOK = new WSResponse("<lfm status=\"ok\"></lfm>");
		responseFail = new WSResponse(false, 404, "Not found");
	}

	@Test
	public void serviceConfigured() {
		assertNotNull(injectedService.lastFmDao);
		assertNotNull(injectedService.artistTopTagsDao);
		assertNotNull(injectedService.tagUpdateClient);
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
	public void duplicateUpdatesAreRemoved() throws ApplicationException {
		tagUpdateService.failedUpdates.clear();
		setClientResponse(responseFail);

		ArtistTopTagsDao artistTopTagsDao = mock(ArtistTopTagsDao.class);
		tagUpdateService.setArtistTopTagsDao(artistTopTagsDao);
		
		tagUpdateService.register(artist1Addition);
		tagUpdateService.register(artist1Addition);
		tagUpdateService.register(artist1Addition);
		tagUpdateService.updateTags();

		// dupes removed, one item updated and put on fail queue
		assertEquals(1, tagUpdateService.failedUpdates.size()); 
	}

	@Test
	public void updatesWithinThresholdsAreIgnored() throws ApplicationException {
		ArtistTopTagsDao artistTopTagsDao = mock(ArtistTopTagsDao.class);
		tagUpdateService.setArtistTopTagsDao(artistTopTagsDao);

		setClientResponse(responseOK);
		TagUpdateClient tagUpdateClient = tagUpdateService.tagUpdateClient;
		
		tagUpdateService.register(artist1Addition);
		tagUpdateService.register(artist2Removal);
		tagUpdateService.register(artist3Addition);
		tagUpdateService.updateTags();

		verify(tagUpdateClient, times(1)).updateTag(artist1Addition);
		verify(tagUpdateClient, times(1)).updateTag(artist2Removal);
		verify(tagUpdateClient, times(0)).updateTag(artist3Addition);
	}
	
	private void setClientResponse(WSResponse response) throws ApplicationException {
		TagUpdateClient tagUpdateClient = mock(TagUpdateClient.class);
		when(tagUpdateClient.updateTag(any(ArtistUserTag.class))).thenReturn(response);
		tagUpdateService.setTagUpdateClient(tagUpdateClient);
	}
	
}