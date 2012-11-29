package com.github.hakko.musiccabinet.parser.musicbrainz;

import static org.apache.commons.lang.StringUtils.substring;
import static org.apache.commons.lang.math.NumberUtils.toInt;
import static org.apache.commons.lang.math.NumberUtils.toShort;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;

public class ReleaseGroupHandler extends DefaultHandler {
	
	private int totalAlbums;
	private List<MBAlbum> albums = new ArrayList<>();
	private MBAlbum currentAlbum;
	
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_RELEASE_GROUP_LIST = "release-group-list";
	private static final String TAG_RELEASE_GROUP = "release-group";
	private static final String TAG_TITLE = "title";
	private static final String TAG_FIRST_RELEASE_DATE = "first-release-date";
	private static final String TAG_PRIMARY_TYPE = "primary-type";
	
	private static final String ATTR_ID = "id";
	private static final String ATTR_COUNT = "count";
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_RELEASE_GROUP_LIST.equals(qName)) {
			totalAlbums = toInt(attributes.getValue(ATTR_COUNT));
		} else if (TAG_RELEASE_GROUP.equals(qName)) {
			albums.add(currentAlbum = new MBAlbum());
			currentAlbum.setMbid(attributes.getValue(ATTR_ID));
		}
		characterData = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		String chars = characterData.toString();
		if (TAG_TITLE.equals(qName)) {
			currentAlbum.setTitle(chars);
		} else if (TAG_FIRST_RELEASE_DATE.equals(qName)) {
			currentAlbum.setFirstReleaseYear(toShort(substring(chars, 0, 4)));
		} else if (TAG_PRIMARY_TYPE.equals(qName)) {
			currentAlbum.setAlbumType(chars);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public List<MBAlbum> getAlbums() {
		return albums;
	}

	public int getTotalAlbums() {
		return totalAlbums;
	}
	
}