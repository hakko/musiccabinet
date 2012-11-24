package com.github.hakko.musiccabinet.service;

import static com.github.hakko.musiccabinet.service.MusicBrainzService.TYPE_ALBUM;
import static com.github.hakko.musiccabinet.service.MusicBrainzService.TYPE_EP;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static org.apache.commons.lang.StringUtils.reverse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryAdditionDao;
import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.ws.musicbrainz.ArtistQueryClient;
import com.github.hakko.musiccabinet.ws.musicbrainz.ReleaseGroupsClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class MusicBrainzServiceTest {

	@Autowired
	private MusicBrainzService service;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	private String artistName = "Cult of Luna", albumName = "Eternal Kingdom", 
			trackName = "Owlwood", mbid = "d347406f-839d-4423-9a28-188939282afa";
	
	@Before
	public void configureService() throws ApplicationException {
		service.setArtistQueryClient(getArtistQueryClient());
		service.setReleaseGroupsClient(getReleaseGroupsClient());
	}
	
	@Before
	public void prepareTestdata() {
		additionDao.getJdbcTemplate().execute("truncate music.artist cascade");
		additionDao.getJdbcTemplate().execute("truncate library.file cascade");

		submitFile(additionDao, getFile(artistName, albumName, trackName));
	}
	
	@Test
	public void updatesDiscography() {
		assertFalse(service.isIndexBeingCreated());
		Assert.assertEquals("0/0 artist ids, 0/0 artist discographies",
				service.getProgressDescription());
		
		service.updateDiscography();
		
		assertFalse(service.isIndexBeingCreated());
		Assert.assertEquals("1/1 artist ids, 1/1 artist discographies",
				service.getProgressDescription());
		
		List<MBAlbum> albums = service.getMissingAlbums(artistName, TYPE_ALBUM, null, -1, 0);
		Assert.assertEquals(4, albums.size());
		
		assertEquals("Cult of Luna", albums.get(0).getTitle());
		assertEquals("The Beyond", albums.get(1).getTitle());
		assertEquals("Salvation", albums.get(2).getTitle());
		assertEquals("Somewhere Along the Highway", albums.get(3).getTitle());
		// Eternal Kingdom exists in library
	}
	
	@Test
	public void handlesArtistFailureDuringUpdate() throws ApplicationException {
		final String revName = reverse(artistName);
		submitFile(additionDao, getFile(revName, albumName, trackName));
		
		Mockito.when(service.artistQueryClient.get(revName)).thenThrow(
			new ApplicationException("Fail", new HttpResponseException(503, "Overloaded")));
		
		service.updateDiscography();
		
		List<MBAlbum> albums = service.getMissingAlbums(artistName, TYPE_EP, null, -1, 0);
		Assert.assertEquals(2, albums.size());
		assertEquals("Switchblade / Cult of Luna", albums.get(0).getTitle());
		assertEquals("Bodies / Recluse", albums.get(1).getTitle());
	}
	
	private ArtistQueryClient getArtistQueryClient() throws ApplicationException {
		ArtistQueryClient client = Mockito.mock(ArtistQueryClient.class);
		
		Mockito.when(client.get(artistName)).thenReturn(
				new ResourceUtil("musicbrainz/xml/artistQuery.xml").getContent());
			
		return client;
	}

	private ReleaseGroupsClient getReleaseGroupsClient() throws ApplicationException {
		ReleaseGroupsClient client = Mockito.mock(ReleaseGroupsClient.class);
		
		Mockito.when(client.get(artistName, mbid, 0)).thenReturn(
				new ResourceUtil("musicbrainz/xml/releaseGroup.xml").getContent());
		
		return client;
	}

}