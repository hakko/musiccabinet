package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Tag {

	private String name;
	private short count;
	
	public Tag(String name, short count) {
		setName(name);
		setCount(count);
	}

	public Tag() {
		
	}
	
	public final void setName(String name) {
		this.name = name.toLowerCase();
	}

	public final void setCount(short count) {
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public short getCount() {
		return count;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(name)
		.append(count)
		.toHashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  Tag t = (Tag) o;
          return new EqualsBuilder()
          .append(name, t.name)
          .append(count, t.count)
          .isEquals();
	}
	
	@Override
	public String toString() {
		return name + " [" + count + "]";
	}

}