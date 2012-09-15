package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.ArtistPlayCount;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class GroupWeeklyArtistChartParserImpl extends AbstractSAXParserImpl 
implements GroupWeeklyArtistChartParser {
	
	private GroupWeeklyArtistChartHandler handler = new GroupWeeklyArtistChartHandler();

	public GroupWeeklyArtistChartParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public List<ArtistPlayCount> getArtistPlayCount() {
		return handler.getArtistPlayCount();
	}
	
}