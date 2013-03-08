package com.github.hakko.musiccabinet.parser.lastfm;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;
import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.math.NumberUtils.toInt;

import java.io.UnsupportedEncodingException;

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
	
	private static final String LICENSE = "User-contributed text is available under the Creative Commons By-SA License and may also be available under the GNU FDL.";
	private static final String READ_MORE_LINK = "<a href=\"http://www.last.fm/music/%s\">Read more about %s on Last.fm</a>.";

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
			if (TAG_NAME.equals(qName) && artistInfo == null) {
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
			artistInfo.setBioSummary(stripLicenseAndReadMoreLink(
					characterData.toString(), artistInfo.getArtist().getName()));
		} else if (TAG_BIO_CONTENT.equals(qName)) {
			artistInfo.setBioContent(stripLicenseAndReadMoreLink(
					characterData.toString(), artistInfo.getArtist().getName()));
		}
	}

	protected String stripLicenseAndReadMoreLink(String biography, String artistName) {
		biography = remove(biography, LICENSE);
		try {
			String LINK = format(READ_MORE_LINK, artistName, encode(artistName, UTF8));
			biography = remove(biography, LINK);
		} catch (UnsupportedEncodingException e) {
			// unlikely, and only means we can't remove the "read more about ..." link.
		}
		return biography.trim();
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