package com.otheri.comm4and.api;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONObject;

import com.otheri.comm.Utils;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

/**
 * 
 * @author cloud
 * 
 */
public class Application {

	private static final String TAG = "Application";

	public static HashMap<String, String> _getApks(PackageManager pm) {
		List<PackageInfo> pis = pm.getInstalledPackages(0);
		HashMap<String, String> ret = new HashMap<String, String>(pis.size());
		for (PackageInfo pi : pis) {
			if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
				ret.put(pi.packageName, "sys");
			} else {
				ret.put(pi.packageName, "user");
			}
		}
		return ret;
	}

	public static HashMap<String, String> _getApkLabels(PackageManager pm,
			List<String> packageNames) throws NameNotFoundException {
		int size = packageNames.size();
		HashMap<String, String> ret = new HashMap<String, String>(size);
		for (String packageName : packageNames) {
			ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
			ret.put(packageName, ai.loadLabel(pm).toString());
		}
		return ret;
	}

	/**
	 * 功能：根据包名获得apk基本信息,如，软件名、大小、版本号
	 * 
	 * @return HashMap<String, ArrayList<String>> packageName为key，基本信息为值
	 * @throws NameNotFoundException
	 * */
	public static HashMap<String, ArrayList<String>> _getApkInfo(
			PackageManager pm, List<String> packageNames)
			throws NameNotFoundException {
		int size = packageNames.size();
		HashMap<String, ArrayList<String>> ret = new HashMap<String, ArrayList<String>>(
				size);
		for (String packageName : packageNames) {
			ArrayList<String> apkInfo = new ArrayList<String>();
			PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
			// 软件名
			String Label = (String) pm
					.getApplicationLabel(packageInfo.applicationInfo);
			// 版本号
			String versionName = packageInfo.versionName;
			//versionName = Utils.modify_VersionName(versionName);
			// 软件大小
			Long lfileSize = new File(packageInfo.applicationInfo.sourceDir)
					.length()
					+ new File(packageInfo.applicationInfo.dataDir).length();
			String fileSize = Utils.accountFileSize(lfileSize);

			apkInfo.add(Label);
			apkInfo.add(versionName);
			apkInfo.add(fileSize);
			ret.put(packageName, apkInfo);
		}
		return ret;
	}

	public static InputStream _getApkIcon(PackageManager pm, String packageName)
			throws NameNotFoundException {
		ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
		Resources res = pm.getResourcesForApplication(packageName);
		return res.openRawResource(ai.icon);
	}

	public static void _startApk(Context context, PackageManager pm,
			String packageName) {
		Intent launchIntent = pm.getLaunchIntentForPackage(packageName);
		context.startActivity(launchIntent);
	}

	public static List<String> _getApkPermission(PackageManager pm,
			String packageName) throws NameNotFoundException {
		PackageInfo pi = pm.getPackageInfo(packageName,
				PackageManager.GET_PERMISSIONS);
		String[] permissions = pi.requestedPermissions;
		ArrayList<String> ret = new ArrayList<String>();
		if (permissions != null && permissions.length > 0) {
			for (String per : permissions) {
				ret.add(per);
			}
		}
		return ret;
	}

	public static void _uninstallApk(Context context, PackageManager pm,
			String packageName) {
		Uri packageUri = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
		uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(uninstallIntent);
	}

	public static void _installApk(Context context, ContentResolver cr,
			String fileName) {
		// result 0表示只允许安装google market的应用
		int result = Settings.Secure.getInt(cr,
				Settings.Secure.INSTALL_NON_MARKET_APPS, 0);

		if (result == 0) {
			// 设置为可以安装第三方应用
			Settings.Secure.putInt(cr, Settings.Secure.INSTALL_NON_MARKET_APPS,
					1);
		}

		Uri uri = Uri.fromFile(new File(fileName));
		Intent installIntent = new Intent(Intent.ACTION_VIEW);
		installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		installIntent.setDataAndType(uri,
				"application/vnd.android.package-archive");
		context.startActivity(installIntent);

		if (result == 0) {
			// 恢复用户以前的默认设置
			Settings.Secure.putInt(cr, Settings.Secure.INSTALL_NON_MARKET_APPS,
					1);
		}
	}

	public static HashMap<String, String> _getActivitys(PackageManager pm) {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);

		HashMap<String, String> ret = new HashMap<String, String>(apps.size());
		for (ResolveInfo app : apps) {
			ret.put(app.activityInfo.name,
					app.activityInfo.applicationInfo.packageName);
		}
		return ret;
	}

	public static HashMap<String, String> _getActivityLabels(PackageManager pm,
			HashMap<String, String> activityInfos) throws NameNotFoundException {
		int size = activityInfos.size();
		HashMap<String, String> ret = new HashMap<String, String>(size);
		Iterator<Entry<String, String>> entrys = activityInfos.entrySet()
				.iterator();
		while (entrys.hasNext()) {
			Entry<String, String> entry = entrys.next();
			String activityName = entry.getKey();
			String packageName = entry.getValue();
			ActivityInfo activityInfo = pm.getActivityInfo(new ComponentName(
					packageName, activityName), 0);
			ret.put(activityName, activityInfo.loadLabel(pm).toString());
		}
		return ret;
	}

	public static InputStream _getActivityIcon(PackageManager pm,
			String packageName, String activityName)
			throws NameNotFoundException {
		ActivityInfo ai = pm.getActivityInfo(new ComponentName(packageName,
				activityName), 0);
		Resources res = pm.getResourcesForApplication(packageName);
		return res.openRawResource(ai.icon);
	}

}
