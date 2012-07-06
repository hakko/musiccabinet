package com.github.hakko.musiccabinet.domain.model.library;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

public class File {

	private String directory;
	private String filename;
	private DateTime modified;
	private int size;
	private MetaData metaData;
	
	public File(String directory, String filename, DateTime modified, int size) {
		this.directory = directory;
		this.filename = filename;
		this.modified = modified;
		this.size = size;
	}

	public String getDirectory() {
		return directory;
	}

	public String getFilename() {
		return filename;
	}

	public DateTime getModified() {
		return modified;
	}

	public int getSize() {
		return size;
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}
	
	public MetaData getMetadata() {
		return metaData;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(directory)
		.append(filename)
		.append(modified)
		.append(size)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		File f = (File) o;
		return new EqualsBuilder()
		.append(directory, f.directory)
		.append(filename, f.filename)
		.append(modified, f.modified)
		.append(size, f.size)
		.isEquals();
	}
	
	@Override
	public String toString() {
		return filename;
	}

}