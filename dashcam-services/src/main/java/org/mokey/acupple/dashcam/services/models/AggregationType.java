package org.mokey.acupple.dashcam.services.models;


public enum AggregationType {
	MINUTE(0), HOUR(1);

	private int value;

	AggregationType(int value) {
		this.value = value;
	}
	
	 public int getValue() {
		    return value;
		  }

	public static AggregationType findByValue(int value) {
		switch (value) {
		case 0:
			return MINUTE;
		case 1:
			return HOUR;
		default:
			return null;
		}
	}

}
