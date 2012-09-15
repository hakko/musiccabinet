package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.github.hakko.musiccabinet.domain.model.music.ArtistPlayCount;

public class GroupWeeklyArtistChart {

	private String groupName;
	private List<ArtistPlayCount> artistPlayCounts;
	
	public GroupWeeklyArtistChart(String groupName, List<ArtistPlayCount> artistPlayCounts) {
		this.groupName = groupName;
		this.artistPlayCounts = artistPlayCounts;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<ArtistPlayCount> getArtistPlayCounts() {
		return artistPlayCounts;
	}

	public void setArtistPlayCounts(List<ArtistPlayCount> artistPlayCounts) {
		this.artistPlayCounts = artistPlayCounts;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(groupName)
		.append(artistPlayCounts)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  GroupWeeklyArtistChart gwac = (GroupWeeklyArtistChart) o;
          return new EqualsBuilder()
          .append(groupName, gwac.groupName)
          .append(artistPlayCounts, gwac.artistPlayCounts)
          .isEquals();
	}

}