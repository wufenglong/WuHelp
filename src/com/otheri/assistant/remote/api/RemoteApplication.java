package com.otheri.assistant.remote.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.otheri.assistant.remote.HttpServer;
import com.otheri.assistant.remote.HttpServer.Response;
import com.otheri.assistant.remote.Message;
import com.otheri.comm.Utils;
import com.otheri.comm4and.api.Application;

/**
 * 
 * @author cloud
 * 
 */
public class RemoteApplication {

	private static final String TAG = "RemoteApplication";

	private Context context;
	private HttpServer httpServer;

	private PackageManager pm;
	private ContentResolver cr;

	public RemoteApplication(Context context, HttpServer httpServer) {
		this.context = context;
		this.httpServer = httpServer;
		this.pm = context.getPackageManager();
		this.cr = context.getContentResolver();
	}

	public Response get(String uri, Stack<String> uris) {
		String currentUri = uris.pop();
		if (currentUri.equalsIgnoreCase("getApkIcon")) {
			return getApkIcon(uri, uris);
		}
		if (currentUri.equalsIgnoreCase("getActivityIcon")) {
			return getActivityIcon(uri, uris);
		} else {
			return null;
		}
	}

	public Response post(Message request) throws JSONException {
		String command = request.getCommand();
		Log.e("RemotApplication post....", "command=" + command);
		if (command.equalsIgnoreCase("getApks")) {
			Message response = getApks(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("startApk")) {
			Message response = startApk(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getApkPermission")) {
			Message response = getApkPermission(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("uninstallApk")) {
			Message response = uninstallApk(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("installApk")) {
			Message response = installApk(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getActivitys")) {
			Message response = getActivitys(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getApkLabels")) {
			Message response = getApkLabels(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getActivityLabels")) {
			Message response = getActivityLabels(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getApkInfo")) {
			Message response = getApkInfo(request);
			return httpServer.httpGetResponse(response.toString());
		} else {
			return null;
		}
	}

	public Message getApks(Message request) {
		try {
			HashMap<String, String> apkMap = Application._getApks(pm);
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("apks");
			js.array();
			Iterator<Entry<String, String>> entrys = apkMap.entrySet()
					.iterator();
			while (entrys.hasNext()) {
				Entry<String, String> entry = entrys.next();
				js.object();
				js.key("packageName").value(entry.getKey());
				js.key("type").value(entry.getValue());
				js.endObject();
			}
			js.endArray();
			js.endObject();
			return Message.getResponseMessage(request.getRegion(), request
					.getCommand(), true, new JSONObject(js.toString()));
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Message getApkLabels(Message request) {
		try {
			JSONArray array = request.getContent().getJSONArray("packageNames");
			int size = array.length();
			ArrayList<String> list = new ArrayList<String>(size);
			for (int i = 0; i < size; i++) {
				list.add(array.getString(i));
			}
			HashMap<String, String> labelMap = Application._getApkLabels(pm,
					list);
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("labels");
			js.array();
			Iterator<Entry<String, String>> entrys = labelMap.entrySet()
					.iterator();
			while (entrys.hasNext()) {
				Entry<String, String> entry = entrys.next();
				js.object();
				js.key("packageName").value(entry.getKey());
				js.key("label").value(entry.getValue());
				js.endObject();
			}
			js.endArray();
			js.endObject();
			return Message.getResponseMessage(request.getRegion(), request
					.getCommand(), true, new JSONObject(js.toString()));
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Message getApkInfo(Message request) {
		try {
			String req_packageNames = "packageNames";
			String resp_apkInfos = "apkInfos";
			JSONArray array = request.getContent().getJSONArray(
					req_packageNames);
			List<String> packageNames = Utils.getStringListFromJSONArray(array);
			HashMap<String, ArrayList<String>> appMap = Application
					._getApkInfo(pm, packageNames);

			Iterator<Entry<String, ArrayList<String>>> iterator = appMap
					.entrySet().iterator();
			JSONStringer js = new JSONStringer();
			js.object();
			js.key(resp_apkInfos);
			js.array();
			while (iterator.hasNext()) {
				js.array();
				Entry<String, ArrayList<String>> entry = iterator.next();
				String packageName = entry.getKey();
				js.value(packageName);
				ArrayList<String> list = entry.getValue();
				for (String info : list) {
					js.value(info);
				}
				js.endArray();
			}
			js.endArray();
			js.endObject();
			return Message.getResponseMessage(request.getRegion(), request
					.getCommand(), true, new JSONObject(js.toString()));
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Response getApkIcon(String uri, Stack<String> uris) {
		String packageName = uris.pop();
		try {
			InputStream is = Application._getApkIcon(pm, packageName);
			return httpServer.new Response(HttpServer.HTTP_OK, "image/png", is);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			try {
				return httpServer.new Response(
						HttpServer.HTTP_OK,
						"image/png",
						httpServer
								.getFileInputStream("/img/default_apk_icon.png"));
			} catch (Exception ee) {
				return null;
			}
		}
	}

	public Message startApk(Message request) {
		try {
			String packageName = request.getContent().getString("packageName");
			Application._startApk(context, pm, packageName);
			return Message.getResponseSuccessMessage(request);
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Message getApkPermission(Message request) {
		try {
			String packageName = request.getContent().getString("packageName");
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("permissions").array();
			List<String> permissions = Application._getApkPermission(pm,
					packageName);
			if (permissions != null && permissions.size() > 0) {
				for (String permission : permissions) {
					js.object();
					js.key("name").value(permission);
					js.endObject();
				}
			}
			js.endArray();
			js.endObject();
			return Message.getResponseMessage(request.getRegion(), request
					.getCommand(), true, new JSONObject(js.toString()));
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Message installApk(Message request) {
		try {
			String fileName = request.getContent().getString("fileName");
			Application._installApk(context, cr, fileName);
			return Message.getResponseSuccessMessage(request);
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Message uninstallApk(Message request) {
		try {
			String packageName = request.getContent().getString("packageName");
			Application._uninstallApk(context, pm, packageName);
			return Message.getResponseSuccessMessage(request);
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Message getActivitys(Message request) {
		try {
			HashMap<String, String> activityMap = Application._getActivitys(pm);
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("activitys");
			js.array();
			Iterator<Entry<String, String>> entrys = activityMap.entrySet()
					.iterator();
			while (entrys.hasNext()) {
				Entry<String, String> entry = entrys.next();
				js.object();
				js.key("activityName").value(entry.getKey());
				js.key("packageName").value(entry.getValue());
				js.endObject();
			}
			js.endArray();
			js.endObject();
			return Message.getResponseMessage(request.getRegion(), request
					.getCommand(), true, new JSONObject(js.toString()));
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Message getActivityLabels(Message request) {
		try {
			JSONArray array = request.getContent()
					.getJSONArray("activityInfos");
			int size = array.length();
			HashMap<String, String> map = new HashMap<String, String>(size);
			for (int i = 0; i < size; i++) {
				JSONObject jo = array.getJSONObject(i);
				String activityName = jo.getString("activityName");
				String packageName = jo.getString("packageName");
				map.put(activityName, packageName);
			}

			HashMap<String, String> activityMap = Application
					._getActivityLabels(pm, map);
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("labels");
			js.array();
			Iterator<Entry<String, String>> entrys = activityMap.entrySet()
					.iterator();
			while (entrys.hasNext()) {
				Entry<String, String> entry = entrys.next();
				js.object();
				js.key("activityName").value(entry.getKey());
				js.key("label").value(entry.getValue());
				js.endObject();
			}
			js.endArray();
			js.endObject();
			return Message.getResponseMessage(request.getRegion(), request
					.getCommand(), true, new JSONObject(js.toString()));
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Response getActivityIcon(String uri, Stack<String> uris) {
		String packageName = uris.pop();
		String activityName = uris.pop();
		try {
			InputStream is = Application._getActivityIcon(pm, packageName,
					activityName);
			return httpServer.new Response(HttpServer.HTTP_OK, "image/png", is);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			try {
				return httpServer.new Response(
						HttpServer.HTTP_OK,
						"image/png",
						httpServer
								.getFileInputStream("/img/default_activity_icon.png"));
			} catch (Exception ee) {
				return null;
			}
		}
	}

}
