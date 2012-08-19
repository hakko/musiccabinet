package com.github.hakko.musiccabinet.domain.model.music;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Tag {

	private int id;
	private String name;
	private short count;
	
	public Tag(int id, String name) {
		setId(id);
		setName(name);
	}
	
	public Tag(String name, short count) {
		setName(name);
		setCount(count);
	}

	public Tag() {
		
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.toLowerCase();
	}

	public short getCount() {
		return count;
	}

	public void setCount(short count) {
		this.count = count;
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