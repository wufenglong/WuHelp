package com.otheri.assistant.remote.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;

import com.otheri.assistant.remote.HttpServer;
import com.otheri.assistant.remote.HttpServer.Response;
import com.otheri.assistant.remote.Message;
import com.otheri.comm4and.api.Files;

public class RemoteFiles {

	private static final String TAG = "RemoteFiles";

	private Context context;
	private HttpServer httpServer;

	public RemoteFiles(Context context, HttpServer httpServer) {
		this.context = context;
		this.httpServer = httpServer;
	}

	public Response get(String uri, Stack<String> uris) {
		return null;
	}

	public Response post(Properties header, Message request)
			throws JSONException, IOException {
		String command = request.getCommand();
		if (command.equalsIgnoreCase("listFile")) {
			Message response = listFile(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("upload")) {
			Message response = upload(request);
			return httpServer.httpGetResponse(response.toString());
		} else {
			return null;
		}
	}

	public Message listFile(Message request) {
		try {
			String url = request.getContent().getString("url");
			ArrayList<File> files = Files.listFile(url);

			JSONStringer js = new JSONStringer();
			js.object();
			js.key("files").array();
			for (File file : files) {
				js.object();
				js.key("name").value(file.getName());
				js.key("read").value(file.canRead());
				js.key("write").value(file.canWrite());
				js.key("length").value(file.length());
				js.key("isDirectory").value(file.isDirectory());
				js.key("isHidden").value(file.isHidden());
				js.key("lastModified").value(file.lastModified());
				js.endObject();
			}
			js.endArray();
			js.endObject();
			return Message.getResponseMessage(request.getRegion(),
					request.getCommand(), true, new JSONObject(js.toString()));
		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		}
	}

	public Message upload(Message request) throws JSONException, IOException {
		return null;
	}

}
