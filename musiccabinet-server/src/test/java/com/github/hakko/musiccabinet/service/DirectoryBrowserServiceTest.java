package com.github.hakko.musiccabinet.service;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static java.io.File.separatorChar;
import static java.lang.Thread.currentThread;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcDirectoryBrowserDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcMusicDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;
import com.github.hakko.musiccabinet.domain.model.library.Directory;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.library.LibraryScannerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class DirectoryBrowserServiceTest {

	@Autowired
	private LibraryScannerService scannerService;
	
	@Autowired
	private DirectoryBrowserService browserService; 

	@Autowired
	private JdbcDirectoryBrowserDao dao;
	
	// paths to resources folders containing actual, tagged, mp3 files
	private String library, media1, media2, cd1, artwork, media8;
	
	@Before
	public void clearDirectories() throws Exception {
		PostgreSQLUtil.truncateTables(dao);

		library = new File(currentThread().getContextClassLoader()
				.getResource("library").toURI()).getAbsolutePath();
		media1 = library + separatorChar + "media1";
		media2 = library + separatorChar + "media2";
		cd1 = media1 + separatorChar + "The Beatles" + separatorChar 
				+ "1962-1966" + separatorChar + "cd1";
		artwork = library + separatorChar + "media3" + separatorChar + "Artist"
				+ separatorChar + "Folder artwork";
		media8 = library + separatorChar + "media8";
	}
	
	@Test
	public void findsRootDirectories() throws ApplicationException {
		Assert.assertEquals(0, browserService.getRootDirectories().size());
		
		scannerService.add(set(media1));
		Assert.assertEquals(1, browserService.getRootDirectories().size());
		Assert.assertEquals(media1, getFirstRootDirectory().getPath());

		scannerService.add(set(media2));
		Assert.assertEquals(2, browserService.getRootDirectories().size());

		scannerService.delete(set(media1));
		Assert.assertEquals(1, browserService.getRootDirectories().size());
		Assert.assertEquals(media2, getFirstRootDirectory().getPath());

		scannerService.delete(set(media2));
		Assert.assertEquals(0, browserService.getRootDirectories().size());
	}

	@Test
	public void findsSubDirectories() throws ApplicationException {
		scannerService.add(set(media1));
		Directory dirMedia1 = getFirstRootDirectory();
		
		List<Directory> subMedia1 = list(dirMedia1.getId());
		Assert.assertEquals(2, subMedia1.size());
		Assert.assertEquals("Elvis Presley", subMedia1.get(0).getName());
		Assert.assertEquals("The Beatles", subMedia1.get(1).getName());
		
		List<Directory> subTheBeatles = list(subMedia1.get(1).getId());
		Assert.assertEquals(1, subTheBeatles.size());
		Assert.assertEquals("1962-1966", subTheBeatles.get(0).getName());

		List<Directory> sub1962_1966 = list(subTheBeatles.get(0).getId());
		Assert.assertEquals(2, sub1962_1966.size());
		Assert.assertEquals("cd1", sub1962_1966.get(0).getName());
		Assert.assertEquals("cd2", sub1962_1966.get(1).getName());
		
		List<Directory> empty = list(sub1962_1966.get(0).getId());
		Assert.assertEquals(0, empty.size());
	}
	
	@Test
	public void findsParentDirectory() throws ApplicationException {
		scannerService.add(set(media1));
		Directory root = getFirstRootDirectory();
		
		Set<Directory> subDirectories = browserService.getSubDirectories(root.getId());
		Assert.assertEquals(2, subDirectories.size());
		for (Directory subDirectory : subDirectories) {
			Assert.assertEquals(root.getId(), 
					browserService.getParentId(subDirectory.getId()));
		}
	}
	
	@Test
	public void addsDirectory() throws ApplicationException {
		scannerService.add(set(cd1));
		Directory root = getFirstRootDirectory();
		
		browserService.addDirectory(cd1 + separatorChar + "subdir", root.getId());
		
		Set<Directory> subDirs = browserService.getSubDirectories(root.getId());
		Assert.assertFalse(subDirs.isEmpty());
		Assert.assertEquals(1, subDirs.size());
		Assert.assertEquals(cd1 + separatorChar + "subdir",
				subDirs.iterator().next().getPath());
	}
	
	@Test
	public void noChangesWhenListingFiles() throws ApplicationException {
		scannerService.add(set(media1));
		Directory dirMedia1 = getFirstRootDirectory();
		
		DirectoryContent found = browserService.getDirectoryDiff(dirMedia1.getId());
		
		Assert.assertTrue(found.getFiles().isEmpty());
		Assert.assertTrue(found.getSubDirectories().isEmpty());
	}

	@Test
	public void addedSubdirectoryDetectedWhenListingFiles() throws ApplicationException, IOException {
		scannerService.add(set(media1));
		
		File newDir = new File(media1 + separatorChar + "newdir");
		newDir.mkdir();
		newDir.deleteOnExit();
		
		Directory dirMedia1 = getFirstRootDirectory();
		
		DirectoryContent found = browserService.getDirectoryDiff(dirMedia1.getId());
		
		Assert.assertTrue(found.getFiles().isEmpty());
		Assert.assertFalse(found.getSubDirectories().isEmpty());
		Assert.assertEquals(1, found.getSubDirectories().size());
		Assert.assertTrue(found.getSubDirectories().contains(newDir.getAbsolutePath()));
		
		newDir.delete();
	}

	@Test
	public void addedFileDetectedWhenListingFiles() throws ApplicationException, IOException {
		scannerService.add(set(media1));
		
		File newFile = new File(media1 + separatorChar + "newfile");
		newFile.createNewFile();
		newFile.deleteOnExit();
		
		Directory dirMedia1 = getFirstRootDirectory();
		
		DirectoryContent found = browserService.getDirectoryDiff(dirMedia1.getId());
		
		Assert.assertFalse(found.getFiles().isEmpty());
		Assert.assertTrue(found.getSubDirectories().isEmpty());
		Assert.assertEquals(1, found.getFiles().size());
		Assert.assertEquals(found.getFiles().iterator().next().getFilename(), 
				newFile.getName());

		newFile.delete();
	}
	
	@Test
	public void findsSingleAlbumInDirectory() throws ApplicationException {
		scannerService.add(set(cd1));
		int cd1Id = browserService.getRootDirectories().iterator().next().getId();
		
		List<Album> albums = browserService.getAlbums(cd1Id, true, true);
		
		Assert.assertEquals(1, albums.size());
		Album album = albums.get(0);
		
		Assert.assertEquals("The Beatles", album.getArtist().getName());
		Assert.assertEquals("1962-1966", album.getName());
		Assert.assertEquals(4, album.getTrackIds().size());
	}

	@Test
	public void sortsAlbumsByEitherYearOrName() throws Exception {
		scannerService.add(set(media8));
		int directoryId = getFirstRootDirectory().getId();

		Assert.assertEquals("ACB", getAlbumNames(directoryId, true, true));
		Assert.assertEquals("BCA", getAlbumNames(directoryId, true, false));

		Assert.assertEquals("ABC", getAlbumNames(directoryId, false, true));
		Assert.assertEquals("CBA", getAlbumNames(directoryId, false, false));
	}

	private String getAlbumNames(int directoryId, boolean sortByYear, boolean sortAscending) {
		List<Album> albums = browserService.getAlbums(directoryId, sortByYear, sortAscending);

		StringBuilder sb = new StringBuilder();
		for (Album album : albums) {
			sb.append(album.getName());
		}

		return sb.toString();
	}

	@Test
	public void findsNonAudioFiles() throws ApplicationException {
		scannerService.add(set(artwork));
		
		int directoryId = browserService.getRootDirectories().iterator().next().getId();
		
		List<String> files = browserService.getNonAudioFiles(directoryId);

		Assert.assertEquals(2, files.size());
		Assert.assertEquals(files.get(0), "cover.jpg");
		Assert.assertEquals(files.get(1), "folder.png");
	}

	private List<Directory> list(int id) {
		return new ArrayList<>(browserService.getSubDirectories(id));
	}
	
	private Directory getFirstRootDirectory() {
		return browserService.getRootDirectories().iterator().next();
	}
	
}