package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class TrackSimilarityParserImpl extends AbstractSAXParserImpl implements TrackSimilarityParser {
	
	private Track track;
	private List<TrackRelation> trackRelations = new ArrayList<TrackRelation>();
	
	public TrackSimilarityParserImpl(InputStream source) throws ApplicationException {
		loadTrackSimilarity(source);
	}

	private void loadTrackSimilarity(InputStream source) 
	throws ApplicationException {
		TrackSimilarityHandler handler = new TrackSimilarityHandler();
		parseFromStream(source, handler);
		track = handler.getSourceTrack();
		trackRelations = handler.getTrackRelations();
	}

	@Override
	public Track getTrack() {
		return track;
	}

	@Override
	public List<TrackRelation> getTrackRelations() {
		return trackRelations;
	}
	
}