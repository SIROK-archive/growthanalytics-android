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
import android.widget.Button;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;

public class MainActivity extends Activity {

	private SharedPreferences sharedPreferences = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		GrowthAnalytics.getInstance().initialize(getApplicationContext(), "OvN0YOGiqdxYWeBf", "tGPhKbZTDNkJifBmP9CxyOO33wON5Weo");

		sharedPreferences = getSharedPreferences("GrowthAnalyticsSample", Context.MODE_PRIVATE);

		findViewById(R.id.tag).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String tag = ((Button) v).getText().toString();
				GrowthAnalytics.getInstance().setTag("clicked", tag);
			}
		});

	}

	@Override
	public void onStart() {

		super.onStart();

		GrowthAnalytics.getInstance().open();
		GrowthAnalytics.getInstance().setDeviceTags();

		String userId = sharedPreferences.getString("userId", UUID.randomUUID().toString());
		GrowthAnalytics.getInstance().setUserId(userId);

		sendAdvertisingId();

	}

	@Override
	public void onStop() {
		super.onStop();
		GrowthAnalytics.getInstance().close();
	}

	private void sendAdvertisingId() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this.getApplicationContext());
					if (adInfo.getId() == null || !adInfo.isLimitAdTrackingEnabled())
						return;
					GrowthAnalytics.getInstance().setAdvertisingId(adInfo.getId());
				} catch (Exception e) {
				}
			}
		}).start();

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
