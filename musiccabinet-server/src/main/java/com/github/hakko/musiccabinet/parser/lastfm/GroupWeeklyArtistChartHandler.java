package com.github.hakko.musiccabinet.parser.lastfm;

import static org.apache.commons.lang.math.NumberUtils.toInt;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.ArtistPlayCount;

public class GroupWeeklyArtistChartHandler extends DefaultHandler {
	
	private List<ArtistPlayCount> artistPlayCounts = new ArrayList<>();
	private ArtistPlayCount artistPlayCount;
	
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_NAME = "name";
	private static final String TAG_PLAY_COUNT = "playcount";
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		characterData = new StringBuilder();
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		String chars = characterData.toString();
		if (TAG_NAME.equals(qName)) {
			artistPlayCount = new ArtistPlayCount(chars);
		} else if (TAG_PLAY_COUNT.equals(qName)) {
			artistPlayCount.setPlayCount(toInt(chars));
			artistPlayCounts.add(artistPlayCount);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public List<ArtistPlayCount> getArtistPlayCount() {
		return artistPlayCounts;
	}

}