package com.growthbeat.analytics;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Context;

import com.growthbeat.CatchableThread;
import com.growthbeat.GrowthbeatCore;
import com.growthbeat.Logger;
import com.growthbeat.Preference;
import com.growthbeat.analytics.model.ClientEvent;
import com.growthbeat.analytics.model.ClientTag;
import com.growthbeat.analytics.options.TrackOption;
import com.growthbeat.http.GrowthbeatHttpClient;
import com.growthbeat.utils.AppUtils;
import com.growthbeat.utils.DeviceUtils;

public class GrowthAnalytics {

	public static final String LOGGER_DEFAULT_TAG = "GrowthAnalytics";
	public static final String HTTP_CLIENT_DEFAULT_BASE_URL = "https://api.analytics.growthbeat.com/";
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

	public void track(final String openEventId) {
		track(openEventId, null, null);
	}

	public void track(final String openEventId, final Map<String, String> properties) {
		track(openEventId, properties, null);
	}

	public void track(final String openEventId, final TrackOption option) {
		track(openEventId, null, option);
	}

	public void track(final String eventId, final Map<String, String> properties, final TrackOption option) {

		this.logger.info(String.format("Track event... (eventId: %s, properties: %s)", eventId, properties));

		new Thread(new Runnable() {
			@Override
			public void run() {

				ClientEvent referencedClientEvent = ClientEvent.load(eventId);
				if ((option == TrackOption.ONCE) && referencedClientEvent != null) {
					GrowthAnalytics.this.logger.info(String.format("This event send only once. (eventId: %s)", eventId));
					return;
				}

				if (referencedClientEvent == null && (option == TrackOption.COUNTER))
					properties.put("first_time", null);

				try {

					ClientEvent clientEvent = ClientEvent.create(GrowthbeatCore.getInstance().waitClient().getId(), eventId, properties);
					ClientEvent.save(clientEvent);
					GrowthAnalytics.this.logger.info("save event .");

					GrowthAnalytics.this.logger.info(String.format("Tracking event success. (id: %s)", clientEvent.getId()));
				} catch (GrowthAnalyticsException e) {
					GrowthAnalytics.this.logger.info(String.format("Tracking event fail. %s", e.getMessage()));
				}
			}
		}).start();

	}

	public void tag(final String tagId) {
		tag(tagId, null);
	}

	public void tag(final String tagId, final String value) {

		this.logger.info(String.format("Set tag... (tagId: %s, value: %s)", tagId, value));

		new Thread(new Runnable() {
			@Override
			public void run() {

				ClientTag referecedClientTag = ClientTag.load(tagId);
				if (referecedClientTag != null && (value != null && value.equals(referecedClientTag.getValue()))) {
					GrowthAnalytics.this.logger.info(String.format("Already exists tag... (tagId: %s, value: %s)", tagId, value));
					return;
				}

				try {
					ClientTag clientTag = ClientTag.create(GrowthbeatCore.getInstance().waitClient().getId(), tagId, value);
					GrowthAnalytics.this.logger.info("Setting tag success.");
					ClientTag.save(clientTag);
				} catch (GrowthAnalyticsException e) {
					GrowthAnalytics.this.logger.info(String.format("Setting tag fail. %s", e.getMessage()));
				}
			}
		}).start();

	}

	public void open() {
		track(generateEventId("Open"));
		track(generateEventId("Install"), TrackOption.ONCE);
	}

	public void close() {
		ClientEvent openEvent = ClientEvent.load(generateEventId("Open"));
		if (openEvent == null)
			return;
		long time = new Date().getTime() - openEvent.getCreated().getTime();
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("time", String.valueOf(time));
		track(generateEventId("Close"), properties);
	}

	public void purchase(int price, String category, String product) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("price", String.valueOf(price));
		properties.put("category", category);
		properties.put("product", product);
		track(generateEventId("Purchase"), properties);
	}

	public void setUserId(String userId) {
		tag(generateTagId("UserID"), userId);
	}

	public void setName(String name) {
		tag(generateTagId("Name"), name);
	}

	public void setAge(int age) {
		tag(generateTagId("Age"), String.valueOf(age));
	}

	public void setGender(Gender gender) {
		tag(generateTagId("Gender"), gender.getValue());
	}

	public void setLevel(int level) {
		tag(generateTagId("Level"), String.valueOf(level));
	}

	public void setAdvertisingId(String advertisingId) {
		tag(generateTagId("AdvertisingID"), advertisingId);
	}

	public void setDevelopment(boolean development) {
		tag(generateTagId("Development"), String.valueOf(development));
	}

	public void setOS() {
		tag(generateTagId("OS"), "Android " + DeviceUtils.getOsVersion());
	}

	public void setLanguage() {
		tag(generateTagId("language"), DeviceUtils.getLanguage());
	}

	public void setTimeZone() {
		tag(generateTagId("TimeZone"), DeviceUtils.getTimeZone());
	}

	public void setTimeZoneOffset() {
		tag(generateTagId("TimeZoneOffset"), DeviceUtils.getTimeZoneOffset());
	}

	public void setAppVersion() {
		tag(generateTagId("AppVersion"), AppUtils.getaAppVersion(GrowthbeatCore.getInstance().getContext()));
	}

	public void setRandom() {
		tag(generateTagId("Random"), String.valueOf(new Random().nextDouble()));
	}

	public void setDeviceTags() {
		setOS();
		setLanguage();
		setTimeZone();
		setTimeZoneOffset();
		setAppVersion();
	}

	public String getApplicationId() {
		return applicationId;
	}

	public String getCredentialId() {
		return credentialId;
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

	private String generateEventId(String name) {

		return String.format("Event:%s:Default:%s", applicationId, name);
	}

	private String generateTagId(String name) {
		return String.format("Tag:%s:Default:%s", applicationId, name);
	}

	public static enum Gender {

		MALE("male"), FEMALE("female");
		private String value = null;

		Gender(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

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
