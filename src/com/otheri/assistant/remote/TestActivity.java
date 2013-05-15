package com.otheri.assistant.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.otheri.assistant.remote.AssistantService.MessageReceiver;
import com.otheri.comm4and.api.Application;
import com.otheri.comm4and.api.Telephony;

public class TestActivity extends Activity {

	private static final String TAG = "TestActivity";

	private Context context;
	private PackageManager pm;
	private ContentResolver cr;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		pm = context.getPackageManager();
		cr = context.getContentResolver();

		try {
			test();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void test() throws Throwable {

		// testMessage();

		// testGetApkLabel();

		// testGetApkPermission();

		// testGetActivityLabels();

		testMmsPart();
	}

	private void testMmsPart() {
		List<String[]> ret = Telephony._getMmsPart(cr, "2");

		if (ret.size() > 0) {
			Log.e(TAG, Integer.toString(ret.size()));
			for (String[] cv : ret) {

				StringBuilder sb = new StringBuilder();
				for (String entry : cv) {
					sb.append(entry);
					sb.append(' ');
				}
				Log.e(TAG, sb.toString());
			}
		} else {
			Log.e(TAG, "00000000000");
			Log.e(TAG, "00000000000");
			Log.e(TAG, "00000000000");
		}
	}

	private void testMessage() throws Throwable {

		String strNum = "10086";
		String strContent = "ye";
		SmsManager smsManager = SmsManager.getDefault();

		Intent sendIntent = new Intent(MessageReceiver.ACTION_SMS_SENDED);
		sendIntent.putExtra("num", strNum);
		sendIntent.putExtra("content", strContent);

		PendingIntent sentPendingIntent = PendingIntent.getBroadcast(this, 0,
				sendIntent, PendingIntent.FLAG_ONE_SHOT);

		smsManager.sendTextMessage(strNum, null, strContent, sentPendingIntent,
				null);
		Toast.makeText(this, "短信发送完成", Toast.LENGTH_LONG).show();
	}

	private void testGetApkLabel() throws Throwable {

		long start = System.currentTimeMillis();

		HashMap<String, String> ret = null;

		HashMap<String, String> apks = Application._getApks(pm);

		Iterator<String> iterator = apks.keySet().iterator();
		List<String> pns = new ArrayList<String>(apks.size());
		while (iterator.hasNext()) {
			String packageName = iterator.next();
			pns.add(packageName);
		}
		ret = Application._getApkLabels(pm, pns);

		long spend = System.currentTimeMillis() - start;

		Log.e("testGetApkLabel:", "-----------------------" + spend);
		Log.e("testGetApkLabel:", "size=" + ret.size());
		Iterator<Entry<String, String>> entrys = ret.entrySet().iterator();
		while (entrys.hasNext()) {
			Entry<String, String> entry = entrys.next();
			Log.e("testGetApkLabel:", entry.getKey() + " & " + entry.getValue());
		}
	}

	private void testGetApkPermission() throws Throwable {

		long start = System.currentTimeMillis();

		List<String> pis = Application._getApkPermission(pm,
				"com.android.bluetooth");

		long spend = System.currentTimeMillis() - start;

		Log.e("testGetApkPermission:", "-----------------------" + spend);
		Log.e("testGetApkPermission:", "size=" + pis.size());
		for (String pi : pis) {
			Log.e("testGetApkPermission:", pi);
		}
	}

	private void testGetActivityLabels() throws Throwable {
		long start = System.currentTimeMillis();

		HashMap<String, String> ret = null;

		HashMap<String, String> activitys = Application._getActivitys(pm);

		ret = Application._getActivityLabels(pm, activitys);

		long spend = System.currentTimeMillis() - start;

		Log.e("testGetActivityLabels:", "-----------------------" + spend);
		Log.e("testGetActivityLabels:", "size=" + ret.size());
		Iterator<Entry<String, String>> entrys = ret.entrySet().iterator();
		while (entrys.hasNext()) {
			Entry<String, String> entry = entrys.next();
			Log.e("testGetActivityLabels:",
					entry.getKey() + " & " + entry.getValue());
		}

	}
}