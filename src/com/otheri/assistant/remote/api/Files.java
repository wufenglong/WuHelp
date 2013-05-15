package com.otheri.assistant.remote.api;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.Stack;

import org.json.JSONException;

import android.content.Context;

import com.otheri.assistant.remote.HttpServer;
import com.otheri.assistant.remote.Message;
import com.otheri.assistant.remote.HttpServer.Response;

public class Files {

	private static final String TAG = "Files";

	private Context context;
	private HttpServer httpServer;

	public Files(Context context, HttpServer httpServer) {
		this.context = context;
		this.httpServer = httpServer;
	}

	public Response get(Properties header, String uri, Stack<String> uris)
			throws IOException {
		String currentUri = uris.pop();
		if (currentUri.equalsIgnoreCase("browse")) {
			return browse(header, uri, uris);
		} else {
			return null;
		}
	}

	public Response browse(Properties header, String uri, Stack<String> uris)
			throws IOException {
		String base = "/_extendapis/file/browse/";
		int ind = uri.indexOf(base);
		if (ind >= 0) {

			String newURI = uri.substring(ind + base.length());
			File root = new File(".");

			File file = new File(root, newURI);
			if (file.exists()) {
				if (file.isDirectory()) {
					if (!newURI.endsWith(File.separator)) {
						newURI += File.separator;
					}
					StringBuilder sb = new StringBuilder();
					sb.append(
							"<html><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><body><h1>Directory ")
							.append(newURI).append("</h1><br/>");
					File[] files = file.listFiles();
					if (files == null) {
						sb.append("没有权限");
					} else {
						for (File f : files) {
							try {
								sb.append("<a href=\"")
										.append(base)
										.append(newURI)
										.append(URLEncoder.encode(f.getName(),
												"UTF-8")).append("\">")
										.append(f.getName()).append("</a>");
							} catch (Exception e) {
								sb.append("<a href=\"").append(base)
										.append(newURI)
										.append(URLEncoder.encode(f.getName()))
										.append("\">").append(f.getName())
										.append("</a>");
							}
							sb.append("<br/>");
						}
					}

					sb.append("</body></html>");
					return httpServer.new Response(HttpServer.HTTP_OK,
							HttpServer.MIME_HTML, sb.toString());
				} else if (file.isFile()) {
					return httpServer.dealFile(header, file);
				}
			}
		}
		return null;
	}

	public Response post(Properties header, Message request)
			throws JSONException, IOException {
		String command = request.getCommand();
		if (command.equalsIgnoreCase("upload")) {
			Message response = upload(request);
			return httpServer.httpGetResponse(response.toString());
		} else {
			return null;
		}
	}

	public Message upload(Message request) throws JSONException, IOException {
		return null;
	}

}
