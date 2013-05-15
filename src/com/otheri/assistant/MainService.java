package com.otheri.assistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.otheri.assistant.remote.AssistantService;

public class MainService extends Service {

	private static final String TAG = "MainService";

	private AssistantService assistantService;

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if (assistantService == null) {
			assistantService = new AssistantService(this);
			assistantService.onCreate();
		}

		assistantService.onStart(intent, startId);
	}

	public void onDestroy() {
		super.onDestroy();

		assistantService.onDestroy();
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

}
