package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.List;

public class NameSearchResult<T> {

	private final List<T> results;
	private final int offset;
	private final int totalHits;

	public NameSearchResult(List<T> results, int offset, int totalHits) {
		this.results = results;
		this.offset = offset;
		this.totalHits = totalHits;
	}

	public List<T> getResults() {
		return results;
	}

	public int getOffset() {
		return offset;
	}

	public int getTotalHits() {
		return totalHits;
	}
	
}