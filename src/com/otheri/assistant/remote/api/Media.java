package com.otheri.assistant.remote.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore.Images.Thumbnails;

import com.otheri.assistant.remote.HttpServer;
import com.otheri.assistant.remote.Message;
import com.otheri.assistant.remote.HttpServer.Response;

public class Media {

	private Context context;
	private HttpServer httpServer;

	private ContentResolver cr;

	public Media(Context context, HttpServer httpServer) {
		this.context = context;
		this.httpServer = httpServer;

		cr = context.getContentResolver();
	}

	public Response get(String uri, Stack<String> uris)
			throws NameNotFoundException, IOException {
		String currentUri = uris.pop();
		if (currentUri.equalsIgnoreCase("getImageThumb")) {
			return getImageThumb(uri, uris);
		} else if (currentUri.equalsIgnoreCase("getImage")) {
			return getImage(uri, uris);
		} else {
			return null;
		}
	}

	public Response getImageThumb(String uri, Stack<String> uris)
			throws IOException {
		String imageId = uris.pop();
		String kind = uris.pop();

		int intKind = Thumbnails.MICRO_KIND;
		if ("mini".equalsIgnoreCase(kind)) {
			intKind = Thumbnails.MINI_KIND;
		}

		Bitmap bitmap = Thumbnails.getThumbnail(cr, Long.parseLong(imageId),
				intKind, new BitmapFactory.Options());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);

		out.flush();
		byte[] data = out.toByteArray();

		return httpServer.new Response(HttpServer.HTTP_OK, "image/png",
				new ByteArrayInputStream(data));
	}

	public Response getImage(String uri, Stack<String> uris) {
		String imageId = uris.pop();

		Uri tUri = Uri.withAppendedPath(Thumbnails.EXTERNAL_CONTENT_URI,
				imageId);

		// Thumbnails.getThumbnail(arg0, arg1, arg2, arg3)

		return null;
	}

	public Response post(Message request) throws JSONException {
		String command = request.getCommand();
		if (command.equalsIgnoreCase("getImageThumbs")) {
			Message response = getImageThumbs(request);
			return httpServer.httpGetResponse(response.toString());
		} else {
			return null;
		}
	}

	private Message getImageThumbs(Message request) throws JSONException {
		
		String resp_imageThumbs = "imageThumbs";

		String[] projectionThumbnail = new String[] { Thumbnails.IMAGE_ID };
		Cursor cursorThumbnail = Thumbnails.queryMiniThumbnails(cr,
				Thumbnails.EXTERNAL_CONTENT_URI, Thumbnails.MINI_KIND,
				projectionThumbnail);
		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_imageThumbs);
		js.array();
		while (cursorThumbnail.moveToNext()) {
			int count = cursorThumbnail.getColumnCount();
			js.object();
			for (int i = 0; i < count; i++) {
				js.key(cursorThumbnail.getColumnName(i)).value(
						cursorThumbnail.getString(i));
			}
			js.endObject();
		}
		js.endArray();
		js.endObject();
		cursorThumbnail.close();

		return Message.getResponseMessage(request.getRegion(),
				request.getCommand(), true, new JSONObject(js.toString()));
	}
}
