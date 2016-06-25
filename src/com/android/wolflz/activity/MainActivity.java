package com.android.wolflz.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.android.wolflz.R;
import com.android.wolflz.view.RefreshScrollView;
import com.android.wolflz.view.RefreshScrollView.OnRefreshListener;

public class MainActivity extends Activity {

	private RefreshScrollView refreshScrollView;
	private Context mContext;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			refreshScrollView.onRefreshComplete();
			Toast.makeText(mContext, R.string.app_name, Toast.LENGTH_SHORT).show();;
		};
	};

	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		refreshScrollView = (RefreshScrollView) findViewById(R.id.refreshScrollView);

		refreshScrollView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				Timer timer = new Timer();
				TimerTask task = new TimerTask() {
					@Override
					public void run() {
						handler.sendEmptyMessage(0);
					}
				};
				timer.schedule(task, 3000);
			}
		});
	}
}
