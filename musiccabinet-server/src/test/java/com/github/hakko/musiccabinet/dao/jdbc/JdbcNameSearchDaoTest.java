package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.ADD_TO_LIBRARY;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.SearchCriteria;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.library.LibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcNameSearchDaoTest {

	@Autowired
	private JdbcNameSearchDao searchDao;
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Before
	public void clearPreviousData() throws ApplicationException {
		PostgreSQLUtil.loadFunction(searchDao, ADD_TO_LIBRARY);
		
		searchDao.getJdbcTemplate().execute("truncate music.artist cascade");
		searchDao.getJdbcTemplate().execute("truncate library.file cascade");

		submitFile(additionDao, getFile("Casiotone For The Painfully Alone", "Young Shields", "Subway Home"));
		submitFile(additionDao, getFile("Casiotone For The Painfully Alone", "Etiquette", "New Year's Kiss"));
		submitFile(additionDao, getFile("Casiotone For The Painfully Alone", "Etiquette", "I Love Creedence"));
	}

	@Test
	public void findsArtists() {
		Assert.assertTrue(findsArtist("Casiotone for The Painfully Alone"));
		Assert.assertTrue(findsArtist("casioTONE for THE PAINfully ALONE"));
		Assert.assertTrue(findsArtist("Casiotone"));
		Assert.assertTrue(findsArtist("Casioton for painful"));
		Assert.assertTrue(findsArtist("Casio for Pain"));
		
		Assert.assertFalse(findsArtist("Cccazzziotn"));
	}

	private boolean findsArtist(String artistName) {
		NameSearchResult<Artist> artists = searchDao.getArtists(artistName, 0, 10);
		
		Assert.assertNotNull(artists);
		return artists.getResults().size() == 1;
	}

	@Test
	public void findsAlbums() {
		NameSearchResult<Album> albums = searchDao.getAlbums("Casiotone", 0, 10);
		Assert.assertEquals(2, albums.getResults().size());
		
		albums = searchDao.getAlbums("young shield", 0, 10);
		Assert.assertEquals(1, albums.getResults().size());

		albums = searchDao.getAlbums("etiquett", 0, 10);
		Assert.assertEquals(1, albums.getResults().size());
	}

	@Test
	public void findsTracks() {
		NameSearchResult<Track> tracks = searchDao.getTracks("Casiotone", 0, 10);
		Assert.assertEquals(3, tracks.getResults().size());
		
		tracks = searchDao.getTracks("casiotone etiquette", 0, 10);
		Assert.assertEquals(2, tracks.getResults().size());

		tracks = searchDao.getTracks("young shield", 0, 10);
		Assert.assertEquals(1, tracks.getResults().size());

		tracks = searchDao.getTracks("kiss", 0, 10);
		Assert.assertEquals(1, tracks.getResults().size());

	}

	/*
	 * Use reflection to invoke all setXXX methods on a SearchCriteria
	 * object, and pass it to search dao. Not expected to return any
	 * results, only meant to validate syntax of generated SQL code.
	 */
	@Test
	public void searchesUsingAllCriteria() throws Exception {
		SearchCriteria criteria = new SearchCriteria();
		
		for (Method method : SearchCriteria.class.getMethods()) {
			if (SearchCriteria.class.equals(method.getDeclaringClass()) 
					&& method.getName().startsWith("set")) {
				method.invoke(criteria, getParameter(method));
			}
		}
		
		searchDao.getTrackIds(criteria, 0, 10);
	}

	/*
	 * Use reflection to invoke each setXXX methods individually on a
	 * SearchCriteria object, and pass it to search dao. Not expected
	 * to return any results, only meant to validate generated SQL code.
	 */
	@Test
	public void searchesUsingOneCriteria() throws Exception {
		Method[] methods = SearchCriteria.class.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (SearchCriteria.class.equals(method.getDeclaringClass())
					&& method.getName().startsWith("set")) {
				SearchCriteria criteria = new SearchCriteria();
				method.invoke(criteria, getParameter(method));
				searchDao.getTrackIds(criteria, 0, 10);
			}
		}
	}

	private Object getParameter(Method method) {
		Class<?> param = method.getParameterTypes()[0];
		if (String.class.equals(param)) {
			return method.getName().substring("set".length());
		} else if (Short.class.equals(param)) {
			return Short.MAX_VALUE;
		} else if (Set.class.equals(param)) {
			return LibraryUtil.set(0, 1, 2);
		} else {
			return true;
		}
	}
	
	@Test
	public void returnsAvailableFileTypes() {
		List<String> fileTypes = searchDao.getFileTypes();
		
		Assert.assertNotNull(fileTypes);
		Assert.assertEquals(11, fileTypes.size());
		Assert.assertEquals("OGG", fileTypes.get(0));
		Assert.assertEquals("MP3", fileTypes.get(1));
		Assert.assertEquals("FLAC", fileTypes.get(2));
		Assert.assertEquals("M4B", fileTypes.get(10));
	}

}