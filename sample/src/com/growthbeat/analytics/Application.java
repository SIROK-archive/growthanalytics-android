package com.growthbeat.analytics;

public class Application extends android.app.Application {

	@Override
	public void onCreate() {

		GrowthAnalytics.getInstance().initialize(getApplicationContext(), "OvN0YOGiqdxYWeBf", "tGPhKbZTDNkJifBmP9CxyOO33wON5Weo");
		GrowthAnalytics.getInstance().open();
		GrowthAnalytics.getInstance().setDeviceTags();

	}

}
