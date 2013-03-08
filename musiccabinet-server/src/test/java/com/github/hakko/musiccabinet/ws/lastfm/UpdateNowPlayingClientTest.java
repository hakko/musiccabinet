package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static java.io.File.separatorChar;
import static java.lang.Thread.currentThread;

import java.io.File;
import java.util.List;

import org.apache.http.NameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.Scrobble;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.library.LibraryScannerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class UpdateNowPlayingClientTest extends AbstractWSImplementationTest {

	@Autowired
	private LibraryScannerService scannerService;

	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	@Before
	public void clearLibrary() throws Exception {
		PostgreSQLUtil.truncateTables(browserDao);

		String library = new File(currentThread().getContextClassLoader()
				.getResource("library").toURI()).getAbsolutePath();
		String media0 = library + separatorChar + "media0";

		scannerService.add(set(media0));
	}

	@Test
	public void validateParameters() throws ApplicationException {

		final Track track = browserDao.getTracks(browserDao.getRandomTrackIds(1)).get(0);
		final LastFmUser user = new LastFmUser("lastFmUser", "sessionKey");
		final Scrobble scrobble = new Scrobble(user, track, false);

		final String method = UpdateNowPlayingClient.METHOD;

		new UpdateNowPlayingClient() {
			@Override
			protected WSResponse executeWSRequest(List<NameValuePair> params) throws ApplicationException {

				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_ARTIST, track.getArtist().getName());
				assertHasParameter(params, PARAM_TRACK, track.getName());
				assertHasParameter(params, PARAM_DURATION, "" + track.getMetaData().getDuration());
				assertHasParameter(params, PARAM_SK, user.getSessionKey());

				return null;
			}

		}.updateNowPlaying(scrobble);
	}
	
}