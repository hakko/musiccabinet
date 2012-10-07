package com.github.hakko.musiccabinet.domain.model.aggr;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/*
 * TagOccurrence doesn't map to a single database table but is rather aggregated
 * from music.tag, music.artisttoptag, library.toptag.
 */
public class TagOccurrence {

	private String tag;
	private String correctedTag;
	private int occurrence;
	private boolean use;
	
	public TagOccurrence(String tag, String correctedTag, int occurrence, boolean use) {
		this.tag = tag;
		this.correctedTag = correctedTag;
		this.occurrence = occurrence;
		this.use = use;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCorrectedTag() {
		return correctedTag;
	}

	public void setCorrectedTag(String correctedTag) {
		this.correctedTag = correctedTag;
	}

	public int getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(int occurrence) {
		this.occurrence = occurrence;
	}

	public boolean isUse() {
		return use;
	}

	public void setUse(boolean use) {
		this.use = use;
	}
	
	// tag is used as primary id, which is what we want to use for equals method.
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(tag)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		TagOccurrence to = (TagOccurrence) o;
		return new EqualsBuilder()
		.append(tag, to.tag)
		.isEquals();
	}
	
	@Override
	public String toString() {
		return "[tag=" + tag + ", corrected=" + correctedTag + ", occurrence=" + occurrence + ", use=" + use + "]";
	}

}