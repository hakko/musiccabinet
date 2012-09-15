package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.List;

public class NameSearchResult<T> {

	private final List<T> results;
	private final int offset;

	public NameSearchResult(List<T> results, int offset) {
		this.results = results;
		this.offset = offset;
	}

	public List<T> getResults() {
		return results;
	}

	public int getOffset() {
		return offset;
	}
	
}