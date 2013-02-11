package com.github.hakko.musiccabinet.service;

import static java.lang.Math.max;
import static java.util.Arrays.asList;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.hakko.musiccabinet.domain.model.aggr.SearchIndexUpdateProgress;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.lastfm.AlbumInfoService;
import com.github.hakko.musiccabinet.service.lastfm.ArtistInfoService;
import com.github.hakko.musiccabinet.service.lastfm.ArtistRelationService;
import com.github.hakko.musiccabinet.service.lastfm.ArtistTopTagsService;
import com.github.hakko.musiccabinet.service.lastfm.ArtistTopTracksService;
import com.github.hakko.musiccabinet.service.lastfm.GroupWeeklyArtistChartService;
import com.github.hakko.musiccabinet.service.lastfm.ScrobbledTracksService;
import com.github.hakko.musiccabinet.service.lastfm.SearchIndexUpdateExecutorService;
import com.github.hakko.musiccabinet.service.lastfm.SearchIndexUpdateService;
import com.github.hakko.musiccabinet.service.lastfm.SearchIndexUpdateSettingsService;
import com.github.hakko.musiccabinet.service.lastfm.TagInfoService;
import com.github.hakko.musiccabinet.service.lastfm.TagTopArtistsService;
import com.github.hakko.musiccabinet.service.lastfm.UserLovedTracksService;
import com.github.hakko.musiccabinet.service.lastfm.UserRecommendedArtistsService;
import com.github.hakko.musiccabinet.service.lastfm.UserTopArtistsService;
import com.github.hakko.musiccabinet.service.library.LibraryScannerService;

public class LibraryUpdateService {

	private LibraryScannerService libraryScannerService;
	private LibraryBrowserService libraryBrowserService;
	
	private ArtistRelationService artistRelationService;
    private ArtistTopTracksService artistTopTracksService;
    private ArtistTopTagsService artistTopTagsService;
    private ArtistInfoService artistInfoService;
    private AlbumInfoService albumInfoService;
    private TagInfoService tagInfoService;
    private TagTopArtistsService tagTopArtistsService;
    private GroupWeeklyArtistChartService groupWeeklyArtistChartService;
    private UserTopArtistsService userTopArtistsService;
    private UserRecommendedArtistsService userRecommendedArtistsService;
    private UserLovedTracksService userLovedTracksService;
    private ScrobbledTracksService scrobbledTracksService;
    
    private PlaylistGeneratorService playlistGeneratorService;
    private SearchIndexUpdateExecutorService executorService;
    private SearchIndexUpdateSettingsService settingsService;
    
    private boolean isIndexBeingCreated = false;
    
	private static final Logger LOG = Logger.getLogger(LibraryUpdateService.class);

	public boolean isIndexCreated() {
		return libraryBrowserService.hasArtists();
	}
	
	public boolean isIndexBeingCreated() {
		return libraryScannerService.isLibraryBeingScanned() ||
				isIndexBeingCreated;
	}
	
	public void createSearchIndex(Set<String> paths, boolean isRootPaths, boolean offlineScan, boolean onlyNewArtists) throws ApplicationException {
		if (isIndexBeingCreated()) {
			LOG.debug("Search index is being created. Additional update cancelled.");
			return;
		}
		
		isIndexBeingCreated = true;
		LOG.info("Starting library update. Scan " + paths + ", offline = " + offlineScan);

    	for (SearchIndexUpdateService updateService : getUpdateServices()) {
    		updateService.reset();
    	}

		long millis = -System.currentTimeMillis();
		libraryScannerService.update(paths, isRootPaths);
		millis += System.currentTimeMillis();
		LOG.info("Library scanned in " + (millis / 1000) + " seconds.");

		if (!offlineScan) {
			if (canConnectToLastFm()) {
				updateLastFmData(onlyNewArtists);
				playlistGeneratorService.updateSearchIndex();
			} else {
				LOG.warn("Could not connect to last.fm, no data fetched.");
			}
		}
		
		LOG.info("Finishing library update.");
		isIndexBeingCreated = false;
	}
	
	private void updateLastFmData(boolean onlyUpdateNewArtists) {
		LOG.debug("Starting last.fm update.");
		settingsService.setOnlyUpdateNewArtists(onlyUpdateNewArtists);
    	long millis = -System.currentTimeMillis();
    	executorService.updateSearchIndex(onlyUpdateNewArtists ? 
    			getUpdateServicesForNewArtists() : getUpdateServices());
    	executorService.updateSearchIndex(asList(
    			tagInfoService, tagTopArtistsService)); // re-run for new tags
    	millis += System.currentTimeMillis();

    	int total = 0;
    	for (SearchIndexUpdateService updateService : getUpdateServices()) {
    		total += max(0, updateService.getProgress().getTotalOperations());
    		LOG.info(updateService.getProgress().getTotalOperations() + " " + 
    				updateService.getUpdateDescription() + ".");
    	}
    	LOG.info("In total, " + total + " last.fm operations done in " + (millis / 1000) + " sec.");
	}
	
    private boolean canConnectToLastFm() {
    	for (int i = 0; i < 20; i++) {
    		LOG.debug("Check if last.fm can be looked up...");
    		InetSocketAddress isa = new InetSocketAddress("last.fm", 80);
    		if (isa.isUnresolved()) {
    			LOG.debug("Failed, sleep and try again.");
    			try {
    				Thread.sleep(15000);
    			} catch (InterruptedException e1) {
    			}
    		} else {
    			LOG.debug("That went well, return true.");
    			return true;
    		}
    	}
    	
    	LOG.warn("Tried connecting to last.fm for five minutes, give up.");
    	return false;
    }

    private List<SearchIndexUpdateService> getUpdateServicesForNewArtists() {
    	return asList(
        		artistRelationService, artistTopTracksService,
    			artistTopTagsService, artistInfoService,
    			albumInfoService);
    }
    
    private List<SearchIndexUpdateService> getUpdateServices() {
    	return asList(
        		artistRelationService, artistTopTracksService,
    			artistTopTagsService, artistInfoService, 
    			albumInfoService, tagInfoService,
    			groupWeeklyArtistChartService,
    			tagTopArtistsService, userTopArtistsService, 
    			userRecommendedArtistsService, userLovedTracksService,
    			scrobbledTracksService);
    }
    
	public List<SearchIndexUpdateProgress> getSearchIndexUpdateProgress() {
		List<SearchIndexUpdateProgress> updateProgress = new ArrayList<>();

		updateProgress.addAll(libraryScannerService.getUpdateProgress());
		for (SearchIndexUpdateService updateService : getUpdateServices()) {
			updateProgress.add(updateService.getProgress());
		}
		
		return updateProgress;
	}

	// Spring setters

	public void setLibraryScannerService(LibraryScannerService libraryScannerService) {
		this.libraryScannerService = libraryScannerService;
	}

	public void setLibraryBrowserService(LibraryBrowserService libraryBrowserService) {
		this.libraryBrowserService = libraryBrowserService;
	}

	public void setAlbumInfoService(AlbumInfoService albumInfoService) {
		this.albumInfoService = albumInfoService;
	}

	public void setArtistRelationService(ArtistRelationService artistRelationService) {
		this.artistRelationService = artistRelationService;
	}

	public void setArtistTopTracksService(ArtistTopTracksService artistTopTracksService) {
		this.artistTopTracksService = artistTopTracksService;
	}

	public void setArtistTopTagsService(ArtistTopTagsService artistTopTagsService) {
		this.artistTopTagsService = artistTopTagsService;
	}

	public void setArtistInfoService(ArtistInfoService artistInfoService) {
		this.artistInfoService = artistInfoService;
	}

	public void setPlaylistGeneratorService(PlaylistGeneratorService playlistGeneratorService) {
		this.playlistGeneratorService = playlistGeneratorService;
	}

	public void setScrobbledTracksService(ScrobbledTracksService scrobbledTracksService) {
		this.scrobbledTracksService = scrobbledTracksService;
	}

	public void setTagInfoService(TagInfoService tagInfoService) {
		this.tagInfoService = tagInfoService;
	}

	public void setTagTopArtistsService(TagTopArtistsService tagTopArtistsService) {
		this.tagTopArtistsService = tagTopArtistsService;
	}

	public void setGroupWeeklyArtistChartService(GroupWeeklyArtistChartService groupWeeklyArtistChartService) {
		this.groupWeeklyArtistChartService = groupWeeklyArtistChartService;
	}

	public void setUserTopArtistsService(UserTopArtistsService userTopArtistsService) {
		this.userTopArtistsService = userTopArtistsService;
	}

	public void setUserRecommendedArtistsService(UserRecommendedArtistsService userRecommendedArtistsService) {
		this.userRecommendedArtistsService = userRecommendedArtistsService;
	}

	public void setUserLovedTracksService(UserLovedTracksService userLovedTracksService) {
		this.userLovedTracksService = userLovedTracksService;
	}

	public void setSearchIndexUpdateExecutorService(SearchIndexUpdateExecutorService executorService) {
		this.executorService = executorService;
	}

	public void setSearchIndexUpdateSettingsService(SearchIndexUpdateSettingsService settingsService) {
		this.settingsService = settingsService;
	}
	
}