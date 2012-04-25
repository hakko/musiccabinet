package com.github.hakko.musiccabinet.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.itunes.ItunesMusicLibraryParserCallback;
import com.github.hakko.musiccabinet.parser.itunes.ItunesMusicLibraryParserImpl;

/*
 * Provides services related to the application iTunes.
 * 
 * What it currently does: just one thing, updateLibraryIndex.
 * 
 * Input:
 * a path to an xml file, that defines the media files stored in an iTunes library.
 * 
 * Result:
 * The xml file is parsed, and the media stored in iTunes is matched with the media stored
 * in MusicCabinet. When a match is found, MusicCabinet then adds a note of the iTunes id.
 * 
 * Using this, we can have MusicCabinet generate different clever combinations of songs,
 * and only output the iTunes id of those songs. We can then let iTunes read the ids and
 * quickly create it's own playlists out of it.
 * (This means iTunes isn't forced to read all of the media files, as it recognizes their
 * ids and knows they're already imported to the library. Just a way to make the integration
 * of iTunes+MusicCabinet easier, really.)
 * 
 */
public class ItunesMusicLibraryService {
	
	protected MusicFileDao musicFileDao;

	private static final int BATCH_SIZE = 1000;

	public void updateLibraryIndex(InputStream source) throws ApplicationException {
		musicFileDao.clearImport();
		new ItunesMusicLibraryParserImpl(source, new ItunesMusicLibraryParserCallback() {
			
			private List<MusicFile> musicFiles = new ArrayList<MusicFile>();

			@Override
			public void endOfMusicFiles() {
				batchInsert();
			}
			
			@Override
			public void addMusicFile(MusicFile musicFile) {
				musicFiles.add(musicFile);
				if (musicFiles.size() == BATCH_SIZE) {
					batchInsert();
				}
			}
			
			private void batchInsert() {
				musicFileDao.addMusicFiles(musicFiles);
				musicFiles.clear();
			}
		});
		musicFileDao.createMusicFileInternalIds();
	}
	
	// Spring setters

	public void setMusicFileDao(MusicFileDao musicFileDao) {
		this.musicFileDao = musicFileDao;
	}

}