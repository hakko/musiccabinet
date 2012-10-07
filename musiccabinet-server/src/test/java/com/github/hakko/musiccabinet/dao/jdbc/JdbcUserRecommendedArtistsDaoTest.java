package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_USER_RECOMMENDED_ARTISTS;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists;
import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists.RecommendedArtist;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.UserRecommendedArtistsParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcUserRecommendedArtistsDaoTest {

	/* Test data */
	private UserRecommendedArtists joanRec, rjRec, ftpareaRec;

	private static final LastFmUser joan = new LastFmUser("joan");
	private static final LastFmUser rj = new LastFmUser("rj");
	private static final LastFmUser ftparea = new LastFmUser("ftparea");
	
	private static final String JOAN_FILE = 
			"last.fm/xml/userrecommendedartists.joanofarctan.xml";
	private static final String RJ_FILE = 
			"last.fm/xml/userrecommendedartists.rj.xml";
	private static final String FTPAREA_FILE = 
			"last.fm/xml/userrecommendedartists.ftparea.xml";
	
	@Autowired
	private JdbcUserRecommendedArtistsDao dao;

	@Autowired
	private JdbcArtistRecommendationDao recDao;
	
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Autowired
	private JdbcArtistInfoDao artistInfoDao;

	private static final int OFFSET = 0;
	private static final int LIMIT = 50;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_USER_RECOMMENDED_ARTISTS);

		joanRec = new UserRecommendedArtists(joan, 
				new UserRecommendedArtistsParserImpl(new ResourceUtil(
						JOAN_FILE).getInputStream()).getArtists());

		rjRec = new UserRecommendedArtists(rj, 
				new UserRecommendedArtistsParserImpl(new ResourceUtil(
						RJ_FILE).getInputStream()).getArtists());

		String body = new WSResponse(new ResourceUtil(FTPAREA_FILE).getContent()).getResponseBody();
		ftpareaRec = new UserRecommendedArtists(ftparea, 
				new UserRecommendedArtistsParserImpl(new StringUtil(
						body).getInputStream()).getArtists());

		createArtistMetaData();
	}
	
	private void createArtistMetaData() {
		Set<Artist> artists = new HashSet<>();
		for (UserRecommendedArtists ura : Arrays.asList(joanRec, rjRec, ftpareaRec)) {
			for (RecommendedArtist rec : ura.getArtists()) {
				artists.add(rec.getArtist());
			}
		}
		List<File> files = new ArrayList<>();
		for (Artist artist : artists) {
			files.add(getFile(artist.getName(), null, null));
		}
		List<ArtistInfo> artistInfos = new ArrayList<>();
		for (Artist artist : artists) {
			artistInfos.add(new ArtistInfo(artist, "/url/to/" + artist.getName()));
		}

		additionDao.getJdbcTemplate().execute("truncate library.directory cascade");
		UnittestLibraryUtil.submitFile(additionDao, files);
		artistInfoDao.createArtistInfo(artistInfos);
	}

	@Test
	public void validateSingleImport() {
		deleteUserRecommendedArtists();
		
		dao.createUserRecommendedArtists(Arrays.asList(joanRec));
		
		List<ArtistRecommendation> artists;
		
		artists = recDao.getRecommendedArtistsInLibrary(rj.getLastFmUsername(), OFFSET, LIMIT, true);
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());

		artists = recDao.getRecommendedArtistsInLibrary(joan.getLastFmUsername(), 0, 1, true);
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertEquals("Quest.Room.Project", artists.get(0).getArtistName());
		
		artists = recDao.getRecommendedArtistsInLibrary(joan.getLastFmUsername(), 1, 10, true);
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertEquals("Senior Soul", artists.get(0).getArtistName());
	}

	@Test
	public void validateSingleImportOfTwoUsers() {
		deleteUserRecommendedArtists();
		
		dao.createUserRecommendedArtists(Arrays.asList(joanRec, rjRec));
		
		List<ArtistRecommendation> artists;
		
		artists = recDao.getRecommendedArtistsInLibrary(joan.getLastFmUsername(), OFFSET, LIMIT, true);
		validateEquality(joanRec.getArtists(), artists);

		artists = recDao.getRecommendedArtistsInLibrary(rj.getLastFmUsername(), OFFSET, LIMIT, true);
		validateEquality(rjRec.getArtists(), artists);
		
		Assert.assertEquals(0, recDao.getRecommendedArtistsNotInLibrary(
				rj.getLastFmUsername(), 10, true).size());
	}
	
	@Test
	public void validateImportOfContextArtists() {
		deleteUserRecommendedArtists();
		
		dao.createUserRecommendedArtists(Arrays.asList(ftpareaRec));
		List<RecommendedArtist> daoRec = 
				dao.getUserRecommendedArtists(ftparea.getLastFmUsername());

		Assert.assertEquals(ftpareaRec.getArtists().size(), daoRec.size());
		for (int i = 0; i < ftpareaRec.getArtists().size(); i++) {
			Assert.assertEquals(ftpareaRec.getArtists().get(i), daoRec.get(i));
		}
	}

	private void validateEquality(List<RecommendedArtist> artists, List<ArtistRecommendation> recs) {
		assertNotNull(recs);
		assertEquals(artists.size(), recs.size());
		
		for (int i = 0; i < artists.size(); i++) {
			String artistName = artists.get(i).getArtist().getName();
			assertEquals(artistName, recs.get(i).getArtistName());
			assertEquals("/url/to/" + artistName, recs.get(i).getImageUrl());
		}
	}
	
	private void deleteUserRecommendedArtists() {
		dao.getJdbcTemplate().execute("truncate music.userrecommendedartist cascade");
	}

}