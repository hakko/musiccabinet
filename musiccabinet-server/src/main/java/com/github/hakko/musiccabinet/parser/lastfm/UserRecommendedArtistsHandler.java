package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists.RecommendedArtist;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class UserRecommendedArtistsHandler extends DefaultHandler {
	
	private List<RecommendedArtist> artists = new ArrayList<>();
	private RecommendedArtist currentRecommendedArtist = null;
	
	 // indicates if we're interested in found data, to skip similar artists/tags/bio
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_NAME = "name";
	private static final String TAG_CONTEXT = "context";

	private boolean insideContext = false;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) 
	throws SAXException {
		characterData = new StringBuilder();
		if (TAG_CONTEXT.equals(qName)) {
			insideContext = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		if (TAG_CONTEXT.equals(qName)) {
			insideContext = false;
		}
		
		if (TAG_NAME.equals(qName)) {
			String chars = characterData.toString();
			Artist artist = new Artist(chars);
			if (insideContext) {
				if (currentRecommendedArtist.getContextArtist1() == null) {
					currentRecommendedArtist.setContextArtist1(artist);
				} else {
					currentRecommendedArtist.setContextArtist2(artist);
				}
			} else {
				artists.add(currentRecommendedArtist = new RecommendedArtist(artist));
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public List<RecommendedArtist> getArtists() {
		return artists;
	}

}