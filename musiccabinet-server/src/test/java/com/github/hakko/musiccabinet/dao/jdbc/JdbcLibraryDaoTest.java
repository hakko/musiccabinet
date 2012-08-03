package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcLibraryDaoTest {

	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	@Autowired
	private JdbcLibraryDeletionDao deletionDao;
	
	@Autowired
	private JdbcLibraryPresenceDao presenceDao;
	
	@Before
	public void clearLibrary() throws ApplicationException {
		PostgreSQLUtil.loadFunction(additionDao, PostgreSQLFunction.ADD_TO_LIBRARY);
		PostgreSQLUtil.loadFunction(additionDao, PostgreSQLFunction.DELETE_FROM_LIBRARY);
		
		additionDao.getJdbcTemplate().execute("truncate library.directory cascade");
	}
	
	@Test
	public void storesSiblingSubDirectories() {
		String root = "/a";
		Set<String> subDirs = set("/a/b", "/a/c");
		
		additionDao.addSubdirectories(root, subDirs);
		additionDao.updateLibrary();
		
		Assert.assertEquals(subDirs, presenceDao.getSubdirectories(root));
	}

	@Test
	public void storesNestedSubDirectories() {
		String a = "/a", b = "/a/b", c = "/a/b/c";
		
		additionDao.addSubdirectories(a, set(b));
		additionDao.addSubdirectories(b, set(c));
		additionDao.updateLibrary();
		
		Assert.assertEquals(set(b), presenceDao.getSubdirectories(a));
		Assert.assertEquals(set(c), presenceDao.getSubdirectories(b));
	}
	
	@Test
	public void deletesSiblingSubDirectories() {
		String a = "/a", b = "/a/b", c = "/a/c",
				d = "/a/d", e = "/a/d/e", f = "/a/d/f";
		
		additionDao.addSubdirectories(a, set(b, c));
		additionDao.addSubdirectories(a, set(d));
		additionDao.addSubdirectories(d, set(e, f));
		additionDao.updateLibrary();

		deletionDao.deleteSubdirectories(a, set(c, d));
		deletionDao.updateLibrary();
		
		Assert.assertEquals(set(b), presenceDao.getSubdirectories(a));
		
	}

	@Test
	public void deletesNestedSubDirectories() {
		String a = "/a", b = "/a/b", c = "/a/c",
				d = "/a/d", e = "/a/d/e", f = "/a/d/f";
		
		additionDao.addSubdirectories(a, set(b, c));
		additionDao.addSubdirectories(a, set(d));
		additionDao.addSubdirectories(d, set(e, f));
		additionDao.updateLibrary();

		deletionDao.deleteSubdirectories(a, set(d));
		deletionDao.updateLibrary();
		
		Assert.assertEquals(set(b, c), presenceDao.getSubdirectories(a));
		
	}

	@Test
	public void storesFile() {
		String d = "/d";
		File f = getFile(d, "f1");

		additionDao.addSubdirectories(null, set(d));
		additionDao.addFiles(d, set(f));
		additionDao.updateLibrary();
		
		Assert.assertEquals(set(f), presenceDao.getFiles(d));
	}

	@Test
	public void storesFiles() {
		String d1 = "/d1", d2 = "/d1/d2";
		File f1 = getFile(d1, "f1");
		File f2a = getFile(d2, "f2a");
		File f2b = getFile(d2, "f2b");

		additionDao.addSubdirectories(null, set(d1));
		additionDao.addSubdirectories(d1, set(d2));
		additionDao.addFiles(d1, set(f1));
		additionDao.addFiles(d2, set(f2a, f2b));
		additionDao.updateLibrary();
		
		Assert.assertEquals(set(f1), presenceDao.getFiles(d1));
		Assert.assertEquals(set(f2a, f2b), presenceDao.getFiles(d2));
	}

	@Test
	public void deletesDirectoryWithFiles() {
		String d1 = "/d1", d2 = "/d1/d2", d3 = "/d1/d2/d3";
		File f1 = getFile(d1, "f1");
		File f2 = getFile(d2, "f2");
		File f3 = getFile(d3, "f3");

		additionDao.addSubdirectories(null, set(d1));
		additionDao.addSubdirectories(d1, set(d2));
		additionDao.addSubdirectories(d2, set(d3));
		additionDao.addFiles(d1, set(f1));
		additionDao.addFiles(d2, set(f2));
		additionDao.addFiles(d3, set(f3));
		additionDao.updateLibrary();
		
		deletionDao.deleteSubdirectories(d1, set(d2));
		deletionDao.updateLibrary();
		
		Assert.assertEquals(set(f1), presenceDao.getFiles(d1));
		Assert.assertTrue(presenceDao.exists(d1));
		Assert.assertFalse(presenceDao.exists(d2));
		Assert.assertFalse(presenceDao.exists(d3));
	}
	
}