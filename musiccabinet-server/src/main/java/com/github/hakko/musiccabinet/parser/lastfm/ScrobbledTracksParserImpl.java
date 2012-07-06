package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.TrackPlayCount;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ScrobbledTracksParserImpl extends AbstractSAXParserImpl implements ScrobbledTracksParser {
	
	private List<TrackPlayCount> trackPlayCounts = new ArrayList<>();
	private short page;
	private short totalPages;
	
	public ScrobbledTracksParserImpl(InputStream source) throws ApplicationException {
		loadScrobbledTracks(source);
	}

	private void loadScrobbledTracks(InputStream source) throws ApplicationException {
		ScrobbledTracksHandler handler = new ScrobbledTracksHandler();
		parseFromStream(source, handler);
		trackPlayCounts = handler.getTrackPlayCounts();
		page = handler.getPage();
		totalPages = handler.getTotalPages();
	}

	@Override
	public List<TrackPlayCount> getTrackPlayCounts() {
		return trackPlayCounts;
	}

	@Override
	public short getPage() {
		return page;
	}
	
	@Override
	public short getTotalPages() {
		return totalPages;
	}
	
}