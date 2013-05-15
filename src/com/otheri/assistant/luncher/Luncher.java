package com.otheri.assistant.luncher;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;

import com.otheri.assistant.R;

public class Luncher extends Activity {

	private static final String TAG = "Luncher";

	private AppWidgetHost mAppWidgetHost;
	private AppWidgetManager mAppWidgetManager;

	private static final int REQUEST_PICK_APPWIDGET = 1;
	private static final int REQUEST_CREATE_APPWIDGET = 2;
	private static final int APPWIDGET_HOST_ID = 0x100;
	private static final String EXTRA_CUSTOM_WIDGET = "custom_widget";

	private LuncherTestView layout;

	private WallpaperManager wallpaperManager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAppWidgetManager = AppWidgetManager
				.getInstance(getApplicationContext());
		mAppWidgetHost = new AppWidgetHost(getApplicationContext(),
				APPWIDGET_HOST_ID);
		// 开始监听widget的变化
		mAppWidgetHost.startListening();

		layout = new LuncherTestView(this);
		layout.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				addWidget();
				return false;
			}
		});

		setContentView(layout);

		wallpaperManager = WallpaperManager.getInstance(this);

		setDefaultWallpaper();
	}

	private boolean mWallpaperChecked = false;

	private void setDefaultWallpaper() {
		if (!mWallpaperChecked) {
			Drawable wallpaper = wallpaperManager.peekDrawable();
			if (wallpaper == null) {
				try {
					wallpaperManager.clear();

				} catch (IOException e) {
					Log.e(TAG, "Failed to clear wallpaper " + e);
				}
			} else {
				getWindow().setBackgroundDrawable(
						new ClippedDrawable(wallpaper));
			}
			mWallpaperChecked = true;
		}
	}

	private class ClippedDrawable extends Drawable {
		private final Drawable mWallpaper;

		public ClippedDrawable(Drawable wallpaper) {
			mWallpaper = wallpaper;
		}

		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			super.setBounds(left, top, right, bottom);
			// Ensure the wallpaper is as large as it really is, to avoid
			// stretching it
			// at drawing time
			mWallpaper.setBounds(left, top,
					left + mWallpaper.getIntrinsicWidth(),
					top + mWallpaper.getIntrinsicHeight());
		}

		public void draw(Canvas canvas) {
			mWallpaper.draw(canvas);
		}

		public void setAlpha(int alpha) {
			mWallpaper.setAlpha(alpha);
		}

		public void setColorFilter(ColorFilter cf) {
			mWallpaper.setColorFilter(cf);
		}

		public int getOpacity() {
			return mWallpaper.getOpacity();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_PICK_APPWIDGET:
				addAppWidget(data);
				break;
			case REQUEST_CREATE_APPWIDGET:
				completeAddAppWidget(data);
				break;
			}
		} else if (requestCode == REQUEST_PICK_APPWIDGET
				&& resultCode == RESULT_CANCELED && data != null) {
			// Clean up the appWidgetId if we canceled
			int appWidgetId = data.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (appWidgetId != -1) {
				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			}
		}
	}

	/**
	 * 选中了某个widget之后，根据是否有配置来决定直接添加还是弹出配置activity
	 * 
	 * @param data
	 */
	private void addAppWidget(Intent data) {
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				-1);

		String customWidget = data.getStringExtra(EXTRA_CUSTOM_WIDGET);
		Log.d("addAppWidget", "data:" + customWidget);
		if ("search_widget".equals(customWidget)) {
			// 这里直接将search_widget删掉了
			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
		} else {
			AppWidgetProviderInfo appWidget = mAppWidgetManager
					.getAppWidgetInfo(appWidgetId);

			Log.d("addAppWidget", "configure:" + appWidget.configure);
			if (appWidget.configure != null) {
				// 有配置，弹出配置
				Intent intent = new Intent(
						AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
				intent.setComponent(appWidget.configure);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						appWidgetId);

				startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
			} else {
				// 没有配置，直接添加
				onActivityResult(REQUEST_CREATE_APPWIDGET, Activity.RESULT_OK,
						data);
			}
		}
	}

	/**
	 * 请求添加一个新的widget
	 */
	private void addWidget() {
		int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

		Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		// add the search widget
		ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<AppWidgetProviderInfo>();
		AppWidgetProviderInfo info = new AppWidgetProviderInfo();
		info.provider = new ComponentName(getPackageName(), "XXX.YYY");
		info.label = "Search";
		info.icon = R.drawable.icon;
		customInfo.add(info);
		pickIntent.putParcelableArrayListExtra(
				AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
		ArrayList<Bundle> customExtras = new ArrayList<Bundle>();
		Bundle b = new Bundle();
		b.putString(EXTRA_CUSTOM_WIDGET, "search_widget");
		customExtras.add(b);
		pickIntent.putParcelableArrayListExtra(
				AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
		// start the pick activity
		startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
	}

	/**
	 * 添加widget
	 * 
	 * @param data
	 */
	private void completeAddAppWidget(Intent data) {
		Bundle extras = data.getExtras();
		int appWidgetId = extras
				.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

		Log.d("completeAddAppWidget",
				"dumping extras content=" + extras.toString());
		Log.d("completeAddAppWidget", "appWidgetId:" + appWidgetId);
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager
				.getAppWidgetInfo(appWidgetId);

		View hostView = mAppWidgetHost.createView(this, appWidgetId,
				appWidgetInfo);

		layout.addInScreen(hostView, appWidgetInfo.minWidth,
				appWidgetInfo.minHeight);
	}
}
