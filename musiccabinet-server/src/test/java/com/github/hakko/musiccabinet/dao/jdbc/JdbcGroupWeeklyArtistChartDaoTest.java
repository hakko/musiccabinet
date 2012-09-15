package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_GROUP_WEEKLY_ARTIST_CHART;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_LASTFMGROUP;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;

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
import com.github.hakko.musiccabinet.domain.model.aggr.GroupWeeklyArtistChart;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.GroupWeeklyArtistChartParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcGroupWeeklyArtistChartDaoTest {

	/* Test data */
	private GroupWeeklyArtistChart artistChart;

	private static final LastFmGroup brainwashed = new LastFmGroup("brainwashed");
	
	private static final String BRAINWASHED_FILE = 
			"last.fm/xml/group.weeklyartistchart.xml";
	
	@Autowired
	private JdbcGroupWeeklyArtistChartDao dao;
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Autowired
	private JdbcArtistInfoDao artistInfoDao;

	@Autowired
	private JdbcArtistRecommendationDao recDao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_GROUP_WEEKLY_ARTIST_CHART);
		PostgreSQLUtil.loadFunction(dao, UPDATE_LASTFMGROUP);

		artistChart = new GroupWeeklyArtistChart(brainwashed.getName(), 
				new GroupWeeklyArtistChartParserImpl(new ResourceUtil(
						BRAINWASHED_FILE).getInputStream()).getArtistPlayCount());

		createArtistMetaData();
	}
	
	private void createArtistMetaData() {
		Set<Artist> artists = new HashSet<>();
		for (int i = 0; i < 3; i++) {
			artists.add(artistChart.getArtistPlayCounts().get(i).getArtist());
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
		additionDao.getJdbcTemplate().execute("truncate library.artist cascade");
		additionDao.getJdbcTemplate().execute("truncate music.groupweeklyartistchart cascade");
		
		UnittestLibraryUtil.submitFile(additionDao, files);
		artistInfoDao.createArtistInfo(artistInfos);
		
		dao.createArtistCharts(Arrays.asList(artistChart));
	}

	@Test
	public void validateSingleImport() {
		GroupWeeklyArtistChart daoChart = dao.getWeeklyArtistChart(brainwashed);

		Assert.assertNotNull(daoChart);
		Assert.assertNotNull(daoChart.getArtistPlayCounts());
		
		Assert.assertEquals(artistChart, daoChart);
	}

	@Test
	public void browseGroupArtists() {
		List<ArtistRecommendation> artists;

		artists = recDao.getGroupArtistsInLibrary(brainwashed.getName(), 0, 1, true);
		Assert.assertEquals("Swans", artists.get(0).getArtistName());

		artists = recDao.getGroupArtistsInLibrary(brainwashed.getName(), 1, 2, true);
		Assert.assertEquals("Coil", artists.get(0).getArtistName());
		Assert.assertEquals("Current 93", artists.get(1).getArtistName());
	}

	@Test
	public void getGroupRecommendations() {
		List<String> names = recDao.getGroupArtistsNotInLibrary(brainwashed.getName(), 3, true);
		
		Assert.assertNotNull(names);
		Assert.assertEquals(3, names.size());
		
		Assert.assertEquals("Dead Can Dance", names.get(0));
		Assert.assertEquals("Four Tet", names.get(1));
		Assert.assertEquals("The Legendary Pink Dots", names.get(2));
	}

}