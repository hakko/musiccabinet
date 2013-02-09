package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class TagInfo {

	private String tagName;
	private String summary;
	private String content;

	public TagInfo() {
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(tagName)
		.append(summary)
		.append(content)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		TagInfo ai = (TagInfo) o;
		return new EqualsBuilder()
		.append(tagName, ai.tagName)
		.append(summary, ai.summary)
		.append(content, ai.content)
		.isEquals();
	}

}