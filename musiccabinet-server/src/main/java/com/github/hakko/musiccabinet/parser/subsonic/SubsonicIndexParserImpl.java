package com.github.hakko.musiccabinet.parser.subsonic;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.text.StrTokenizer;

import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;

public class SubsonicIndexParserImpl implements SubsonicIndexParser {

	private BufferedReader fileReader;
	private List<MusicFile> musicFiles;
	private List<MusicDirectory> musicDirectories;
	
	/* Field delimiter used in Subsonic index file, version 14. */
	private static final String SEPARATOR = " ixYxi ";
	/* Constant indicating a file. */
	private static final String IS_FILE = "F";
	/* Constant indicating an artist. */
	private static final String IS_ARTIST = "R";
	/* Constant indicating an album. */
	private static final String IS_ALBUM = "A";

	/* This is the maximum number of lines read at a time and returned.
	 * The subsonic index file might be too big to fit in heap.
	 */
	public static final int BATCH_SIZE = 1000;
	
	private static final Logger LOG = Logger.getLogger(SubsonicIndexParserImpl.class);
	
	public SubsonicIndexParserImpl(InputStream inputStream) throws ApplicationException {
		try {
			fileReader = new BufferedReader(new InputStreamReader(inputStream, UTF8));
		} catch (UnsupportedEncodingException e) {
			throw new ApplicationException("Could not read index file using UTF-8!", e);
		}
	}

	/*
	 * Reads a batch of file descriptions from a Subsonic index file, adds them to
	 * local list of MusicFiles or MusicDirectories, and returns a flag indicating
	 * if there are more rows to read.
	 * 
	 * The reason for reading in batches is that the index file is pretty big and
	 * might cause a heap space error if we try to allocate it all at once.
	 * 
	 * @return true if there are more lines to read, false if finished.
	 */
	@Override
	public boolean readBatch() throws ApplicationException {
		String line;
		int lines = 0;
		musicFiles = new ArrayList<MusicFile>();
		musicDirectories = new ArrayList<MusicDirectory>();
		
		try {
			while ((line = fileReader.readLine()) != null) {
				if (line.startsWith(IS_FILE)) {
					addMusicFile(line);
				} else if (line.startsWith(IS_ARTIST) || line.startsWith(IS_ALBUM)) {
					addMusicDirectory(line);
				}
				if (++lines == BATCH_SIZE) {
					break;
				}
			}
			if (line == null) {
				fileReader.close();
			}
		} catch (IOException e) {
			throw new ApplicationException("Could not parse subsonic index file!", e);
		}
		
		return lines == BATCH_SIZE;
	}

	/**
	 * Parses a single line representing a file from a Subsonic index file, version 14.
	 * 
	 * @see net.sourceforge.subsonic.service.SearchService
	 * 
	 * @param line
	 * @return <code>null</code> if it could not be parsed as a track
	 */
    protected void addMusicFile(String line) {
    	StrTokenizer tokenizer = new StrTokenizer(line, SEPARATOR);
        String[] tokens = tokenizer.getTokenArray();
    	if (!IS_FILE.equals(tokens[0])) {
        	return;
        }
    	
    	/* One user reported an ArrayIndexOutOfBoundsException from this class.
    	 * Still haven't figured out the exact cause for this, so this line is
    	 * to be seen as debugging of when Subsonic creates such a line.
    	 */
    	if (tokens.length < 8) {
    		LOG.warn("Can't add track from line " + line + "!");
    		return;
    	}
        
        long created = Long.parseLong(tokens[1]);
        long lastModified = Long.parseLong(tokens[2]);
        String path = tokens[3];
        String artistName = tokens[5];
        String trackName = tokens[7];

        musicFiles.add(new MusicFile(artistName, trackName, path, created, lastModified));
    }
	
    /**
	 * Parses a single line representing a directory from a Subsonic index file, version 14.
	 * 
	 * @see net.sourceforge.subsonic.service.SearchService
	 * 
	 * @param line
	 * @return <code>null</code> if it could not be parsed as a track
	 */
    protected void addMusicDirectory(String line) {
    	StrTokenizer tokenizer = new StrTokenizer(line, SEPARATOR);
        String[] tokens = tokenizer.getTokenArray();
    	if (!IS_ARTIST.equals(tokens[0]) && !IS_ALBUM.equals(tokens[0])) {
        	return;
        }
    	
    	String path = tokens[3];
    	String artistName = tokens[5];
    	String albumName = IS_ARTIST.equals(tokens[0]) ? null : tokens[6];
    	
    	musicDirectories.add(new MusicDirectory(artistName, albumName, path));
    }

	public List<MusicFile> getMusicFiles() {
		return musicFiles;
	}

	public List<MusicDirectory> getMusicDirectories() {
		return musicDirectories;
	}

}