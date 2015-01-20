package com.growthbeat.analytics;

import java.io.IOException;
import java.util.Random;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Random random = new Random();
		int userId = random.nextInt(100);
		GrowthAnalytics.getInstance().setUserId(String.valueOf(userId));
		this.sendAdvertisingId();

		findViewById(R.id.tag).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String tag = ((Button) v).getText().toString();
				GrowthAnalytics.getInstance().setTag("clicked", tag);
			}
		});

	}

	private void sendAdvertisingId() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				int count = 10;
				Info adInfo = null;
				try {
					adInfo = AdvertisingIdClient.getAdvertisingIdInfo(MainActivity.this.getApplicationContext());
					while (adInfo.getId() == null) {
						if (adInfo.getId() != null)
							break;
						if (count < 0)
							return;
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
						count--;
					}

					if (adInfo.getId() != null && !adInfo.isLimitAdTrackingEnabled())
						GrowthAnalytics.getInstance().setAdvertisingId(adInfo.getId());

				} catch (IllegalStateException e) {
				} catch (GooglePlayServicesRepairableException e) {
				} catch (IOException e) {
				} catch (GooglePlayServicesNotAvailableException e) {
				}
			}
		}).start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onUserLeaveHint() {
		GrowthAnalytics.getInstance().close();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			GrowthAnalytics.getInstance().close();
			finish();
			return true;
		}
		return false;
	}

}
