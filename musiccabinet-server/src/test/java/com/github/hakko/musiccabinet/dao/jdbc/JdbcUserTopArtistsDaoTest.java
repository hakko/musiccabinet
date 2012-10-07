package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_USER_TOP_ARTISTS;
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
import com.github.hakko.musiccabinet.domain.model.aggr.UserTopArtists;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.UserTopArtistsParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcUserTopArtistsDaoTest {

	/* Test data */
	private UserTopArtists arnOverall, arn6month, sys3month;

	private static final LastFmUser arn = new LastFmUser("arnathalon");
	private static final LastFmUser sys = new LastFmUser("SysterV");
	
	private static final String ARN_OVERALL_FILE = 
			"last.fm/xml/usertopartists.arnathalon.overall.xml";
	private static final String ARN_6MONTH_FILE = 
			"last.fm/xml/usertopartists.arnathalon.6month.xml";
	private static final String SYS_3MONTH_FILE = 
			"last.fm/xml/usertopartists.systerv.3month.xml";
	
	@Autowired
	private JdbcUserTopArtistsDao dao;
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Autowired
	private JdbcArtistInfoDao artistInfoDao;

	private static final int OFFSET = 0;
	private static final int LIMIT = 50;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_USER_TOP_ARTISTS);

		arnOverall = new UserTopArtists(arn, Period.OVERALL,
				new UserTopArtistsParserImpl(new ResourceUtil(
						ARN_OVERALL_FILE).getInputStream()).getArtists());
		arn6month = new UserTopArtists(arn, Period.SIX_MONTHS,
				new UserTopArtistsParserImpl(new ResourceUtil(
						ARN_6MONTH_FILE).getInputStream()).getArtists());
		sys3month = new UserTopArtists(sys, Period.THREE_MONTHS,
				new UserTopArtistsParserImpl(new ResourceUtil(
						SYS_3MONTH_FILE).getInputStream()).getArtists());

		createArtistMetaData();
	}
	
	private void createArtistMetaData() {
		Set<Artist> artists = new HashSet<>();
		for (UserTopArtists uta : Arrays.asList(arnOverall, arn6month, sys3month)) {
			for (Artist artist : uta.getArtists()) {
				artists.add(artist);
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

		additionDao.getJdbcTemplate().execute("truncate library.file cascade");
		UnittestLibraryUtil.submitFile(additionDao, files);
		artistInfoDao.createArtistInfo(artistInfos);
	}

	@Test
	public void validateSingleImport() {
		deleteUserTopArtists();
		
		dao.createUserTopArtists(Arrays.asList(arnOverall));
		
		List<ArtistRecommendation> artists;
		
		artists = dao.getUserTopArtists(arn, Period.SIX_MONTHS, OFFSET, LIMIT);
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());

		artists = dao.getUserTopArtists(sys, Period.SIX_MONTHS, OFFSET, LIMIT);
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());

		artists = dao.getUserTopArtists(arn, Period.OVERALL, OFFSET, LIMIT);
		validateEquality(arnOverall.getArtists(), artists);
	}

	@Test
	public void validateSingleImportOfTwoUsers() {
		deleteUserTopArtists();
		
		dao.createUserTopArtists(Arrays.asList(arn6month, sys3month));
		
		List<ArtistRecommendation> artists;
		
		artists = dao.getUserTopArtists(arn, Period.OVERALL, OFFSET, LIMIT);
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());

		artists = dao.getUserTopArtists(arn, Period.SIX_MONTHS, OFFSET, LIMIT);
		validateEquality(arn6month.getArtists(), artists);

		artists = dao.getUserTopArtists(sys, Period.THREE_MONTHS, OFFSET, LIMIT);
		validateEquality(sys3month.getArtists(), artists);
	}

	@Test
	public void validateImportOfAllData() {
		deleteUserTopArtists();
		
		dao.createUserTopArtists(Arrays.asList(arnOverall, arn6month, sys3month));
		
		List<ArtistRecommendation> artists;
		
		artists = dao.getUserTopArtists(arn, Period.OVERALL, OFFSET, LIMIT);
		validateEquality(arnOverall.getArtists(), artists);

		artists = dao.getUserTopArtists(arn, Period.SIX_MONTHS, OFFSET, LIMIT);
		validateEquality(arn6month.getArtists(), artists);

		artists = dao.getUserTopArtists(arn, Period.THREE_MONTHS, OFFSET, LIMIT);
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());

		artists = dao.getUserTopArtists(sys, Period.THREE_MONTHS, OFFSET, LIMIT);
		validateEquality(sys3month.getArtists(), artists);
	}

	private void validateEquality(List<Artist> artists, List<ArtistRecommendation> recs) {
		assertNotNull(recs);
		assertEquals(artists.size(), recs.size());
		
		for (int i = 0; i < artists.size(); i++) {
			String artistName = artists.get(i).getName();
			assertEquals(artistName, recs.get(i).getArtistName());
			assertEquals("/url/to/" + artistName, recs.get(i).getImageUrl());
		}
	}
	
	private void deleteUserTopArtists() {
		dao.getJdbcTemplate().execute("truncate music.usertopartist cascade");
	}

}