package com.growthbeat.analytics;

import java.util.Map;

public abstract class EventHandler {

	abstract void callback(String eventId, Map<String, String> properties);

}
