package com.growthbeat.analytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.os.Handler;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.growthbeat.CatchableThread;
import com.growthbeat.GrowthbeatCore;
import com.growthbeat.GrowthbeatException;
import com.growthbeat.Logger;
import com.growthbeat.Preference;
import com.growthbeat.analytics.model.ClientEvent;
import com.growthbeat.analytics.model.ClientTag;
import com.growthbeat.http.GrowthbeatHttpClient;
import com.growthbeat.utils.AppUtils;
import com.growthbeat.utils.DeviceUtils;

public class GrowthAnalytics {

	public static final String LOGGER_DEFAULT_TAG = "GrowthAnalytics";
	public static final String HTTP_CLIENT_DEFAULT_BASE_URL = "https://api.analytics.growthbeat.com/";
	public static final String PREFERENCE_DEFAULT_FILE_NAME = "growthanalytics-preferences";

	private static final String DEFAULT = "Default";
	private static final String CUSTOM = "Custom";

	private static final GrowthAnalytics instance = new GrowthAnalytics();
	private final Logger logger = new Logger(LOGGER_DEFAULT_TAG);
	private final GrowthbeatHttpClient httpClient = new GrowthbeatHttpClient(HTTP_CLIENT_DEFAULT_BASE_URL);
	private final Preference preference = new Preference(PREFERENCE_DEFAULT_FILE_NAME);

	private String applicationId = null;
	private String credentialId = null;

	private boolean initialized = false;
	private Date openDate = null;
	private List<EventHandler> eventHandlers = new ArrayList<EventHandler>();

	private GrowthAnalytics() {
		super();
	}

	public static GrowthAnalytics getInstance() {
		return instance;
	}

	public void initialize(final Context context, final String applicationId, final String credentialId) {

		if (initialized)
			return;
		initialized = true;

		if (context == null) {
			logger.warning("The context parameter cannot be null.");
			return;
		}

		this.applicationId = applicationId;
		this.credentialId = credentialId;

		GrowthbeatCore.getInstance().initialize(context, applicationId, credentialId);
		this.preference.setContext(GrowthbeatCore.getInstance().getContext());

		setBasicTags();

	}

	public void track(final String namespace, final String eventId) {
		track(namespace, eventId, null, null);
	}

	public void track(final String namespace, final String eventId, final Map<String, String> properties) {
		track(namespace, eventId, properties, null);
	}

	public void track(final String namespace, final String eventId, final TrackOption option) {
		track(namespace, eventId, null, option);
	}

	public void track(final String namespace, final String eventId, final Map<String, String> properties, final TrackOption option) {

		final String fullId = generateEventId(namespace, eventId);

		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {

				logger.info(String.format("Track event... (eventId: %s)", fullId));

				final Map<String, String> processedProperties = (properties != null) ? properties : new HashMap<String, String>();

				ClientEvent existingClientEvent = ClientEvent.load(fullId);

				if (option == TrackOption.ONCE) {
					if (existingClientEvent != null) {
						logger.info(String.format("Event already sent with once option. (eventId: %s)", fullId));
						return;
					}
				}

				if (option == TrackOption.COUNTER) {
					int counter = 0;
					if (existingClientEvent != null && existingClientEvent.getProperties() != null) {
						try {
							counter = Integer.valueOf(existingClientEvent.getProperties().get("counter"));
						} catch (NumberFormatException e) {
						}
					}
					processedProperties.put("counter", String.valueOf(counter + 1));
				}

				try {
					ClientEvent createdClientEvent = ClientEvent.create(GrowthbeatCore.getInstance().waitClient().getId(), fullId,
							processedProperties, credentialId);
					if (createdClientEvent != null) {
						ClientEvent.save(createdClientEvent);
						logger.info(String.format("Tracking event success. (id: %s, eventId: %s, properties: %s)",
								createdClientEvent.getId(), fullId, processedProperties));
					} else {
						logger.warning("Created client_event is null.");
					}
				} catch (GrowthbeatException e) {
					logger.info(String.format("Tracking event fail. %s", e.getMessage()));
				}

				handler.post(new Runnable() {
					@Override
					public void run() {
						for (EventHandler eventHandler : eventHandlers) {
							eventHandler.callback(fullId, processedProperties);
						}
					}
				});

			}
		}).start();

	}

	public void track(final String lastId) {
		track(CUSTOM, lastId);
	}

	public void track(final String lastId, final Map<String, String> properties) {
		track(CUSTOM, lastId, properties);
	}

	public void track(final String lastId, final TrackOption option) {
		track(CUSTOM, lastId, option);
	}

	public void track(final String lastId, final Map<String, String> properties, final TrackOption option) {
		track(CUSTOM, lastId, properties, option);
	}

	public void addEventHandler(EventHandler eventHandler) {
		eventHandlers.add(eventHandler);
	}

	public void tag(final String namespace, final String tagId) {
		tag(namespace, tagId, null);
	}

	public void tag(final String namespace, final String tagId, final String value) {

		final String fullId = generateTagId(namespace, tagId);
		new Thread(new Runnable() {
			@Override
			public void run() {

				logger.info(String.format("Set tag... (tagId: %s, value: %s)", fullId, value));

				ClientTag existingClientTag = ClientTag.load(fullId);

				if (existingClientTag != null) {
					if (value == existingClientTag.getValue() || (value != null && value.equals(existingClientTag.getValue()))) {
						logger.info(String.format("Tag exists with the same value. (tagId: %s, value: %s)", fullId, value));
						return;
					}
					logger.info(String.format("Tag exists with the other value. (tagId: %s, value: %s)", fullId, value));
				}

				try {
					ClientTag createdClientTag = ClientTag.create(GrowthbeatCore.getInstance().waitClient().getId(), fullId, value,
							credentialId);
					if (createdClientTag != null) {
						ClientTag.save(createdClientTag);
						logger.info(String.format("Setting tag success. (tagId: %s)", fullId));
					} else {
						logger.warning("Created client_tag is null.");
					}
				} catch (GrowthbeatException e) {
					logger.info(String.format("Setting tag fail. %s", e.getMessage()));
				}

			}
		}).start();

	}

	public void tag(String lastId) {
		tag(CUSTOM, lastId);
	}

	public void tag(String lastId, String value) {
		tag(CUSTOM, lastId, value);
	}

	public void open() {
		openDate = new Date();
		track(DEFAULT, "Open", TrackOption.COUNTER);
		track(DEFAULT, "Install", TrackOption.ONCE);
	}

	public void close() {
		if (openDate == null)
			return;
		long time = (new Date().getTime() - openDate.getTime()) / 1000;
		openDate = null;
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("time", String.valueOf(time));
		track(DEFAULT, "Close", properties);
	}

	public void purchase(int price, String category, String product) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("price", String.valueOf(price));
		properties.put("category", category);
		properties.put("product", product);
		track(DEFAULT, "Purchase", properties);
	}

	public void setUserId(String userId) {
		tag(DEFAULT, "UserID", userId);
	}

	public void setName(String name) {
		tag(DEFAULT, "Name", name);
	}

	public void setAge(int age) {
		tag(DEFAULT, "Age", String.valueOf(age));
	}

	public void setGender(Gender gender) {
		tag(DEFAULT, "Gender", gender.getValue());
	}

	public void setLevel(int level) {
		tag(DEFAULT, "Level", String.valueOf(level));
	}

	public void setDevelopment(boolean development) {
		tag(DEFAULT, "Development", String.valueOf(development));
	}

	public void setDeviceModel() {
		tag(DEFAULT, "DeviceModel", DeviceUtils.getModel());
	}

	public void setOS() {
		tag(DEFAULT, "OS", "Android " + DeviceUtils.getOsVersion());
	}

	public void setLanguage() {
		tag(DEFAULT, "Language", DeviceUtils.getLanguage());
	}

	public void setTimeZone() {
		tag(DEFAULT, "TimeZone", DeviceUtils.getTimeZone());
	}

	public void setTimeZoneOffset() {
		tag(DEFAULT, "TimeZoneOffset", String.valueOf(DeviceUtils.getTimeZoneOffset()));
	}

	public void setAppVersion() {
		tag(DEFAULT, "AppVersion", AppUtils.getaAppVersion(GrowthbeatCore.getInstance().getContext()));
	}

	public void setRandom() {
		tag(DEFAULT, "Random", String.valueOf(new Random().nextDouble()));
	}

	public void setAdvertisingId() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(GrowthbeatCore.getInstance().getContext());
					if (adInfo.getId() == null || !adInfo.isLimitAdTrackingEnabled())
						return;
					tag(DEFAULT, "AdvertisingID", adInfo.getId());
				} catch (Exception e) {
				}
			}
		}).start();
	}

	public void setBasicTags() {
		setDeviceModel();
		setOS();
		setLanguage();
		setTimeZone();
		setTimeZoneOffset();
		setAppVersion();
		setAdvertisingId();
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

	private String generateEventId(String namespace, String name) {
		return String.format("Event:%s:%s:%s", namespace, applicationId, name);
	}

	private String generateTagId(final String namespace, String name) {
		return String.format("Tag:%s:%s:%s", namespace, applicationId, name);
	}

	public static enum TrackOption {
		ONCE, COUNTER;
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
