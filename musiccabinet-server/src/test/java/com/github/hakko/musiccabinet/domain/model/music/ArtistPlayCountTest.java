package com.github.hakko.musiccabinet.domain.model.music;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ArtistPlayCountTest {

	@Test
	public void sortsArtistPlayCountByPlayCountAndName() {
	
		ArtistPlayCount a99 = new ArtistPlayCount("A", 99);
		ArtistPlayCount a100 = new ArtistPlayCount("A", 100);
		
		ArtistPlayCount b99 = new ArtistPlayCount("B", 99);
		ArtistPlayCount b100 = new ArtistPlayCount("B", 100);

		List<ArtistPlayCount> apcs = new ArrayList<>();
		apcs.addAll(asList(b99, a99, a100, b100));
		Collections.sort(apcs);
		
		Assert.assertEquals(Arrays.asList(a100, b100, a99, b99), apcs);
		
	}
	
}