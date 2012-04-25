package com.github.hakko.musiccabinet.domain.model.library;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import com.github.hakko.musiccabinet.domain.model.music.Track;

public class MusicFile {

	private Track track;
	private String path;
	private DateTime created;
	private DateTime lastModified;
	private String externalId;
	
	public MusicFile(String artistName, String trackName, String path,
			long created, long lastModified) {
		this.track = new Track(artistName, trackName);
		this.path = path;
		this.created = new DateTime(created);
		this.lastModified = new DateTime(lastModified);
	}
	
	public MusicFile(String externalId) {
		this.externalId = externalId;
		this.track = new Track();
	}

	public Track getTrack() {
		return track;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public DateTime getCreated() {
		return created;
	}

	public DateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(DateTime lastModified) {
		this.lastModified = lastModified;
	}
	
	public String getExternalId() {
		return externalId;
	}
	
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(track)
		.append(path)
		.append(created)
		.append(lastModified)
		.append(externalId)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  MusicFile mf = (MusicFile) o;
          return new EqualsBuilder()
          .append(track, mf.track)
          .append(path, mf.path)
          .append(created, mf.created)
          .append(lastModified, mf.lastModified)
          .append(externalId, mf.externalId)
          .isEquals();
	}

	@Override
	public String toString() {
		return track + " at " + path + ", created " 
		+ created + ", modified " + lastModified;
	}
	
}