package com.github.hakko.musiccabinet.domain.model.music;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;

public class PlaylistItemTest {

	@Test
	public void validateConstructor() {
		String artistName = "madonna";
		String path = "/path/";
		PlaylistItem ts = new PlaylistItem(artistName, path);
		
		Assert.assertEquals(ts.getArtist(), new Artist(artistName));
		Assert.assertEquals(ts.getPath(), path);
	}
	
}