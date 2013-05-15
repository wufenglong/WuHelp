package com.otheri.assistant.remote.api;

import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;

import com.otheri.assistant.remote.HttpServer;
import com.otheri.assistant.remote.HttpServer.Response;
import com.otheri.assistant.remote.Message;
import com.otheri.comm4and.api.Environment;

/**
 * 
 * @author cloud
 * 
 */
public class RemoteEnvironment {

	private static final String TAG = "RemoteEnvironment";

	private Context context;
	private HttpServer httpServer;

	public RemoteEnvironment(Context context, HttpServer httpServer) {
		this.context = context;
		this.httpServer = httpServer;

	}

	public Response get(String uri, Stack<String> uris) {
		String currentUri = uris.pop();
		if (currentUri.equalsIgnoreCase("xxxxxxx")) {
			return null;
		} else {
			return null;
		}
	}

	public Response post(Message request) throws JSONException {
		String command = request.getCommand();
		if (command.equalsIgnoreCase("heartbeat")) {
			Message response = heartbeat(request);
			return httpServer.httpGetResponse(response.toString());
		} else {
			return null;
		}
	}

	public Message heartbeat(Message request) {
		String[][] events = Environment._heartbeat();
		try {
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("events");
			js.object();
			for (String[] event : events) {
				js.key(event[0]).value(event[1]);
			}
			js.endObject();
			js.endObject();
			return Message.getResponseMessage(request.getRegion(),
					request.getCommand(), true, new JSONObject(js.toString()));
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

}
