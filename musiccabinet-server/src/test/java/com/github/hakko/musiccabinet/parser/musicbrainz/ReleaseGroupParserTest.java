package com.github.hakko.musiccabinet.parser.musicbrainz;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ReleaseGroupParserTest {

	private static final String RELEASE_GROUP_DJMIX_FILE = "musicbrainz/xml/releaseGroup-djmix.xml";

	private static final String RELEASE_GROUP_FILE = "musicbrainz/xml/releaseGroup.xml";

	// constant values below are copied from file above
	private static final MBAlbum ALBUM_0 = new MBAlbum("The Beyond",
			"236316f7-c919-3986-918b-25e135ba8000", (short) 2003, "Album");
	private static final MBAlbum ALBUM_1 = new MBAlbum("Bodies / Recluse",
			"5484925b-884c-31d8-9c3e-2ef3824e6a5f", (short) 2006, "EP");
	private static final MBAlbum ALBUM_6 = new MBAlbum("Cult of Luna",
			"eb1f6b39-68ce-36c6-9d60-f1cd10c82da5", (short) 2002, "Single");
	private static final MBAlbum ALBUM_7 = new MBAlbum("Salvation",
			"f76d8b42-d8b0-389f-9bb4-2a5d7473c231", (short) 2004, "Album");

	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		ReleaseGroupParser parser = new ReleaseGroupParserImpl(new ResourceUtil(
				RELEASE_GROUP_FILE).getInputStream());
		List<MBAlbum> albums = parser.getAlbums();

		assertEquals(ALBUM_0, albums.get(0));
		assertEquals(ALBUM_1, albums.get(1));
		assertEquals(ALBUM_6, albums.get(6));
		assertEquals(ALBUM_7, albums.get(7));
		
		assertEquals(8, parser.getTotalAlbums());
	}
	
	@Test
	public void groupsUnknownAlbumTypesAsOther() throws ApplicationException {
		ReleaseGroupParser parser = new ReleaseGroupParserImpl(new ResourceUtil(
				RELEASE_GROUP_DJMIX_FILE).getInputStream());
		List<MBAlbum> albums = parser.getAlbums();

		assertEquals(1, albums.size());
		assertEquals(10, albums.get(0).getTypeId()); // OTHER
	}

}