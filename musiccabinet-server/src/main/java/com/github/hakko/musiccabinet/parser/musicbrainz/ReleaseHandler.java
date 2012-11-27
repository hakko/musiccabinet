package com.github.hakko.musiccabinet.parser.musicbrainz;

import static org.apache.commons.lang.StringUtils.substring;
import static org.apache.commons.lang.math.NumberUtils.toInt;
import static org.apache.commons.lang.math.NumberUtils.toShort;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.MBRelease;

public class ReleaseHandler extends DefaultHandler {
	
	private int totalAlbums;
	private List<MBRelease> releases = new ArrayList<>();
	private MBRelease currentRelease;
	
	private StringBuilder characterData; // used to assemble xml text passed by parser
	private boolean label = false; // whether we're parsing a label node
	
	private static final String TAG_RELEASE_LIST = "release-list";
	private static final String TAG_RELEASE = "release";
	private static final String TAG_RELEASE_GROUP = "release-group";
	private static final String TAG_TITLE = "title";
	private static final String TAG_DATE = "date";
	private static final String TAG_LABEL = "label";
	private static final String TAG_NAME = "name";
	private static final String TAG_FORMAT = "format";

	private static final String ATTR_COUNT = "count";
	private static final String ATTR_ID = "id";
	private static final String ATTR_TYPE = "type";
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		if (TAG_RELEASE_LIST.equals(qName)) {
			totalAlbums = toInt(attributes.getValue(ATTR_COUNT));
		} else if (TAG_RELEASE.equals(qName)) {
			releases.add(currentRelease = new MBRelease());
		} else if (TAG_RELEASE_GROUP.equals(qName)) {
			currentRelease.setReleaseGroupMbid(attributes.getValue(ATTR_ID));
			currentRelease.setAlbumType(attributes.getValue(ATTR_TYPE));
		} else if (TAG_LABEL.equals(qName)) {
			currentRelease.setLabelMbid(attributes.getValue(ATTR_ID));
			label = true;
		}
		characterData = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		String chars = characterData.toString();
		if (TAG_TITLE.equals(qName)) {
			currentRelease.setTitle(chars);
		} else if (TAG_DATE.equals(qName)) {
			currentRelease.setReleaseYear(toShort(substring(chars, 0, 4)));
		} else if (TAG_NAME.equals(qName) && label) {
			currentRelease.setLabelName(chars);
			label = false;
		} else if (TAG_FORMAT.equals(qName)) {
			currentRelease.setFormat(chars);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public List<MBRelease> getReleases() {
		return releases;
	}

	public int getTotalReleases() {
		return totalAlbums;
	}
	
}