package com.github.hakko.musiccabinet.service.library;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jaudiotagger.tag.datatype.Artwork;
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

	/*
	 * File id3v1+2.mp3 has different id3v1 and id3v2 tags. Assert v2 takes priority.
	 */
	@Test
	public void prefersId3V2Tags() throws Exception {
		java.io.File musicFile = new java.io.File(Thread.currentThread()
				.getContextClassLoader().getResource("library/id3v1+2.mp3").toURI());
	
		Assert.assertTrue(musicFile.exists());
		Assert.assertTrue(musicFile.canRead());
		
		File fileHandle = new File(musicFile.getParent(), musicFile.getName(), 
				new DateTime(), 5717);
		audioTagService.updateMetadata(fileHandle);
		
		Assert.assertEquals("V2 Title", fileHandle.getMetadata().getTitle());
		Assert.assertEquals("V2 Album", fileHandle.getMetadata().getAlbum());
		Assert.assertEquals("V2 Artist", fileHandle.getMetadata().getArtist());
	}

	@Test
	public void readsMp3V1TagAsISO88591() throws Exception {
		java.io.File musicFile = new java.io.File(Thread.currentThread()
				.getContextClassLoader().getResource("library/id3v1.mp3").toURI());
	
		Assert.assertTrue(musicFile.exists());
		Assert.assertTrue(musicFile.canRead());
		
		File fileHandle = new File(musicFile.getParent(), musicFile.getName(), 
				new DateTime(), 5717);
		audioTagService.updateMetadata(fileHandle);
		
		Assert.assertEquals("Å", fileHandle.getMetadata().getTitle());
		Assert.assertEquals("Ä", fileHandle.getMetadata().getAlbum());
		Assert.assertEquals("Ö", fileHandle.getMetadata().getArtist());
	}

	@Test
	public void readsMp3V2TagAsUTF8() throws Exception {
		java.io.File musicFile = new java.io.File(Thread.currentThread()
				.getContextClassLoader().getResource("library/id3v2b.mp3").toURI());
	
		Assert.assertTrue(musicFile.exists());
		Assert.assertTrue(musicFile.canRead());
		
		File fileHandle = new File(musicFile.getParent(), musicFile.getName(), 
				new DateTime(), 5717);
		audioTagService.updateMetadata(fileHandle);
		
		Assert.assertEquals("Title: Ǥ", fileHandle.getMetadata().getTitle());
		Assert.assertEquals("Album: ȡ", fileHandle.getMetadata().getAlbum());
		Assert.assertEquals("Artist: Ƕ", fileHandle.getMetadata().getArtist());
	}

	@Test
	public void identifiesAudioFiles() {
		Assert.assertTrue(audioTagService.isAudioFile("mp3"));
		Assert.assertTrue(audioTagService.isAudioFile("MP3"));

		Assert.assertFalse(audioTagService.isAudioFile("jpg"));
		Assert.assertFalse(audioTagService.isAudioFile("PNG"));
		Assert.assertFalse(audioTagService.isAudioFile(null));
	}
	
	@Test
	public void returnsEmbeddedArtwork() throws Exception {
		String embeddedArtworkFile = "library/media3/Artist/Embedded artwork/Embedded artwork.mp3";
		java.io.File musicFile = new java.io.File(Thread.currentThread()
				.getContextClassLoader().getResource(embeddedArtworkFile).toURI());

		Assert.assertTrue(musicFile.exists());
		Assert.assertTrue(musicFile.canRead());
		
		Artwork artwork = audioTagService.getArtwork(musicFile);
		Assert.assertNotNull(artwork);
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