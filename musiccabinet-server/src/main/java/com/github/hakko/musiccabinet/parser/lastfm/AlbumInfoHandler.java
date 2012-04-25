package com.github.hakko.musiccabinet.parser.lastfm;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class AlbumInfoHandler extends DefaultHandler {
	
	private AlbumInfo albumInfo;
	
	 // indicates if we're interested in found data, to skip similar artists/tags/bio
	private boolean parsing = true; 
	private String imageSize;
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_NAME = "name";
	private static final String TAG_ARTIST = "artist";
	private static final String TAG_IMAGE = "image";
	private static final String TAG_LISTENERS = "listeners";
	private static final String TAG_PLAY_COUNT = "playcount";
	
	private static final String ATTR_SIZE = "size";
	private static final String ATTR_SMALL = "small";
	private static final String ATTR_MEDIUM = "medium";
	private static final String ATTR_LARGE = "large";
	private static final String ATTR_EXTRA_LARGE = "extralarge";

	private static final String IMAGE_HOST = "last.fm";
	
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
				albumInfo = new AlbumInfo();
				albumInfo.setAlbum(new Album(chars));
			} else if (TAG_ARTIST.equals(qName)) {
				albumInfo.getAlbum().setArtist(new Artist(chars));
			} else if (TAG_IMAGE.equals(qName)) {
				if (ATTR_SMALL.equals(imageSize)) {
					albumInfo.setSmallImageUrl(validateUrl(chars));
				} else if (ATTR_MEDIUM.equals(imageSize)) {
					albumInfo.setMediumImageUrl(validateUrl(chars));
				} else if (ATTR_LARGE.equals(imageSize)) {
					albumInfo.setLargeImageUrl(validateUrl(chars));
				} else if (ATTR_EXTRA_LARGE.equals(imageSize)) {
					albumInfo.setExtraLargeImageUrl(validateUrl(chars));
				}
			} else if (TAG_LISTENERS.equals(qName)) {
				albumInfo.setListeners(Integer.parseInt(chars));
			} else if (TAG_PLAY_COUNT.equals(qName)) {
				albumInfo.setPlayCount(Integer.parseInt(chars));
				parsing = false;
			}
		}
	}
	
	// sometimes album info contains weird image urls
	// (low resolution images from amazon, cdbaby etc),
	// which we won't store.
	protected String validateUrl(String url) {
		String result = null;
		
		if (url != null && url.indexOf(IMAGE_HOST) > 0) {
			result = url;
		}
		
		return result;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public AlbumInfo getAlbumInfo() {
		return albumInfo;
	}

}