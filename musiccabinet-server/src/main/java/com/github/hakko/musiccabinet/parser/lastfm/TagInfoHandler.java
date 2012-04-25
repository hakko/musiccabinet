package com.github.hakko.musiccabinet.parser.lastfm;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.hakko.musiccabinet.domain.model.music.TagInfo;

public class TagInfoHandler extends DefaultHandler {
	
	private TagInfo tagInfo;
	
	private StringBuilder characterData; // used to assemble xml text passed by parser

	private static final String TAG_NAME = "name";
	private static final String TAG_SUMMARY = "summary";
	private static final String TAG_CONTENT = "content";
	
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
			tagInfo = new TagInfo();
			tagInfo.setTagName(chars);
		} else if (TAG_SUMMARY.equals(qName)) {
			tagInfo.setSummary(chars);
		} else if (TAG_CONTENT.equals(qName)) {
			tagInfo.setContent(chars);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterData.append(ch, start, length);
	}

	/* Variables to be exposured after parsing */

	public TagInfo getTagInfo() {
		return tagInfo;
	}

}