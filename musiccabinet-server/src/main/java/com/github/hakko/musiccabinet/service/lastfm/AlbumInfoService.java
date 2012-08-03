package com.github.hakko.musiccabinet.service.lastfm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.hakko.musiccabinet.dao.AlbumInfoDao;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.parser.lastfm.AlbumInfoParser;
import com.github.hakko.musiccabinet.parser.lastfm.AlbumInfoParserImpl;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.AlbumInfoClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services related to updating/getting info for albums.
 * 
 */
public class AlbumInfoService extends SearchIndexUpdateService {

	protected AlbumInfoClient albumInfoClient;
	protected AlbumInfoDao albumInfoDao;
	protected WebserviceHistoryService webserviceHistoryService;
	
	private static final int BATCH_SIZE = 1000;
	
	private static final Logger LOG = Logger.getLogger(AlbumInfoService.class);
	
	public List<AlbumInfo> getAlbumInfosForArtist(int artistId) {
		return albumInfoDao.getAlbumInfosForArtist(artistId);
	}
	
	public Map<Integer, AlbumInfo> getAlbumInfosForAlbumIds(List<Integer> ids) {
		return ids.isEmpty() ? new HashMap<Integer, AlbumInfo>() :
			albumInfoDao.getAlbumInfosForIds(ids);
	}
	
	@Override
	protected void updateSearchIndex() throws ApplicationException {
		List<Album> albums = albumInfoDao.getAlbumsWithoutInfo();
		
		List<AlbumInfo> albumInfos = new ArrayList<>();
		setTotalOperations(albums.size());
		
		for (Album album : albums) {
			try {
				WSResponse wsResponse = albumInfoClient.getAlbumInfo(album);
				if (wsResponse.wasCallAllowed() && wsResponse.wasCallSuccessful()) {
					StringUtil stringUtil = new StringUtil(wsResponse.getResponseBody());
					AlbumInfoParser aiParser = 
						new AlbumInfoParserImpl(stringUtil.getInputStream());
					albumInfos.add(aiParser.getAlbumInfo());
					
					if (albumInfos.size() == BATCH_SIZE) {
						albumInfoDao.createAlbumInfo(albumInfos);
						albumInfos.clear();
					}
				}
			} catch (ApplicationException e) {
				LOG.warn("Fetching album info for " + album.getName() + " failed.", e);
			}
			addFinishedOperation();
		}

		albumInfoDao.createAlbumInfo(albumInfos);
	}

	@Override
	public String getUpdateDescription() {
		return "album descriptions";
	}
	
	// Spring setters

	public void setAlbumInfoClient(AlbumInfoClient albumInfoClient) {
		this.albumInfoClient = albumInfoClient;
	}

	public void setAlbumInfoDao(AlbumInfoDao albumInfoDao) {
		this.albumInfoDao = albumInfoDao;
	}

	public void setWebserviceHistoryService(
			WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}

}