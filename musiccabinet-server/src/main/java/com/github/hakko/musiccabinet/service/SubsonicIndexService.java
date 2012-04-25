package com.github.hakko.musiccabinet.service;

import java.io.InputStream;

import com.github.hakko.musiccabinet.dao.MusicDirectoryDao;
import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.dao.PlaylistGeneratorDao;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.subsonic.SubsonicIndexParser;
import com.github.hakko.musiccabinet.parser.subsonic.SubsonicIndexParserImpl;

/*
 * Provides services for updating database record of what music files
 * we have access to, by parsing a Subsonic index file.
 * 
 * Note: search index in library.artisttoptrackplaycount must be cleared before start.
 * (waiting for materialized view)
 */
public class SubsonicIndexService {
	
	protected MusicFileDao musicFileDao;
	protected MusicDirectoryDao musicDirectoryDao;
	protected PlaylistGeneratorDao playlistGeneratorDao;

	private int musicFilesCount;
	
	public void updateLibraryIndex(InputStream inputStream) throws ApplicationException {
		musicFilesCount = 0;
		
		SubsonicIndexParser parser = new SubsonicIndexParserImpl(inputStream);
		
		musicFileDao.clearImport();
		musicDirectoryDao.clearImport();
		boolean moreDataExists;
		do {
			moreDataExists = parser.readBatch();
			musicFileDao.addMusicFiles(parser.getMusicFiles());
			musicDirectoryDao.addMusicDirectories(parser.getMusicDirectories());
			musicFilesCount += parser.getMusicFiles().size();
		} while (moreDataExists);
		musicFileDao.createMusicFiles();
		musicDirectoryDao.createMusicDirectories();
	}
	
	/*
	 * Returns number of files read, so far.
	 */
	public int getMusicFilesCount() {
		return musicFilesCount;
	}
	
	// Spring setters
	
	public void setMusicFileDao(MusicFileDao musicFileDao) {
		this.musicFileDao = musicFileDao;
	}
	
	public void setMusicDirectoryDao(MusicDirectoryDao musicDirectoryDao) {
		this.musicDirectoryDao = musicDirectoryDao;
	}
	
	public void setPlaylistGeneratorDao(PlaylistGeneratorDao playlistGeneratorDao) {
		this.playlistGeneratorDao = playlistGeneratorDao;
	}
	
}