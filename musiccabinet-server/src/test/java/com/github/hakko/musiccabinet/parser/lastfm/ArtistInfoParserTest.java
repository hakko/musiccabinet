package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ArtistInfoParserTest {
	
	private static final String ARTIST_INFO_FILE = 
		"last.fm/xml/artistinfo.abba.xml";

	// constant values below are copied from file above
	
	private static final String SMALL_IMAGE_URL = 
			"http://userserve-ak.last.fm/serve/34/44442235.png";
	private static final String MEDIUM_IMAGE_URL = 
			"http://userserve-ak.last.fm/serve/64/44442235.png";
	private static final String LARGE_IMAGE_URL = 
			"http://userserve-ak.last.fm/serve/126/44442235.png";
	private static final String EXTRA_LARGE_IMAGE_URL =
			"http://userserve-ak.last.fm/serve/252/44442235.png";
	
	private static final int LISTENERS = 1124680;
	private static final int PLAY_COUNT = 24345249;
	
	private static final String BIO_SUMMARY = "ABBA was a <a href=\"http://www.last.fm/tag/pop\" class=\"bbcode_tag\" rel=\"tag\">pop</a> music group formed in Stockholm, Sweden in November 1970. The band consisted of <a href=\"http://www.last.fm/music/Anni-Frid+Lyngstad\" class=\"bbcode_artist\">Anni-Frid Lyngstad</a> (<a href=\"http://www.last.fm/music/Frida\" class=\"bbcode_artist\">Frida</a>), <a href=\"http://www.last.fm/music/Bj%C3%B6rn+Ulvaeus\" class=\"bbcode_artist\">Bj&ouml;rn Ulvaeus</a>, <a href=\"http://www.last.fm/music/Benny+Andersson\" class=\"bbcode_artist\">Benny Andersson</a>, and <a href=\"http://www.last.fm/music/Agnetha+F%C3%A4ltskog\" class=\"bbcode_artist\">Agnetha F&auml;ltskog</a>. (See also <a href=\"http://www.last.fm/music/Bj%25C3%25B6rn%2BUlvaeus%2B%2526%2BBenny%2BAndersson\" class=\"bbcode_artist\">Bj&ouml;rn Ulvaeus &amp; Benny Andersson</a>, as well as Benny's previous band <a href=\"http://www.last.fm/music/Hep+Stars\" class=\"bbcode_artist\">Hep Stars</a>.)  They topped the charts worldwide from 1972 to 1982 with eight studio albums, achieving twenty-six #1 singles and numerous awards. They also won the 1974 Eurovision Song Contest with <a title=\"ABBA &ndash; Waterloo\" href=\"http://www.last.fm/music/ABBA/_/Waterloo\" class=\"bbcode_track\">Waterloo</a>.  ";
	private static final String BIO_CONTENT = "<strong>ABBA</strong> was a <a href=\"http://www.last.fm/tag/pop\" class=\"bbcode_tag\" rel=\"tag\">pop</a> music group formed in Stockholm, Sweden in November 1970. The band consisted of <a href=\"http://www.last.fm/music/Anni-Frid+Lyngstad\" class=\"bbcode_artist\">Anni-Frid Lyngstad</a> (<a href=\"http://www.last.fm/music/Frida\" class=\"bbcode_artist\">Frida</a>), <a href=\"http://www.last.fm/music/Bj%C3%B6rn+Ulvaeus\" class=\"bbcode_artist\">Bj&ouml;rn Ulvaeus</a>, <a href=\"http://www.last.fm/music/Benny+Andersson\" class=\"bbcode_artist\">Benny Andersson</a>, and <a href=\"http://www.last.fm/music/Agnetha+F%C3%A4ltskog\" class=\"bbcode_artist\">Agnetha F&auml;ltskog</a>. (See also <a href=\"http://www.last.fm/music/Bj%25C3%25B6rn%2BUlvaeus%2B%2526%2BBenny%2BAndersson\" class=\"bbcode_artist\">Bj&ouml;rn Ulvaeus &amp; Benny Andersson</a>, as well as Benny's previous band <a href=\"http://www.last.fm/music/Hep+Stars\" class=\"bbcode_artist\">Hep Stars</a>.)\n \n They topped the charts worldwide from 1972 to 1982 with eight studio albums, achieving twenty-six #1 singles and numerous awards. They also won the 1974 Eurovision Song Contest with <a title=\"ABBA &ndash; Waterloo\" href=\"http://www.last.fm/music/ABBA/_/Waterloo\" class=\"bbcode_track\">Waterloo</a>.\n \n A <a href=\"http://www.last.fm/tag/jukebox%20musical\" class=\"bbcode_tag\" rel=\"tag\">jukebox musical</a> based on the music of ABBA titled <em><a href=\"http://en.wikipedia.org/wiki/Mamma_Mia!\" rel=\"nofollow\">Mamma Mia!</a></em> opened in London's West End in 1999 and has since become one of the most popular musicals in the world. In 2008, a movie version was made with <a href=\"http://www.last.fm/music/Meryl+Streep\" class=\"bbcode_artist\">Meryl Streep</a> among the cast.\n \n The recent <a href=\"http://rockhall.com/inductees/abba/\" rel=\"nofollow\">Rock and Roll Hall of Fame 2010 Induction Ceremony</a>, held on March 15 in New York, recognized the huge contribution of ABBA. Represented by Anni-Frid Lyngstad &quot;Frida&quot; and former husband/band member Benny Andersson, it was perhaps the crowning acknowledgment of the phenomenal success of Sweden's most famous four. \n \n \n    \nUser-contributed text is available under the Creative Commons By-SA License and may also be available under the GNU FDL.";

	@Test
	public void testdataOnClasspath() {
		new ResourceUtil(ARTIST_INFO_FILE);
	}
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		ArtistInfoParser parser = new ArtistInfoParserImpl(
				new ResourceUtil(ARTIST_INFO_FILE).getInputStream());
		
		ArtistInfo artistInfo = parser.getArtistInfo();
		
		assertEquals(new Artist("ABBA"), artistInfo.getArtist());
		
		assertEquals(SMALL_IMAGE_URL, artistInfo.getSmallImageUrl());
		assertEquals(MEDIUM_IMAGE_URL, artistInfo.getMediumImageUrl());
		assertEquals(LARGE_IMAGE_URL, artistInfo.getLargeImageUrl());
		assertEquals(EXTRA_LARGE_IMAGE_URL, artistInfo.getExtraLargeImageUrl());
		
		assertEquals(LISTENERS, artistInfo.getListeners());
		assertEquals(PLAY_COUNT, artistInfo.getPlayCount());
		
		assertEquals(BIO_SUMMARY, artistInfo.getBioSummary());
		assertEquals(BIO_CONTENT, artistInfo.getBioContent());
	}

}