package com.github.hakko.musiccabinet.parser.itunes;

import java.io.InputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractStAXParserImpl;

/*
 * The iTunes parser differs from last.fm parsers, in having a DTD from apple.com
 * and being larger than expected heap size.
 * 
 * We don't want to resolve the DTD each time we run a unit test (that would require
 * network access for unit tests to pass). Also, we want to parse parts of it, update
 * database, and repeat that until the whole file has been read. That's why a stax
 * parser seems more feasible for the job than the sax parsers used for last.fm data.
 */
public class ItunesMusicLibraryParserImpl extends AbstractStAXParserImpl implements ItunesMusicLibraryParser {
	
	private XMLEventReader xmlEventReader;

	private MusicFile currentMusicFile; // used internally while parsing

	private StringBuilder characterData = new StringBuilder(); // used to assemble xml text passed by parser

	private int dictDepth; // keep track of level, since xml is self-recursive
	private String dictDepthOneKey; // current outermost key. only "Tracks" is interesting
	private String currentKey;
	
	private ItunesMusicLibraryParserCallback callback;
	
	private static final String TAG_DICT = "dict";
	private static final String TAG_KEY = "key";
	
	private static final String KEY_TRACKS = "Tracks";
	private static final String KEY_TRACK_ID = "Track ID";
	private static final String KEY_NAME = "Name";
	private static final String KEY_ARTIST = "Artist";
	
	public ItunesMusicLibraryParserImpl(InputStream source,
			ItunesMusicLibraryParserCallback callback) throws ApplicationException {
		xmlEventReader = getXMLEventReader(source);
		this.callback = callback;
		try {
			loadItunesLibraryIndex();
		} catch (XMLStreamException e) {
			throw new ApplicationException("Could not parse input stream!", e);
		}
	}

	private void loadItunesLibraryIndex() throws XMLStreamException {
		while (xmlEventReader.hasNext()) {
			XMLEvent event = xmlEventReader.nextEvent();
			if (event.isStartElement()) {
				startElement(event.asStartElement().getName().getLocalPart());
			} else if (event.isEndElement()) {
				endElement(event.asEndElement().getName().getLocalPart());
			} else if (event.isCharacters()) {
				characters(event.asCharacters().getData());
			}
		}
	}

	private void startElement(String qName) {
		if (TAG_DICT.equals(qName)) {
			++dictDepth;
		}
		characterData = new StringBuilder();
	}

	private void endElement(String qName) {
		String chars = characterData.toString();
		if (TAG_DICT.equals(qName)) {
			--dictDepth;
			if (dictDepth == 0) {
				callback.endOfMusicFiles();
			}
		} else if (TAG_KEY.equals(qName)) {
			currentKey = chars;
			if (dictDepth == 1) {
				dictDepthOneKey = chars;
			}
		} else {
			if (KEY_TRACKS.equals(dictDepthOneKey)) {
				if (KEY_TRACK_ID.equals(currentKey)) {
					currentMusicFile = new MusicFile(chars);
				} else if (KEY_NAME.equals(currentKey)) {
					currentMusicFile.getTrack().setName(chars.trim());
				} else if (KEY_ARTIST.equals(currentKey)) {
					currentMusicFile.getTrack().setArtist(new Artist(chars.trim()));
					callback.addMusicFile(currentMusicFile);
				}
			}
		}
	}

	private void characters(String data) {
		characterData.append(data);
	}

}