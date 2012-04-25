package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ArtistSimilarityParserImpl extends AbstractSAXParserImpl implements ArtistSimilarityParser {
	
	private Artist artist;
	private List<ArtistRelation> artistRelations = new ArrayList<ArtistRelation>();
	
	public ArtistSimilarityParserImpl(InputStream source) throws ApplicationException {
		loadArtistSimilarity(source);
	}

	private void loadArtistSimilarity(InputStream source) 
	throws ApplicationException {
		ArtistSimilarityHandler handler = new ArtistSimilarityHandler();
		parseFromStream(source, handler);
		artist = new Artist(handler.getArtistName());
		artistRelations = handler.getArtistRelations();
	}
	
	@Override
	public Artist getArtist() {
		return artist;
	}

	@Override
	public List<ArtistRelation> getArtistRelations() {
		return artistRelations;
	}
	
}