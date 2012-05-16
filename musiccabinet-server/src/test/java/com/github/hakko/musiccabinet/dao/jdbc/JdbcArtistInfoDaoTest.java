package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ARTISTINFO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistInfoParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcArtistInfoDaoTest {

	/* Test data */
	private ArtistInfo aiAbba, aiCher, aiTina;

	private static final String AI_ABBA_FILE = 
			"last.fm/xml/artistinfo.abba.xml";
	private static final String AI_CHER_FILE = 
			"last.fm/xml/artistinfo.cher.xml";
	private static final String AI_TINA_FILE = 
			"last.fm/xml/artistinfo.tinaturner.xml";

	/* Expected outcome */
	private static final String ABBA_IMAGE_URL = 
			"http://userserve-ak.last.fm/serve/126/44442235.png";
	private static final String ABBA_BIO_SUMMARY =
			"ABBA was a <a href=\"http://www.last.fm/tag/pop\" class=\"bbcode_tag\" rel=\"tag\">pop</a> music group formed in Stockholm, Sweden in November 1970. The band consisted of <a href=\"http://www.last.fm/music/Anni-Frid+Lyngstad\" class=\"bbcode_artist\">Anni-Frid Lyngstad</a> (<a href=\"http://www.last.fm/music/Frida\" class=\"bbcode_artist\">Frida</a>), <a href=\"http://www.last.fm/music/Bj%C3%B6rn+Ulvaeus\" class=\"bbcode_artist\">Bj&ouml;rn Ulvaeus</a>, <a href=\"http://www.last.fm/music/Benny+Andersson\" class=\"bbcode_artist\">Benny Andersson</a>, and <a href=\"http://www.last.fm/music/Agnetha+F%C3%A4ltskog\" class=\"bbcode_artist\">Agnetha F&auml;ltskog</a>. (See also <a href=\"http://www.last.fm/music/Bj%25C3%25B6rn%2BUlvaeus%2B%2526%2BBenny%2BAndersson\" class=\"bbcode_artist\">Bj&ouml;rn Ulvaeus &amp; Benny Andersson</a>, as well as Benny's previous band <a href=\"http://www.last.fm/music/Hep+Stars\" class=\"bbcode_artist\">Hep Stars</a>.)  They topped the charts worldwide from 1972 to 1982 with eight studio albums, achieving twenty-six #1 singles and numerous awards. They also won the 1974 Eurovision Song Contest with <a title=\"ABBA &ndash; Waterloo\" href=\"http://www.last.fm/music/ABBA/_/Waterloo\" class=\"bbcode_track\">Waterloo</a>.  ";

	
	@Autowired
	private JdbcArtistInfoDao dao;

	@Autowired
	private JdbcMusicDao musicDao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_ARTISTINFO);
		
		aiAbba = new ArtistInfoParserImpl(new ResourceUtil(
				AI_ABBA_FILE).getInputStream()).getArtistInfo();
		aiCher = new ArtistInfoParserImpl(new ResourceUtil(
				AI_CHER_FILE).getInputStream()).getArtistInfo();
		aiTina = new ArtistInfoParserImpl(new ResourceUtil(
				AI_TINA_FILE).getInputStream()).getArtistInfo();
		
		deleteArtists();

		// re-create artists
		for (ArtistInfo ai : new ArtistInfo[]{aiAbba, aiCher, aiTina}) {
			musicDao.getArtistId(ai.getArtist().getName());
		}
		
	}
	
	@Test
	public void createAndValidateArtistInfos() throws ApplicationException {
		deleteArtistInfos();

		List<ArtistInfo> artistInfos = new ArrayList<ArtistInfo>();
		artistInfos.add(aiAbba);
		artistInfos.add(aiCher);
		
		dao.createArtistInfo(artistInfos);
		
		ArtistInfo dbAbba = dao.getArtistInfo(aiAbba.getArtist());
		ArtistInfo dbCher = dao.getArtistInfo(aiCher.getArtist());
		
		Assert.assertEquals(aiAbba, dbAbba);
		Assert.assertEquals(aiCher, dbCher);
	}

	@Test
	public void createAndValidateUpdatedArtistInfos() throws ApplicationException {
		deleteArtistInfos();

		String newBio = "Abba was a pop group.";
		String newContent = "Abba was a pop group. They sold many records.";
		
		dao.createArtistInfo(Arrays.asList(aiAbba, aiTina));
		
		aiAbba.setBioSummary(newBio);
		aiAbba.setBioContent(newContent);
		
		dao.createArtistInfo(Arrays.asList(aiAbba, aiCher));
		
		ArtistInfo dbAbba = dao.getArtistInfo(aiAbba.getArtist());
		
		Assert.assertEquals(newBio, dbAbba.getBioSummary());
		Assert.assertEquals(newContent, dbAbba.getBioContent());
	}
	
	@Test
	public void biographyAndImageUrlAreReturnedAsInfo() throws ApplicationException {
		deleteArtistInfos();

		int abbaId = musicDao.getArtistId(aiAbba.getArtist().getName());

		dao.createArtistInfo(Arrays.asList(aiAbba));
		ArtistInfo dbAbba = dao.getArtistInfo(abbaId);
		
		Assert.assertEquals(ABBA_IMAGE_URL, dbAbba.getLargeImageUrl());
		Assert.assertEquals(ABBA_BIO_SUMMARY, dbAbba.getBioSummary());
	}
	
	private void deleteArtists() {
		dao.getJdbcTemplate().execute("truncate music.artist cascade");
	}
	
	private void deleteArtistInfos() {
		dao.getJdbcTemplate().execute("truncate music.artistinfo cascade");
	}
	
}