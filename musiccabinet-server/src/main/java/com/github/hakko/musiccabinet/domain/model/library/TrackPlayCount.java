package com.github.hakko.musiccabinet.domain.model.library;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.github.hakko.musiccabinet.domain.model.music.Track;

public class TrackPlayCount {

	private Track track;
	private int playCount;
	
	public TrackPlayCount() {
	}
	
	public TrackPlayCount(String artistName, String trackName, int playCount) {
		track = new Track(artistName, trackName);
		this.playCount = playCount;
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(track)
		.append(playCount)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  TrackPlayCount tpc = (TrackPlayCount) o;
          return new EqualsBuilder()
          .append(track, tpc.track)
          .append(playCount, tpc.playCount)
          .isEquals();
	}
	
}