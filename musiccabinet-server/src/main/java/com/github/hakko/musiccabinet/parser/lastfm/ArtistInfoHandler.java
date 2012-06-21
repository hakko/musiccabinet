package com.github.hakko.musiccabinet.parser.lastfm;

import static org.apache.commons.lang.math.NumberUtils.toInt;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;

public class ArtistInfoHandler extends DefaultHandler {
	
	private ArtistInfo artistInfo;
	
	 // indicates if we're interested in found data, to skip similar artists/tags/bio
	private boolean parsing = true; 
	private String imageSize;
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_NAME = "name";
	private static final String TAG_IMAGE = "image";
	private static final String TAG_LISTENERS = "listeners";
	private static final String TAG_PLAY_COUNT = "playcount";
	private static final String TAG_BIO_SUMMARY = "summary";
	private static final String TAG_BIO_CONTENT = "content";
	
	private static final String ATTR_SIZE = "size";
	private static final String ATTR_SMALL = "small";
	private static final String ATTR_MEDIUM = "medium";
	private static final String ATTR_LARGE = "large";
	private static final String ATTR_EXTRA_LARGE = "extralarge";
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_IMAGE.equals(qName)) {
			imageSize = attributes.getValue(ATTR_SIZE);
		}
		characterData = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		if (parsing) {
			String chars = characterData.toString();
			if (TAG_NAME.equals(qName)) {
				artistInfo = new ArtistInfo();
				artistInfo.setArtist(new Artist(chars));
			} else if (TAG_IMAGE.equals(qName)) {
				if (ATTR_SMALL.equals(imageSize)) {
					artistInfo.setSmallImageUrl(chars);
				} else if (ATTR_MEDIUM.equals(imageSize)) {
					artistInfo.setMediumImageUrl(chars);
				} else if (ATTR_LARGE.equals(imageSize)) {
					artistInfo.setLargeImageUrl(chars);
				} else if (ATTR_EXTRA_LARGE.equals(imageSize)) {
					artistInfo.setExtraLargeImageUrl(chars);
				}
			} else if (TAG_LISTENERS.equals(qName)) {
				artistInfo.setListeners(toInt(chars));
			} else if (TAG_PLAY_COUNT.equals(qName)) {
				artistInfo.setPlayCount(toInt(chars));
				parsing = false;
			}
		}
		if (TAG_BIO_SUMMARY.equals(qName)) {
			artistInfo.setBioSummary(characterData.toString());
		} else if (TAG_BIO_CONTENT.equals(qName)) {
			artistInfo.setBioContent(characterData.toString());
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public ArtistInfo getArtistInfo() {
		return artistInfo;
	}

}