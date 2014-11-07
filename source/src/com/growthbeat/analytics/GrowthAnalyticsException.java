package com.growthbeat.analytics;

import com.growthbeat.GrowthbeatException;

public class GrowthAnalyticsException extends GrowthbeatException {

	private static final long serialVersionUID = 1L;

	public GrowthAnalyticsException() {
		super();
	}

	public GrowthAnalyticsException(String message) {
		super(message);
	}

	public GrowthAnalyticsException(Throwable throwable) {
		super(throwable);
	}

	public GrowthAnalyticsException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
