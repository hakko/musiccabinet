package com.github.hakko.musiccabinet.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.library.MetaData.Mediatype;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class UnittestLibraryUtil {

	private static int counter = 0;
	
	public static File getFile() {
		File file = new File("/unittest", "/unittest/file" + counter++ + ".ogg", new DateTime(), 0);
		MetaData md = new MetaData();
		md.setArtist("Unittest Artist");
		md.setAlbum("Unittest Album " + counter++);
		md.setTitle("Unittest Title " + counter++);
		md.setBitrate((short) 144);
		md.setVbr(false);
		md.setDuration((short) 90);
		md.setYear((short) 1900);
		md.setMediaType(Mediatype.OGG);
		file.setMetaData(md);
		return file;
	}

	public static File getFile(String directory, String filename) {
		File file = getFile();
		file.setDirectory(directory);
		file.setFilename(filename);
		return file;
	}
	
	public static File getFile(String artist, String album, String title) {
		File file = getFile();
		MetaData md = file.getMetadata();
		if (artist != null) md.setArtist(artist);
		if (album != null) md.setAlbum(album);
		if (title != null) md.setTitle(title);
		return file;
	}
	
	public static File getFile(Track track) {
		return getFile(track.getArtist().getName(), null, track.getName());
	}
	
	public static void submitFile(LibraryAdditionDao dao, File file) {
		submitFile(dao, Arrays.asList(file));
	}

	public static void submitFile(LibraryAdditionDao dao, List<File> files) {
		Set<String> directories = new HashSet<>();
		for (File file : files) {
			directories.add(file.getDirectory());
		}
		
		dao.clearImport();
		dao.addFiles("/unittest", new HashSet<>(files));
		dao.addSubdirectories("/unittest", directories);
		dao.updateLibrary();
	}

}
