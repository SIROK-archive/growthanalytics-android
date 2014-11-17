package com.growthbeat.analytics;

import java.util.Map;

import android.content.Context;

import com.growthbeat.CatchableThread;
import com.growthbeat.GrowthbeatCore;
import com.growthbeat.Logger;
import com.growthbeat.analytics.model.ClientEvent;
import com.growthbeat.analytics.model.ClientTag;
import com.growthbeat.analytics.model.GrowthAnalyticsHttpClient;

public class GrowthAnalytics {

	public static final String BASE_URL = "https://api.growthanalytics.growthpush.com/";

	private static final GrowthAnalytics instance = new GrowthAnalytics();
	private final Logger logger = new Logger("Growth Analytics");
	private final GrowthAnalyticsHttpClient httpClient = new GrowthAnalyticsHttpClient();

	private Context context = null;
	private String applicationId;
	private String credentialId;

	private GrowthAnalytics() {
		super();
		httpClient.setBaseUrl(BASE_URL);
	}

	public static GrowthAnalytics getInstance() {
		return instance;
	}

	public void initialize(final Context context, final String applicationId, final String credentialId) {

		if (this.context != null)
			return;

		GrowthbeatCore.getInstance().initialize(context, applicationId, credentialId);

		this.context = context;
		this.applicationId = applicationId;
		this.credentialId = credentialId;

	}

	public void trackEvent(final String eventId, final Map<String, String> properties) {

		this.logger.info(String.format("Track event... (eventId: %s, properties: %s)", eventId, properties));

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ClientEvent clientEvent = ClientEvent.create(GrowthbeatCore.getInstance().waitClient().getId(), eventId, properties);
					GrowthAnalytics.this.logger.info(String.format("Tracking event success. (clientEventId: %s)", clientEvent.getId()));
				} catch (GrowthAnalyticsException e) {
					GrowthAnalytics.this.logger.info(String.format("Tracking event fail. %s", e.getMessage()));
				}
			}
		}).start();

	}

	public void setTag(final String tagId, final String value) {

		this.logger.info(String.format("Set tag... (eventId: %s, value: %s)", tagId, value));

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ClientTag clientTag = new ClientTag().create(GrowthbeatCore.getInstance().waitClient().getId(), tagId, value);
					GrowthAnalytics.this.logger.info(String.format("Setting tag success. (clientTagId: %s)", clientTag.getId()));
				} catch (GrowthAnalyticsException e) {
					GrowthAnalytics.this.logger.info(String.format("Setting tag fail. %s", e.getMessage()));
				}
			}
		}).start();

	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public Logger getLogger() {
		return logger;
	}

	public GrowthAnalyticsHttpClient getHttpClient() {
		return httpClient;
	}

	private static class Thread extends CatchableThread {

		public Thread(Runnable runnable) {
			super(runnable);
		}

		@Override
		public void uncaughtException(java.lang.Thread thread, Throwable e) {
			String message = "Uncaught Exception: " + e.getClass().getName();
			if (e.getMessage() != null)
				message += "; " + e.getMessage();
			GrowthAnalytics.getInstance().getLogger().warning(message);
			e.printStackTrace();
		}

	}

}
