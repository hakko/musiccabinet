package com.github.hakko.musiccabinet.domain.model.aggr;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class LibraryStatistics {

    private final int artistCount;
    private final int albumCount;
    private final int trackCount;
    private final long totalLengthInBytes;
    private final long totalLengthInSeconds;
    
    public LibraryStatistics(int artistCount, int albumCount, int trackCount, 
    		long totalLengthInBytes, long totalLengthInSeconds) {
        this.artistCount = artistCount;
        this.albumCount = albumCount;
        this.trackCount = trackCount;
        this.totalLengthInBytes = totalLengthInBytes;
        this.totalLengthInSeconds = totalLengthInSeconds;
    }

    public int getArtistCount() {
        return artistCount;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public int getTrackCount() {
        return trackCount;
    }

    public long getTotalLengthInBytes() {
        return totalLengthInBytes;
    }

	public long getTotalLengthInSeconds() {
		return totalLengthInSeconds;
	}

	public long getTotalLengthInMinutes() {
		return totalLengthInSeconds / 60;
	}

	public long getTotalLengthInHours() {
		return totalLengthInSeconds / 3600;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
        .append(artistCount)
        .append(albumCount)
        .append(trackCount)
        .append(totalLengthInBytes)
        .append(totalLengthInSeconds)
        .hashCode();
	}

	@Override
	public boolean equals(Object o) {
		  if (o == null) return false;
		  if (o == this) return true;
		  if (o.getClass() != getClass()) return false;

		  LibraryStatistics ls = (LibraryStatistics) o;
          return new EqualsBuilder()
          .append(artistCount, ls.artistCount)
          .append(albumCount, ls.albumCount)
          .append(trackCount, ls.trackCount)
          .append(totalLengthInBytes, ls.totalLengthInBytes)
          .append(totalLengthInSeconds, ls.totalLengthInSeconds)
          .isEquals();
	}

	@Override
	public String toString() {
		return artistCount + ", " + albumCount + ", " + trackCount
				+ ", " + totalLengthInBytes + ", " + totalLengthInSeconds;
	}
}