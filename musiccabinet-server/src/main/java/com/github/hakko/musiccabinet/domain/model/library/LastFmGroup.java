package com.github.hakko.musiccabinet.domain.model.library;

public class LastFmGroup {

	private int id = -1;
	private String name;
	private boolean enabled = true;
	
	public LastFmGroup(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public LastFmGroup(String name, boolean enabled) {
		this.name = name;
		this.enabled = enabled;
	}
	
	public LastFmGroup(String name) {
		this.name = name;
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
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
}