package com.otheri.assistant.remote;

import com.otheri.comm4and.api.Environment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class AssistantReceiver extends BroadcastReceiver {

	private static final String TAG = "AssistantReceiver";

	public void onReceive(Context context, Intent intent) {

		Log.e(TAG, "onReceive:" + intent.getAction());
		String action = intent.getAction();
		if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle bundle = intent.getExtras();

			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				SmsMessage[] messages = new SmsMessage[pdus.length];

				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}
				Environment._receiveSMSMessage(messages);
			}
		}
	}

}