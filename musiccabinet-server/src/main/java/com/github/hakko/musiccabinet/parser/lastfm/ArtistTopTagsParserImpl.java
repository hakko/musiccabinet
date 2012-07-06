package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ArtistTopTagsParserImpl extends AbstractSAXParserImpl implements ArtistTopTagsParser {
	
	private Artist artist;
	private List<Tag> topTags = new ArrayList<>();
	
	public ArtistTopTagsParserImpl(InputStream source) throws ApplicationException {
		loadTopTags(source);
	}

	private void loadTopTags(InputStream source) throws ApplicationException {
		ArtistTopTagsHandler handler = new ArtistTopTagsHandler();
		parseFromStream(source, handler);
		artist = handler.getArtist();
		topTags = handler.getTopTags();
	}
	
	@Override
	public Artist getArtist() {
		return artist;
	}

	@Override
	public List<Tag> getTopTags() {
		return topTags;
	}
	
}