package com.github.hakko.musiccabinet.parser.lastfm;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.TagInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class TagInfoParserTest {
	
	private static final String TAG_INFO_FILE = 
		"last.fm/xml/taginfo.disco.xml";

	// constant values below are copied from file above

	private static final String NAME = "disco";
	private static final String SUMMARY = "Disco is a genre of dance-oriented music that originated in African American, gay and Hispanic American communities in 1970s. In what is considered a forerunner to disco style clubs in February 1970 New York City DJ David Mancuso opened The Loft, a members-only private dance club set in his own home. Most agree that the first disco songs were released in 1973, though some claim that Soul Makossa by <span title=\"Unknown artist\" class=\"bbcode_unknown\">Manu Dibango's</span> from 1972 to be the first disco record. The first article about disco was written in September 1973 by Vince Aletti for Rolling Stone Magazine.";
	private static final String CONTENT = "Disco is a genre of dance-oriented music that originated in African American, gay and Hispanic American communities in 1970s. In what is considered a forerunner to disco style clubs in February 1970 New York City DJ David Mancuso opened The Loft, a members-only private dance club set in his own home. Most agree that the first disco songs were released in 1973, though some claim that Soul Makossa by <span title=\"Unknown artist\" class=\"bbcode_unknown\">Manu Dibango's</span> from 1972 to be the first disco record. The first article about disco was written in September 1973 by Vince Aletti for Rolling Stone Magazine. In 1974 New York City's WPIX-FM premiered the first disco radio show.\n \n Musical influences include funk, soul music. The disco sound has a soaring, often reverberated vocals over a steady &quot;four-on-the-floor&quot; beat, an eighth note (quaver) or sixteenth note (semi-quaver) hi-hat pattern with an open hi-hat on the off-beat, and prominent, syncopated electric bass line. Strings, horns, electric pianos, and electric guitars create a lush background sound. Orchestral instruments such as the flute are often used for solo melodies, and unlike in rock, lead guitar is rarely used.\n \n Well-known late 1970s disco performers included <a href=\"http://www.last.fm/music/Bee+Gees\" class=\"bbcode_artist\">Bee Gees</a>, <a href=\"http://www.last.fm/music/Donna+Summer\" class=\"bbcode_artist\">Donna Summer</a> and <a href=\"http://www.last.fm/music/The+Jacksons\" class=\"bbcode_artist\">The Jacksons</a>. Summer would become the first well-known and most popular female disco artist, and also played a part in pioneering the electronic sound that later became a part of disco (see below). While performers and singers garnered the lion's share of public attention, the behind-the-scenes producers played an equal, if not more important role in disco, since they often wrote the songs and created the innovative sounds and production techniques that were part of the &quot;disco sound&quot;. Many non-disco artists recorded disco songs at the height of disco's popularity, and films such as Saturday Night Fever and Thank God It's Friday contributed to disco's rise in mainstream popularity and ironically the beginning of its commercial decline. \n \n The resurgence of Disco, the second generation of Disco artists, commonly referred to as &quot;Dance&quot; or &quot;Dance Pop&quot; artists took shape. By this point, this style of music no longer favoured only the highly orchestral song structure. Post-disco (club music or dance) is the significant period in popular music history that followed the commercial &quot;death&quot; of disco music that emerged during late 1970s and early 1980s. The stripped-down musical trends followed from the DJ- and producer-driven, increasingly electronic and experimental side of disco, and were typified by the styles of dance-pop, boogie, <a href=\"http://www.last.fm/tag/italo%20disco\" class=\"bbcode_tag\" rel=\"tag\">italo disco</a> and the early alternative dance. <a href=\"http://www.last.fm/tag/techno\" class=\"bbcode_tag\" rel=\"tag\">techno</a> and <a href=\"http://www.last.fm/tag/house\" class=\"bbcode_tag\" rel=\"tag\">house</a> are both rooted in post-disco music. From 1990 forward, while not commonly referred to as Disco,  continued to gain mainstream success. By the 2000s, the direct decendant of Disco, called Nu Disco also started taking shape.";
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		TagInfoParser parser = new TagInfoParserImpl(
				new ResourceUtil(TAG_INFO_FILE).getInputStream());
		
		TagInfo tagInfo = parser.getTagInfo();
		
		Assert.assertEquals(NAME, tagInfo.getTagName());
		Assert.assertEquals(SUMMARY, tagInfo.getSummary());
		Assert.assertEquals(CONTENT, tagInfo.getContent());
	}
	
}