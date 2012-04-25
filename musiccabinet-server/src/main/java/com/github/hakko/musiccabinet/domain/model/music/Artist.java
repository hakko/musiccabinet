package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Artist {

	private String name;
	
	public Artist() {
		
	}
	
	public Artist(String name) {
		setName(name);
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Artist name cannot be set to null.");
		}
		this.name = name;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(name)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		Artist a = (Artist) o;
		return new EqualsBuilder()
		.append(name, a.name)
		.isEquals();
	}
	
	@Override
	public String toString() {
		return "artist " + name;
	}

}