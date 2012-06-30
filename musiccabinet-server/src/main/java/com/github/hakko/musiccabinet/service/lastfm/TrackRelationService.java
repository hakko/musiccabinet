package com.github.hakko.musiccabinet.service.lastfm;

import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.dao.TrackRelationDao;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityParser;
import com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.TrackSimilarityClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to relations between tracks.
 * 
 * (Currently not used - doing the Last.fm call on-the-fly is way too slow,
 * and the playlist becomes quite predictable)
 */
public class TrackRelationService {

	protected TrackSimilarityClient trackSimilarityClient;
	protected TrackRelationDao trackRelationDao;
	protected MusicFileDao musicFileDao;
	
	public void updateTrackRelation(String path) throws ApplicationException {
		Track track = musicFileDao.getTrack(path);
		WSResponse wsResponse = trackSimilarityClient.getTrackSimilarity(track);
		if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
			TrackSimilarityParser parser = new TrackSimilarityParserImpl(
					new StringUtil(wsResponse.getResponseBody()).getInputStream());
			trackRelationDao.createTrackRelations(
					parser.getTrack(), parser.getTrackRelations());
		}
	}

	// Spring setters

	public void setTrackSimilarityClient(TrackSimilarityClient trackSimilarityClient) {
		this.trackSimilarityClient = trackSimilarityClient;
	}

	public void setTrackRelationDao(TrackRelationDao trackRelationDao) {
		this.trackRelationDao = trackRelationDao;
	}

	public void setMusicFileDao(MusicFileDao musicFileDao) {
		this.musicFileDao = musicFileDao;
	}
	
}