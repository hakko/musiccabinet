package com.github.hakko.musiccabinet.service.library;

import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcArtistTopTagsDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcMusicDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.LastFmSettingsService;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class LibraryTagTest {

	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	@Autowired
	private JdbcArtistTopTagsDao topTagsDao;

	@Autowired
	private JdbcMusicDao musicDao;

	@Autowired
	private LastFmSettingsService settingsService;

	final String ARTIST = "artist", ALBUM = "album", TITLE = "title", POP = "pop", ROCK = "rock";

	@Before
	public void setupLibrary() throws ApplicationException {
		PostgreSQLUtil.truncateTables(additionDao);
		File file1 = UnittestLibraryUtil.getFile(ARTIST, ALBUM, TITLE + "1");
		File file2 = UnittestLibraryUtil.getFile(ARTIST, ALBUM, TITLE + "2");
		File file3 = UnittestLibraryUtil.getFile(ARTIST, ALBUM, TITLE + "3");
		file1.getMetadata().setGenre(POP);
		file2.getMetadata().setGenre(POP);
		file3.getMetadata().setGenre(ROCK);
		UnittestLibraryUtil.submitFile(additionDao, asList(file1, file2, file3));

		settingsService.setPreferLocalGenres(true);
	}

	@Test
	public void findsTopTagsForLocalGenres() throws Exception {
		List<Tag> topTags = topTagsDao.getTopTags(musicDao.getArtistId(ARTIST));

		Assert.assertNotNull(topTags);
		Assert.assertEquals(2, topTags.size());
		Assert.assertEquals(new Tag(POP, (short) 66), topTags.get(0));
		Assert.assertEquals(new Tag(ROCK, (short) 33), topTags.get(1));
	}

	@Test
	public void filtersArtistListOnLocalGenres() throws Exception {
		// expected: pop threshold is 66, rock threshold is 33
		List<Artist> pop100 = browserDao.getArtists(POP, 100);
		List<Artist> pop50 = browserDao.getArtists(POP, 50);
		List<Artist> pop25 = browserDao.getArtists(POP, 25);

		List<Artist> rock100 = browserDao.getArtists(ROCK, 100);
		List<Artist> rock50 = browserDao.getArtists(ROCK, 50);
		List<Artist> rock25 = browserDao.getArtists(ROCK, 25);

		Assert.assertTrue(pop100.isEmpty());
		Assert.assertFalse(pop50.isEmpty());
		Assert.assertFalse(pop25.isEmpty());

		Assert.assertTrue(rock100.isEmpty());
		Assert.assertTrue(rock50.isEmpty());
		Assert.assertFalse(rock25.isEmpty());
	}

}