package com.github.hakko.musiccabinet.dao.jdbc;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;
import com.github.hakko.musiccabinet.domain.model.aggr.TagTopArtists;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcTagDaoTest {

	@Autowired
	private JdbcTagDao dao;

	@Autowired
	private JdbcArtistTopTagsDao artistTopTagsDao;
	
	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, PostgreSQLFunction.UPDATE_TAG_TOP_ARTISTS);
	}
	
	@Test
	public void topTagsReturnsAnUnspecifiedList() {
		dao.getTopTags();
	}

	@Test
	public void canStoreAndRetrieveSingleTopTag() {
		deleteTags();
		
		List<String> singleTag = Arrays.asList("disco");
		
		dao.createTags(singleTag);
		dao.setTopTags(singleTag);
		List<String> topTags = dao.getTopTags();
		
		Assert.assertNotNull(topTags);
		Assert.assertEquals(1, topTags.size());
		Assert.assertEquals(singleTag.get(0), topTags.get(0));
	}

	@Test
	public void canStoreAndRetrieveMultipleTopTags() {
		deleteTags();
		
		String tag1 = "disco", tag2 = "pop", tag3 = "americana";
		
		List<String> multipleTags = Arrays.asList(tag1, tag2, tag3);
		
		dao.createTags(multipleTags);
		dao.setTopTags(multipleTags);
		List<String> topTags = dao.getTopTags();
		
		Assert.assertNotNull(topTags);
		Assert.assertEquals(3, topTags.size());
		Assert.assertTrue(topTags.contains(tag1));
		Assert.assertTrue(topTags.contains(tag2));
		Assert.assertTrue(topTags.contains(tag3));
	}
	
	@Test
	public void canStoreAndRetrieveTagIds() {
		deleteTags();
		
		List<String> tagNames = Arrays.asList("disco", "sludge");
		dao.createTags(tagNames);
		
		List<Tag> tags = dao.getTags();
		
		Assert.assertEquals(2, tags.size());
		Assert.assertFalse(tags.get(0).getId() == tags.get(1).getId());
		Assert.assertFalse(tags.get(0).getName().equals(tags.get(1).getName()));
		
		for (String tagName : tagNames) {
			Assert.assertTrue(
					tagName.equals(tags.get(0).getName()) ||
					tagName.equals(tags.get(1).getName()));
		}
	}

	@Test
	public void tagsInitiallyDontHaveTopArtists() {
		deleteTags();
		
		List<String> tagNames = Arrays.asList("disco", "sludge");
		dao.createTags(tagNames);
		dao.setTopTags(tagNames);
		
		List<Tag> tags = dao.getTagsWithoutTopArtists();

		Assert.assertEquals(2, tags.size());
	}

	@Test
	public void tagsWithTopArtistsAreNotPickedForUpdate() {
		deleteTags();
		
		List<String> tagNames = Arrays.asList("disco", "sludge");
		dao.createTags(tagNames);
		dao.setTopTags(tagNames);

		List<TagTopArtists> topArtists = Arrays.asList(
				new TagTopArtists("disco", asList(new Artist("Madonna"))));
		dao.createTopArtists(topArtists);
		
		List<Tag> tags = dao.getTagsWithoutTopArtists();

		Assert.assertEquals(1, tags.size());
		Assert.assertEquals("sludge", tags.get(0).getName());
	}
	
	@Test
	public void createsTagCorrections() {
		deleteTags();
		
		dao.createTags(Arrays.asList("disco", "80s", "pop"));
		
		Map<String, String> corrections = new HashMap<>();
		corrections.put("80s", "disco");
		dao.createTagCorrections(corrections);
		
		Map<String, String> correctedTags = dao.getCorrectedTags();
		
		Assert.assertNotNull(correctedTags);
		Assert.assertEquals(1, correctedTags.size());
		Assert.assertTrue(correctedTags.containsKey("80s"));
		Assert.assertEquals("disco", correctedTags.get("80s"));
	}
	
	@Test
	public void createsNewTagsWhenCreatingCorrections() {
		deleteTags();
		
		Map<String, String> corrections = new HashMap<>();
		corrections.put("sludge", "drone");
		
		dao.createTags(Arrays.asList("disco", "sludge"));
		dao.createTagCorrections(corrections);
		
		List<Tag> tags = dao.getTags();
		Assert.assertNotNull(tags);
		Assert.assertEquals(3, tags.size());
		for (String tagName : Arrays.asList("disco", "sludge", "drone")) {
			Assert.assertTrue(tags.contains(new Tag(tagName, (short) 0)));
		}
	}
	
	@Test
	public void storesRealWorldCorrections() {
		deleteTags();
		
		String correctionsString = "r'n'b=rnb, german hiphop=german, progressive house=house, party=deutsch, post-punk=punk, melodic metal=metal, nu jazz=jazz, r and b=rnb, rock and roll=rock'n'roll, 2011=10s, spoken word=speech, indie pop=pop, orchestral=orchestra, soundtracks=soundtrack, jazz fusion=jazz, volksmusik=schlager, europop=pop, malle=deutsch, ostrock=deutsch, post punk=punk, franÃ§ais=french, duet=duets, pardy=parody, electroclash=electro, glam metal=rock, bluegrass=blues, street punk=punk, j-pop=pop, funky=funk, industrial rock=rock, progressive rock=rock, hip hop=hip-hop, contemporary classical=classical, electropop=electro, glam rock=rock, punk rock=punk, ndw=deutsch, musicals=musical, deutsche schlager=schlager, smooth jazz=jazz, britpop=british, france=french, hair metal=metal, aussie=australian, sido=deutsch, outlaw country=country, sweden=swedish, uk=british, melodic punk=punk, emocore=emo, electronic=electro, hamburg=german, punkrock=punk, stimmung=deutsch, art rock=rock, gangsta=gangsta rap, berlin=german, bavarian=german, jazz vocal=jazz, schoenemusik=deutsch, eurodance=dance, tech-house=house, deutschrap=german, melodic death metal=metal, alt-country=country, guitar virtuoso=guitar, tech trance=trance, italo disco=italian, eurovision song contest=eurovision, saufen=deutsch, progressive metal=metal, germany=german, r&b=rnb, hard trance=trance, girl groups=female vocalists, ost=soundtrack, hardcore punk=punk, alternative=rock, neue deutsche welle=deutsch, belgium=belgian, alternative rock=rock, krautrock=deutsch, stoner rock=rock, industrial metal=metal, progressive death metal=metal, powerpop=pop, irish country=irish, garage=rock, viking metal=metal, ska-punk=punk, indie rock=rock, mundart=deutsch, j-rock=rock, jazz rock=jazz, ska punk=punk, minimal techno=techno, black metal=metal, female vocalist=female vocalists, 2010s=00s, ballermann=german, classic=classical, drum n bass=drum and bass, dream pop=pop, deep house=house, female vocals=female vocalists, electronica=electro, german artists=german, brutal death metal=death metal, christian rock=christian, female=female vocalists, death metal=metal, dansk=danish, vocal house=house, folk metal=folk, ireland=irish, garage rock=rock, soft rock=rock, noise rock=rock, country rock=country, deutschsprachig=german, female fronted metal=female vocalists, dancehall=dance, german hip hop=german, italian pop=italian, modern country=country, karneval=german, pop-rock=rock, 80's=80s, malzbier=german, groovy=groove, nu metal=metal, latin pop=pop, metalcore=metal, disco house=disco, chanson francaise=chanson, folk punk=folk, relaxing=relax, german punk=german, rock n roll=rock'n'roll, futurepop=pop, gothic metal=gothic, electro house=electro, covers=cover, hiphop=hip-hop, pop punk=punk, west coast rap=rap, memphis rap=rap, audiobooks=audiobook, deutsch rock=deutschrock, gothic rock=gothic, psychedelic rock=rock, deutscher hip hop=german, female voices=female vocalists, surf rock=rock, acid jazz=jazz, pagan metal=metal, psytrance=trance, male vocalist=male vocalists, pop rock=rock, doom metal=metal, horror punk=punk, happy hardcore=hardcore, synth pop=pop, french pop=french, southern rock=rock, discofox=disco, rocksteady=rock'n'roll, melodic trance=trance, goth=gothic, club=house, post-rock=rock, hardcore rap=rap, progressive trance=trance, vocal trance=trance, post-metal=metal, symphonic metal=metal, spain=spanish, rapcore=rap, hoerbuch=audiobook, liedermacher=singer-songwriter, psychedelic trance=trance, uplifting trance=trance, latino=pop, reggaeton=reggae, german hip-hop=german, gangsta rap=rap, austrian=austria, acoustic rock=rock, irish folk=irish, math rock=rock, power pop=pop, deutsch=german, underground rap=rap, blues rock=blues, songwriter=singer-songwriter, dark electro=electro, pop-punk=punk, teen pop=pop, funky house=funk, thrash metal=metal, club house=house, mallorca=deutsch, progressive psytrance=trance, stoner metal=metal, nederlandstalig=netherlands, synthpop=pop, alternative metal=metal, deutsch rap=german, melodic=rock, african=africa, composers=composer, melodic rock=rock, southern rap=rap, italiana=italian, female vocal=female vocalists, streetpunk=punk, east coast rap=rap, electro-industrial=electro, german rap=german, hard dance=dance, switzerland=swiss, dark metal=metal, italodance=italian, koelsch=german, groove metal=groove, vocal jazz=jazz, skate punk=punk, power metal=metal, proto-punk=punk, rockabilly=rock'n'roll, nu-metal=metal, partymusik=deutsch, rhythm and blues=rnb, austropop=austria, underground hip-hop=hip-hop, trip-hop=hip-hop, technical death metal=metal, tech house=house, folk rock=folk, mittelalter=deutsch";
		String[] correctionsArray = StringUtils.split(correctionsString, ",");
		Map<String, String> correctionsMap = new HashMap<>();
		for (String correction : correctionsArray) {
			String[] pair = StringUtils.split(correction, "=");
			correctionsMap.put(pair[0].trim(), pair[1].trim());
		}
		Assert.assertEquals("rnb", correctionsMap.get("r'n'b"));
		Assert.assertEquals("german", correctionsMap.get("german hiphop"));
		
		dao.createTagCorrections(correctionsMap);
		
		Map<String, String> correctedTags = dao.getCorrectedTags();
		
		Assert.assertEquals(correctionsMap, correctedTags);
	}
	
	@Test
	public void storedRealWorldTopTags() {
		deleteTags();
		
		String topTagsString = "hard rock, italian, gothic, relax, hip-hop, rock, pop, irish, house, french, belgian, composer, cover, funk, parody, singer-songwriter, metal, gangsta rap, hardcore, disco, female vocalists, deutsch, trance, 10s, spanish, deutschrock, musical, swedish, german, groove, 00s, country, soundtrack, speech, male vocalists, classical, dance, austria, electro, 80s, reggae, christian, rock'n'roll, heavy metal, emo, australian, eurovision, rap, orchestra, 90s, punk, audiobook, swiss, techno, classic rock, rnb, 70s, deutschpunk, 60s, africa, blues, schlager, british, folk, jazz, chanson, drum and bass, duets, netherlands, death metal, danish, guitar, 50s";
		List<String> topTags = Arrays.asList(StringUtils.split(topTagsString, ","));
		
		dao.createTags(topTags);
		dao.setTopTags(topTags);
		
		List<String> dbTopTags = dao.getTopTags();

		Collections.sort(topTags);
		Assert.assertEquals(topTags, dbTopTags);
	}
	
	@Test
	public void retrievesCorrectedAvailableTags() {
		deleteTags();

		for (int i = 0; i < 5; i++) {
			artistTopTagsDao.createTopTags(new Artist("artist" + i), Arrays.asList(
					new Tag("sludge", (short) 100), 
					new Tag("drone", (short) 90),
					new Tag("e-l-e-c-t-r-o", (short) 50),
					new Tag("disco", (short) 10)));
		}

		Map<String, String> tagCorrections = new HashMap<>();
		tagCorrections.put("e-l-e-c-t-r-o", "electro");
		dao.createTagCorrections(tagCorrections);

		dao.setTopTags(Arrays.asList("sludge"));
		
		List<TagOccurrence> tags = dao.getAvailableTags();
		Assert.assertEquals(3, tags.size());
		Assert.assertEquals("drone", tags.get(0).getTag());
		Assert.assertEquals("e-l-e-c-t-r-o", tags.get(1).getTag());
		Assert.assertEquals("electro", tags.get(1).getCorrectedTag());
		Assert.assertEquals("sludge", tags.get(2).getTag());
		
		Assert.assertFalse(tags.get(0).isUse());
		Assert.assertFalse(tags.get(1).isUse());
		Assert.assertTrue(tags.get(2).isUse());
	}
	
	private void deleteTags() {
		dao.getJdbcTemplate().execute("truncate music.tag cascade");
	}

}