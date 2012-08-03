package com.github.hakko.musiccabinet.domain.model.aggr;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class Scrobble {

	private LastFmUser lastFmUser;
	private Track track;
	private boolean submission;
	private DateTime startTime = new DateTime();

	public Scrobble(LastFmUser lastFmUser, Track track, boolean submission) {
		this.lastFmUser = lastFmUser;
		this.track = track;
		this.submission = submission;
	}
	
	public LastFmUser getLastFmUser() {
		return lastFmUser;
	}

	public Track getTrack() {
		return track;
	}

	public boolean isSubmission() {
		return submission;
	}

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

	public DateTime getStartTime() {
		return startTime;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(lastFmUser)
		.append(track.getId())
		.append(startTime)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		Scrobble s = (Scrobble) o;
		return new EqualsBuilder()
		.append(lastFmUser, s.lastFmUser)
		.append(track.getId(), s.track.getId())
		.append(startTime, s.startTime)
		.isEquals();
	}

}