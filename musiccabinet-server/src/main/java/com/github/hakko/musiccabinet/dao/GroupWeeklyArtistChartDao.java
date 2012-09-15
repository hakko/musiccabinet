package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.GroupWeeklyArtistChart;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;

public interface GroupWeeklyArtistChartDao {

	void createArtistCharts(List<GroupWeeklyArtistChart> artistCharts);
	GroupWeeklyArtistChart getWeeklyArtistChart(LastFmGroup group);

}