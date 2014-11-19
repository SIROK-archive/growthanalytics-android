package com.growthbeat.analytics;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.growthbeat.CatchableThread;
import com.growthbeat.GrowthbeatCore;
import com.growthbeat.Logger;
import com.growthbeat.Preference;
import com.growthbeat.analytics.model.ClientEvent;
import com.growthbeat.analytics.model.ClientTag;
import com.growthbeat.analytics.options.TrackEventOption;
import com.growthbeat.http.GrowthbeatHttpClient;

public class GrowthAnalytics {

	public static final String LOGGER_DEFAULT_TAG = "GrowthAnalytics";
	public static final String HTTP_CLIENT_DEFAULT_BASE_URL = "https://api.analytics.growthpush.com/";
	public static final String PREFERENCE_DEFAULT_FILE_NAME = "growthanalytics-preferences";

	private static final GrowthAnalytics instance = new GrowthAnalytics();
	private final Logger logger = new Logger(LOGGER_DEFAULT_TAG);
	private final GrowthbeatHttpClient httpClient = new GrowthbeatHttpClient(HTTP_CLIENT_DEFAULT_BASE_URL);
	private final Preference preference = new Preference(PREFERENCE_DEFAULT_FILE_NAME);

	private String applicationId;
	private String credentialId;

	private GrowthAnalytics() {
		super();
	}

	public static GrowthAnalytics getInstance() {
		return instance;
	}

	public void initialize(final Context context, final String applicationId, final String credentialId) {

		GrowthbeatCore.getInstance().initialize(context, applicationId, credentialId);

		this.applicationId = applicationId;
		this.credentialId = credentialId;
		this.preference.setContext(GrowthbeatCore.getInstance().getContext());

	}

	public void trackEvent(final String eventId) {
		this.trackEvent(eventId, new HashMap<String, String>(), TrackEventOption.DEFAULT);
	}

	public void trackEventOnce(final String eventId, final Map<String, String> properties) {
		this.trackEvent(eventId, properties, TrackEventOption.ONCE);
	}

	public void trackEvent(final String eventId, final Map<String, String> properties, final int option) {

		this.logger.info(String.format("Track event... (eventId: %s, properties: %s)", eventId, properties));

		new Thread(new Runnable() {
			@Override
			public void run() {

				ClientEvent referencedClientEvent = ClientEvent.load(eventId);
				if ((option & TrackEventOption.ONCE) != 0 && referencedClientEvent != null) {
					GrowthAnalytics.this.logger.info(String.format("This event send only once. (eventId: %s)", eventId));
					return;
				}

				if (referencedClientEvent == null && (option & TrackEventOption.MARK_FIRST_TIME) != 0)
					properties.put("first_time", null);

				try {

					ClientEvent clientEvent = ClientEvent.create(GrowthbeatCore.getInstance().waitClient().getId(), eventId, properties);
					if ((referencedClientEvent == null && (option & TrackEventOption.MARK_FIRST_TIME) != 0)
							|| (option & TrackEventOption.ONCE) != 0) {
						ClientEvent.save(clientEvent);
						GrowthAnalytics.this.logger.info("save event .");
					}

					GrowthAnalytics.this.logger.info(String.format("Tracking event success. (clientEventId: %s)", clientEvent.getId()));
				} catch (GrowthAnalyticsException e) {
					GrowthAnalytics.this.logger.info(String.format("Tracking event fail. %s", e.getMessage()));
				}
			}
		}).start();

	}

	public void setTag(final String tagId, final String value) {

		this.logger.info(String.format("Set tag... (tagId: %s, value: %s)", tagId, value));

		ClientTag clientTag = ClientTag.load(tagId);
		if (clientTag != null && (value != null && value.equals(clientTag.getValue()))) {
			this.logger.info(String.format("Already exists tag... (tagId: %s, value: %s)", tagId, value));
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ClientTag clientTag = ClientTag.create(GrowthbeatCore.getInstance().waitClient().getId(), tagId, value);
					GrowthAnalytics.this.logger.info(String.format("Setting tag success. (clientTagId: %s)", clientTag.getId()));
					ClientTag.save(clientTag);
				} catch (GrowthAnalyticsException e) {
					GrowthAnalytics.this.logger.info(String.format("Setting tag fail. %s", e.getMessage()));
				}
			}
		}).start();

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

	public GrowthbeatHttpClient getHttpClient() {
		return httpClient;
	}

	public Preference getPreference() {
		return preference;
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
