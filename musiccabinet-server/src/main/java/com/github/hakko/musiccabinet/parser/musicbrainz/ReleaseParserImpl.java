package com.github.hakko.musiccabinet.parser.musicbrainz;

import java.io.InputStream;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.MBRelease;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ReleaseParserImpl extends AbstractSAXParserImpl implements
		ReleaseParser {

	private ReleaseHandler handler = new ReleaseHandler();

	public ReleaseParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public List<MBRelease> getReleases() {
		return handler.getReleases();
	}

	@Override
	public int getTotalReleases() {
		return handler.getTotalReleases();
	}

}