package com.github.hakko.musiccabinet.parser.musicbrainz;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.MBRelease;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ReleaseParserTest {

	private static final String RELEASE_FILE = "musicbrainz/xml/release.xml";

	private static final String RELEASE_EMPTY_FILE = "musicbrainz/xml/release-empty.xml";

	// constant values below are copied from file above
	private static final MBRelease RELEASE_0 = new MBRelease(
			"d7ce4f9f-8916-369c-a5bd-782a9c6d7568", "be0fec81-5c18-4494-8bbf-0d81dec006bf",
			"Sire Records", "I'll Take the Blame EP", "EP", 2007, "Digital Media");
	private static final MBRelease RELEASE_1 = new MBRelease(
			"c3ed0e1b-6a43-4232-b3fe-da90dca49f7c", null,
			null, "In Your Head: An Introduction to Tegan and Sara", "EP", 2012, "Digital Media");
	private static final MBRelease RELEASE_2 = new MBRelease(
			"d7ce4f9f-8916-369c-a5bd-782a9c6d7568", "be0fec81-5c18-4494-8bbf-0d81dec006bf",
			"Sire Records", "I'll Take the Blame EP", "EP", 2007, "CD");
	private static final MBRelease RELEASE_32 = new MBRelease(
			"50998855-0dd0-34ab-b1c7-32f2468bd5df", "6cee07d5-4cc3-4555-a629-480590e0bebd",
			"Universal Music Canada", "So Jealous", "Album", 2004, "CD");
	private static final MBRelease RELEASE_33 = new MBRelease(
			"e8bfa13e-a5ab-3c50-8eb6-4b4b1f838679", "45d3cbc9-e0e3-40ab-b97e-e0d4e3ecd93d",
			"Plunk Records", "Under Feet Like Ours", "Album", 1999, "CD");

	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		ReleaseParser parser = new ReleaseParserImpl(new ResourceUtil(
				RELEASE_FILE).getInputStream());
		List<MBRelease> releases = parser.getReleases();

		assertEquals(RELEASE_0, releases.get(0));
		assertEquals(RELEASE_1, releases.get(1));
		assertEquals(RELEASE_2, releases.get(2));
		assertEquals(RELEASE_32, releases.get(32));
		assertEquals(RELEASE_33, releases.get(33));
		
		assertEquals(34, parser.getTotalReleases());
	}

	@Test
	public void emptyResponseIsReturnedAsNoReleases() throws ApplicationException {
		ReleaseParser parser = new ReleaseParserImpl(new ResourceUtil(
				RELEASE_EMPTY_FILE).getInputStream());
		List<MBRelease> releases = parser.getReleases();

		assertEquals(0, releases.size());
	}

}