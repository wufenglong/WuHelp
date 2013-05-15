package com.otheri.assistant.remote;

import java.io.IOException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.otheri.assistant.MainActivity;
import com.otheri.assistant.R;
import com.otheri.comm4and.consts.TextBasedSmsColumns;

public class AssistantService {

	private static final String TAG = "AssistantService";
	// private static final String WEBROOT = "/sdcard/assistant";
	// private static final String WEBROOT = "/sdcard/ass";
	private static final String WEBROOT = Environment
			.getExternalStorageDirectory().getName() + "/ass";

	private NotificationManager notificationManager;
	private WifiManager wifiManager;
	private WifiLock wifiLock;
	private MulticastLock multicastLock;

	private WifiReceiver wifiReceiver;
	private MessageReceiver messageReceiver;

	private HttpServer httpServer;

	private Context context;

	public AssistantService(Context context) {
		this.context = context;
	}

	public void onCreate() {
		notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		wifiReceiver = new WifiReceiver();
		wifiReceiver.registe(context);

		messageReceiver = new MessageReceiver();
		messageReceiver.registe(context);

		restartAndLockWifi();

		httpServer = new HttpServer(context, WEBROOT, 10001);

	}

	public void onStart(Intent intent, int startId) {
		showNotification();

		try {
			httpServer.start();
			Log.e(TAG, "------------------------");
			Log.e(TAG, "Listening on port 8088");
			Log.e(TAG, "------------------------");
		} catch (IOException ioe) {
			Log.e(TAG, "------------------------");
			Log.e(TAG, "Couldn't start server: " + ioe);
			Log.e(TAG, "------------------------");
		}
	}

	public void onDestroy() {
		hideNotification();

		releaseWifiLock();

		if (httpServer != null) {
			httpServer.stop();
		}

		if (wifiReceiver != null) {
			wifiReceiver.unregiste(context);
		}
		if (messageReceiver != null) {
			messageReceiver.unregiste(context);
		}
	}

	private void restartAndLockWifi() {
		wifiManager.reassociate();

		wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL,
				getClass().getSimpleName());
		wifiLock.acquire();

		multicastLock = wifiManager.createMulticastLock(getClass()
				.getSimpleName());
		multicastLock.acquire();
	}

	private void releaseWifiLock() {
		if (wifiLock.isHeld()) {
			wifiLock.release();
		}
		if (multicastLock.isHeld()) {
			multicastLock.release();
		}
	}

	private void showNotification() {
		String strNotification = context.getResources().getString(
				R.string.notification);
		Notification notification = new Notification(R.drawable.icon,
				strNotification, System.currentTimeMillis());

		Intent pIntent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				pIntent, 0);
		notification.setLatestEventInfo(context, strNotification, "",
				pendingIntent);
		notificationManager.notify(0, notification);
	}

	private void hideNotification() {
		notificationManager.cancelAll();
	}

	public class WifiReceiver extends BroadcastReceiver {

		private static final String TAG = "WifiReceiver";

		public void registe(Context context) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			context.registerReceiver(this, intentFilter);
		}

		public void unregiste(Context context) {
			context.unregisterReceiver(this);
		}

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				Bundle bundle = intent.getExtras();
				int prevState = bundle
						.getInt(WifiManager.EXTRA_PREVIOUS_WIFI_STATE);
				int newState = bundle.getInt(WifiManager.EXTRA_NEW_STATE);

				Log.e(TAG, "old>" + prevState + " -> " + "new>" + newState);
			}
		}
	};

	public class MessageReceiver extends BroadcastReceiver {

		private static final String TAG = "MessageReceiver";

		public static final String ACTION_SMS_SENDED = "com.otheri.assistant.SMS_SENDED";

		public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

		public void registe(Context context) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ACTION_SMS_SENDED);
			intentFilter.addAction(ACTION_SMS_RECEIVED);
			context.registerReceiver(this, intentFilter);
		}

		public void unregiste(Context context) {
			context.unregisterReceiver(this);
		}

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();

			if (action.equals(ACTION_SMS_SENDED)) {
				String num = bundle.getString("num");
				String content = bundle.getString("content");
				Log.e(TAG, "send message: " + num + " & " + content);

				if (this.getResultCode() == Activity.RESULT_OK) {
					ContentValues sms = new ContentValues();
					sms.put(TextBasedSmsColumns.ADDRESS, num);
					sms.put(TextBasedSmsColumns.BODY, content);
					sms.put(TextBasedSmsColumns.DATE,
							System.currentTimeMillis());
					sms.put(TextBasedSmsColumns.READ, "1");
					sms.put(TextBasedSmsColumns.TYPE,
							TextBasedSmsColumns.MESSAGE_TYPE_SENT);
					Uri uri = context.getContentResolver().insert(
							Uri.parse("content://sms"), sms);
				} else {
					ContentValues sms = new ContentValues();
					sms.put(TextBasedSmsColumns.ADDRESS, num);
					sms.put(TextBasedSmsColumns.BODY, content);
					sms.put(TextBasedSmsColumns.DATE,
							System.currentTimeMillis());
					sms.put(TextBasedSmsColumns.READ, "1");
					sms.put(TextBasedSmsColumns.TYPE,
							TextBasedSmsColumns.MESSAGE_TYPE_FAILED);
					Uri uri = context.getContentResolver().insert(
							Uri.parse("content://sms"), sms);
				}
			} else if (action.equals(ACTION_SMS_RECEIVED)) {

			}
		}
	}
}
