package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;

import com.github.hakko.musiccabinet.domain.model.music.TagInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class TagInfoParserImpl extends AbstractSAXParserImpl implements TagInfoParser {
	
	private TagInfoHandler handler = new TagInfoHandler();

	public TagInfoParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public TagInfo getTagInfo() {
		return handler.getTagInfo();
	}
	
}