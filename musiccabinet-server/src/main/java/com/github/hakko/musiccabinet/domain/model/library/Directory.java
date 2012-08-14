package com.github.hakko.musiccabinet.domain.model.library;

import static java.io.File.separator;
import static java.io.File.separatorChar;
import static org.apache.commons.lang.StringUtils.countMatches;

import java.util.Set;
import java.util.TreeSet;

public class Directory implements Comparable<Directory> {

	private final int id;
	private final String path;
	private final String name;
	
	private Set<File> files = new TreeSet<>();
	private Set<Directory> subDirectories = new TreeSet<>();

	public Directory(int id, String path) {
		this.id = id;
		this.path = path;
		this.name = countMatches(path, separator) <= 1 ? path : 
			path.substring(path.lastIndexOf(separatorChar) + 1);
	}

	public int getId() {
		return id;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}
	
	public Set<File> getFiles() {
		return files;
	}

	public void setFiles(Set<File> files) {
		this.files = files;
	}

	public Set<Directory> getSubDirectories() {
		return subDirectories;
	}

	public void setSubDirectories(Set<Directory> subDirectories) {
		this.subDirectories = subDirectories;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		return id == ((Directory) o).id;
	}

	@Override
	public int compareTo(Directory d) {
		return name.compareTo(d.name);
	}
	
	@Override
	public String toString() {
		return path + " [" + id + "]";
	}
	
}