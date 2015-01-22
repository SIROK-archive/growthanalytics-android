package com.growthbeat.analytics;

import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	private SharedPreferences sharedPreferences = null;

	private static final String applicationId = "OyVa3zboPjHVjsDC";
	private static final String credentialId = "3EKydeJ0imxJ5WqS22FJfdVamFLgu7XA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		GrowthAnalytics.getInstance().initialize(getApplicationContext(), applicationId, credentialId);

		sharedPreferences = getSharedPreferences("GrowthAnalyticsSample", Context.MODE_PRIVATE);

		findViewById(R.id.tag).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String product = "item";
				GrowthAnalytics.getInstance().tag(String.format("Tag:%s:Custom:Click", applicationId), product);
				GrowthAnalytics.getInstance().purchase(100, "item", product);
			}
		});

	}

	@Override
	public void onStart() {

		super.onStart();

		GrowthAnalytics.getInstance().open();
		GrowthAnalytics.getInstance().setBasicTags();

		String userId = sharedPreferences.getString("userId", UUID.randomUUID().toString());
		GrowthAnalytics.getInstance().setUserId(userId);
		GrowthAnalytics.getInstance().setAdvertisingId();

	}

	@Override
	public void onStop() {
		super.onStop();
		GrowthAnalytics.getInstance().close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
