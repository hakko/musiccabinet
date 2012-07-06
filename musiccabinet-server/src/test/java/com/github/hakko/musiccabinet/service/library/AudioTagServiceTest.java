package com.github.hakko.musiccabinet.service.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class AudioTagServiceTest {

	@Autowired
	private AudioTagService audioTagService;
	
	@Test
	public void readsMP3Tags() throws Exception {
		java.io.File musicFile = new java.io.File(Thread.currentThread()
				.getContextClassLoader().getResource("library/boing.mp3").toURI());

		Assert.assertTrue(musicFile.exists());
		Assert.assertTrue(musicFile.canRead());
		
		File fileHandle = new File(musicFile.getParent(), musicFile.getName(), 
				new DateTime(), 5717);
		audioTagService.updateMetadata(fileHandle);
		
		Assert.assertNotNull(fileHandle.getMetadata());
		
		validateTags(fileHandle.getMetadata());
	}
	
	private void validateTags(MetaData metaData) {
		assertEquals("Artist Name", metaData.getArtist());
		assertEquals("Album Artist", metaData.getAlbumArtist());
		assertEquals("Track Title", metaData.getTitle());
		assertEquals("Album Title", metaData.getAlbum());
		assertEquals("Genre", metaData.getGenre());
		assertEquals("Composer", metaData.getComposer());
		
		assertTrue(metaData.getYear() == (short) 2012);
		assertTrue(metaData.getTrackNr() == (short) 1);
		assertTrue(metaData.getTrackNrs() == (short) 2);
		assertTrue(metaData.getDiscNr() == (short) 3);
		assertTrue(metaData.getDiscNrs() == (short) 4);
	}

}