package com.otheri.assistant;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";

	private TextView textview;
	private Button btn_exit;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textview = (TextView) this.findViewById(R.id.textview);
		btn_exit = (Button) this.findViewById(R.id.btn_exit);
		btn_exit.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this, MainService.class);
				stopService(intent);
				Log.e(TAG, "stopService(intent);");
				finish();
			}
		});

		ArrayList<String> ipList = new ArrayList<String>();
		try {
			StringBuilder sb = new StringBuilder();
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();

				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();

					// if (!inetAddress.isLoopbackAddress()) {
					ipList.add(inetAddress.getHostAddress().toString());
					// }
					sb.append(inetAddress.getHostAddress().toString()).append(
							"\n");
				}
			}

			textview.setText(sb.toString());
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
		}

	}

	public void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MainService.class);
		startService(intent);
		Log.e(TAG, "startService(intent);");
	}
}