package com.growthbeat.analytics.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.growthbeat.analytics.GrowthAnalytics;
import com.growthbeat.model.Model;
import com.growthbeat.utils.DateUtils;
import com.growthbeat.utils.JSONObjectUtils;

public class ClientTag extends Model {

	private String id;

	private String clientId;

	private String tagId;

	private String value;

	private Date created;

	public ClientTag() {
	}

	public ClientTag(JSONObject jsonObject) {
		super();
		setJsonObject(jsonObject);
	}

	public static ClientTag create(String clientId, String tagId, String value) {

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("clientId", clientId);
		params.put("tagId", tagId);
		params.put("value", value);
		JSONObject jsonObject = GrowthAnalytics.getInstance().getHttpClient().post("1/client_tags", params);

		return new ClientTag(jsonObject);

	}

	public static void save(ClientTag clientTag) {

		JSONObject loadJson = GrowthAnalytics.getInstance().getPreference().get("tags");
		if (loadJson == null)
			loadJson = new JSONObject();

		JSONObject jsonObject = clientTag.getJsonObject();
		try {
			loadJson.put(clientTag.getTagId(), jsonObject);
		} catch (JSONException e) {
		}

		GrowthAnalytics.getInstance().getPreference().save("tags", loadJson);

	}

	public static ClientTag load(String tagId) {

		JSONObject loadJson = GrowthAnalytics.getInstance().getPreference().get("tags");
		if (loadJson == null)
			return new ClientTag();

		JSONObject json = null;
		try {
			json = loadJson.getJSONObject(tagId);
		} catch (JSONException e) {
			return null;
		}

		return new ClientTag(json);

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public JSONObject getJsonObject() {

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", getId());
			jsonObject.put("clientId", clientId);
			jsonObject.put("tagId", tagId);
			jsonObject.put("value", value);
			jsonObject.put("created", DateUtils.formatToDateTimeString(getCreated()));
		} catch (JSONException e) {
			return null;
		}

		return jsonObject;

	}

	@Override
	public void setJsonObject(JSONObject jsonObject) {

		if (jsonObject == null)
			return;

		try {
			if (JSONObjectUtils.hasAndIsNotNull(jsonObject, "id"))
				setId(jsonObject.getString("id"));
			if (JSONObjectUtils.hasAndIsNotNull(jsonObject, "clientId"))
				setClientId(jsonObject.getString("clientId"));
			if (JSONObjectUtils.hasAndIsNotNull(jsonObject, "tagId"))
				setTagId(jsonObject.getString("tagId"));
			if (JSONObjectUtils.hasAndIsNotNull(jsonObject, "value"))
				setValue(jsonObject.getString("value"));
			if (JSONObjectUtils.hasAndIsNotNull(jsonObject, "created"))
				setCreated(DateUtils.parseFromDateTimeString(jsonObject.getString("created")));
		} catch (JSONException e) {
			throw new IllegalArgumentException("Failed to parse JSON.");
		}

	}
}
