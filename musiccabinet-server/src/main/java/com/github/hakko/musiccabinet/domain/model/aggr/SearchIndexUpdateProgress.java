package com.github.hakko.musiccabinet.domain.model.aggr;

/*
 * Represents progress state for a service updating search index.
 */
public class SearchIndexUpdateProgress {

	private int totalOperations;
	private int finishedOperations;
	private String updateDescription;
	
	public static final int NOT_INITIALIZED = -1;
	
	public SearchIndexUpdateProgress(String updateDescription) {
		this.updateDescription = updateDescription;
		totalOperations = NOT_INITIALIZED;
		finishedOperations = NOT_INITIALIZED;
	}
	
	/*
	 * Returns number of total operations, 
	 * or -1 [NOT_INITIALIZED] if not initialized.
	 */
	public int getTotalOperations() {
		return totalOperations;
	}
	
	public void setTotalOperations(int totalOperations) {
		this.totalOperations = totalOperations;
	}
	
	/*
	 * Returns number of finished operations,
	 * or -1 [NOT_INITIALIZED] if not initialized.
	 */
	public int getFinishedOperations() {
		return finishedOperations;
	}
	
	public void setFinishedOperations(int finishedOperations) {
		this.finishedOperations = finishedOperations;
	}
	
	public void addFinishedOperation() {
		if (finishedOperations == NOT_INITIALIZED) {
			finishedOperations = 0;
		}
		++finishedOperations;
	}

	public String getUpdateDescription() {
		return updateDescription;
	}
	
	public void setUpdateDescription(String updateDescription) {
		this.updateDescription = updateDescription;
	}
	
	public void reset() {
		finishedOperations = NOT_INITIALIZED;
		totalOperations = NOT_INITIALIZED;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (finishedOperations == NOT_INITIALIZED && totalOperations == NOT_INITIALIZED) {
			sb.append(updateDescription + " [not started]");
		} else {
			if (finishedOperations == NOT_INITIALIZED) {
				sb.append("-");
			} else {
				sb.append(finishedOperations);
			}
			if (totalOperations != NOT_INITIALIZED) {
				sb.append("/" + totalOperations);
			}
			sb.append(" " + updateDescription);
		}
		return sb.toString();
	}
}