package com.github.hakko.musiccabinet.domain.model.library;

public enum Period {

	THREE_MONTHS("3month", 3*30),
	SIX_MONTHS("6month", 6*30),
	TWELVE_MONTHS("12month", 12*30),
	OVERALL("overall", -1);

	private final String description;
	private final short days;
		
	Period(String description, int days) {
		this.description = description;
		this.days = (short) days;
	}

	public String getDescription() {
		return description;
	}

	public short getDays() {
		return days;
	}
	
}