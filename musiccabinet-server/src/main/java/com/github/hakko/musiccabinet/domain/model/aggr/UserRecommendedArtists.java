package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class UserRecommendedArtists {

	private LastFmUser user;
	private List<RecommendedArtist> artists;
	
	public UserRecommendedArtists(LastFmUser user, List<RecommendedArtist> artists) {
		this.user = user;
		this.artists = artists;
	}

	public LastFmUser getUser() {
		return user;
	}

	public void setUser(LastFmUser user) {
		this.user = user;
	}

	public List<RecommendedArtist> getArtists() {
		return artists;
	}

	public void setArtists(List<RecommendedArtist> artists) {
		this.artists = artists;
	}
	
	public static class RecommendedArtist {
		
		private Artist artist;
		private Artist contextArtist1;
		private Artist contextArtist2;

		public RecommendedArtist(String artistName, String contextArtist1Name, String contextArtist2Name) {
			this.artist = new Artist(artistName);
			this.contextArtist1 = new Artist(contextArtist1Name);
			this.contextArtist2 = new Artist(contextArtist2Name);
		}

		public RecommendedArtist(Artist artist) {
			this.artist = artist;
		}

		public Artist getArtist() {
			return artist;
		}

		public void setArtist(Artist artist) {
			this.artist = artist;
		}

		public Artist getContextArtist1() {
			return contextArtist1;
		}

		public void setContextArtist1(Artist contextArtist1) {
			this.contextArtist1 = contextArtist1;
		}

		public Artist getContextArtist2() {
			return contextArtist2;
		}

		public void setContextArtist2(Artist contextArtist2) {
			this.contextArtist2 = contextArtist2;
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder()
			.append(artist)
			.append(contextArtist1)
			.append(contextArtist2)
			.toHashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) return false;
			if (o == this) return true;
			if (o.getClass() != getClass()) return false;

			RecommendedArtist ra = (RecommendedArtist) o;
			return new EqualsBuilder()
			.append(artist, ra.artist)
			.append(contextArtist1, ra.contextArtist1)
			.append(contextArtist2, ra.contextArtist2)
			.isEquals();
		}

	}
	
}