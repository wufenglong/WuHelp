package com.otheri.assistant.remote;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.util.Log;

public class Message {

	private static final String TAG = "Message";

	private static final String KEY_REGION = "region";
	private static final String KEY_COMMAND = "command";
	private static final String KEY_SUCCESS = "success";
	private static final String KEY_CONTENT = "content";

	private JSONObject message;

	private Message(String jsonString) throws JSONException {
		message = new JSONObject(jsonString);
	}

	private Message(String region, String command, boolean success,
			JSONObject content) throws JSONException {
		message = new JSONObject();
		message.put(KEY_REGION, region);
		message.put(KEY_COMMAND, command);
		message.put(KEY_SUCCESS, success);
		message.put(KEY_CONTENT, content);
	}

	public String getRegion() throws JSONException {
		return message.getString(KEY_REGION);
	}

	public String getCommand() throws JSONException {
		return message.getString(KEY_COMMAND);
	}

	public boolean isSuccess() throws JSONException {
		return message.getBoolean(KEY_SUCCESS);
	}

	public JSONObject getContent() throws JSONException {
		return message.getJSONObject(KEY_CONTENT);
	}

	public String toString() {
		return message.toString();
	}

	public JSONObject getJSONObject() {
		return message;
	}

	public static Message getRequestMessage(String region, String command,
			JSONObject content) throws JSONException {
		return new Message(region, command, true, content);
	}

	public static Message getResponseMessage(String region, String command,
			boolean success, JSONObject content) throws JSONException {
		return new Message(region, command, success, content);
	}

	public static Message getResponseSuccessMessage(Message request) {
		try {
			JSONStringer js = new JSONStringer();
			js.object();
			js.key(KEY_REGION).value(request.getRegion());
			js.key(KEY_COMMAND).value(request.getCommand());
			js.key(KEY_SUCCESS).value(true);
			js.key(KEY_CONTENT);
			{
				js.object();
				js.key("ok").value("ok");
				js.endObject();
			}
			js.endObject();
			return new Message(js.toString());
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}

	public static Message getResponseErrorMessage(Message request, String error) {
		try {
			JSONStringer js = new JSONStringer();
			js.object();
			js.key(KEY_REGION).value(request.getRegion());
			js.key(KEY_COMMAND).value(request.getCommand());
			js.key(KEY_SUCCESS).value(false);
			js.key(KEY_CONTENT);
			{
				js.object();
				js.key("error").value(error);
				js.endObject();
			}
			js.endObject();
			return new Message(js.toString());
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}

	public static Message getMessageFromJsonString(String jsonString)
			throws JSONException {
		return new Message(jsonString);
	}
}
