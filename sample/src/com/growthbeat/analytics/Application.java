package com.growthbeat.analytics;

import java.util.HashMap;

public class Application extends android.app.Application {

	@Override
	public void onCreate() {

		GrowthAnalytics.getInstance().initialize(getApplicationContext(), "OvN0YOGiqdxYWeBf", "tGPhKbZTDNkJifBmP9CxyOO33wON5Weo");
		GrowthAnalytics.getInstance().trackEvent("install", new HashMap<String, String>(), true);

	}

}
