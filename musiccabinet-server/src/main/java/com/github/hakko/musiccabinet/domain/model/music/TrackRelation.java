package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class TrackRelation {

	private Track target;
	private float match;
	
	public TrackRelation() {
	}
	
	public TrackRelation(Track target, float match) {
		setTarget(target);
		setMatch(match);
	}

	public Track getTarget() {
		return target;
	}

	public final void setTarget(Track target) {
		this.target = target;
	}

	public float getMatch() {
		return match;
	}

	public final void setMatch(float match) {
		this.match = match;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(target)
		.append(match)
		.toHashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  TrackRelation tr = (TrackRelation) o;
          return new EqualsBuilder()
          .append(target, tr.target)
          .append(match, tr.match)
          .isEquals();
	}

	@Override
	public String toString() {
		return "track relation to " + target + ", match " + match;
	}
	
}