package com.otheri.assistant.remote.api;

import org.json.JSONException;

import android.content.Context;

import com.otheri.assistant.remote.HttpServer;
import com.otheri.assistant.remote.Message;
import com.otheri.assistant.remote.HttpServer.Response;

public class Setting {

	private Context context;
	private HttpServer httpServer;

	public Setting(Context context, HttpServer httpServer) {
		this.context = context;
		this.httpServer = httpServer;
	}

	public Response post(Message request) throws JSONException {
		String command = request.getCommand();
		if (command.equalsIgnoreCase("getContacts")) {
			Message response = getContacts(request);
			return httpServer.httpGetResponse(response.toString());
		} else {
			return null;
		}
	}

	private Message getContacts(Message request) throws JSONException {
		return null;
	}
}
