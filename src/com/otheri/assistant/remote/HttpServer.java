package com.otheri.assistant.remote;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;
import android.util.Log;

import com.otheri.assistant.remote.api.Media;
import com.otheri.assistant.remote.api.RemoteApplication;
import com.otheri.assistant.remote.api.RemoteEnvironment;
import com.otheri.assistant.remote.api.RemoteFiles;
import com.otheri.assistant.remote.api.RemoteTelephony;

public class HttpServer {

	private static final String TAG = "HttpServer";

	/**
	 * Some HTTP response status codes
	 */
	public static final String HTTP_OK = "200 OK",
			HTTP_REDIRECT = "301 Moved Permanently",
			HTTP_FORBIDDEN = "403 Forbidden", HTTP_NOTFOUND = "404 Not Found",
			HTTP_BADREQUEST = "400 Bad Request",
			HTTP_INTERNALERROR = "500 Internal Server Error",
			HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String MIME_PLAINTEXT = "text/plain",
			MIME_HTML = "text/html",
			MIME_DEFAULT_BINARY = "application/octet-stream",
			MIME_XML = "text/xml";

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	private static Hashtable<String, String> theMimeTypes = new Hashtable<String, String>();

	/**
	 * GMT date formatter
	 */
	private static java.text.SimpleDateFormat gmtFrmt;

	static {
		StringTokenizer st = new StringTokenizer("css		text/css "
				+ "js			text/javascript " + "htm		text/html "
				+ "html		text/html " + "txt		text/plain " + "asc		text/plain "
				+ "gif		image/gif " + "jpg		image/jpeg " + "jpeg		image/jpeg "
				+ "png		image/png " + "mp3		audio/mpeg "
				+ "m3u		audio/mpeg-url " + "pdf		application/pdf "
				+ "doc		application/msword " + "ogg		application/x-ogg "
				+ "zip		application/octet-stream "
				+ "exe		application/octet-stream "
				+ "class		application/octet-stream ");
		while (st.hasMoreTokens()) {
			theMimeTypes.put(st.nextToken(), st.nextToken());
		}

		gmtFrmt = new java.text.SimpleDateFormat(
				"E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private Context context;
	private RemoteEnvironment env;
	private RemoteApplication app;
	private RemoteTelephony tel;
	private Media media;
	private RemoteFiles files;

	private String webroot;
	private int port;

	private ServerSocket myServerSocket;
	private Thread myThread;

	/**
	 * Starts a HTTP server to given port.
	 * <p>
	 * Throws an IOException if the socket is already in use
	 */
	public HttpServer(Context context, String webroot, int port) {
		this.context = context;
		this.webroot = webroot;
		this.port = port;

		env = new RemoteEnvironment(context, this);
		app = new RemoteApplication(context, this);
		tel = new RemoteTelephony(context, this);
		media = new Media(context, this);
		files = new RemoteFiles(context, this);
	}

	public void start() throws IOException {

		myServerSocket = new ServerSocket(port);
		myThread = new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						new HTTPSession(myServerSocket.accept());
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		});
		myThread.setDaemon(true);
		myThread.start();

	}

	public void stop() {
		try {
			myServerSocket.close();
			myThread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * HTTP response. Return one of these from serve().
	 */
	public class Response {

		/**
		 * Default constructor: response = HTTP_OK, data = mime = 'null'
		 */
		public Response() {
			this.status = HTTP_OK;
		}

		/**
		 * Basic constructor.
		 */
		public Response(String status, String mimeType, InputStream data) {
			this.status = status;
			this.mimeType = mimeType;
			this.data = data;
		}

		/**
		 * Convenience method that makes an InputStream out of given text.
		 */
		public Response(String status, String mimeType, String txt) {
			this.status = status;
			this.mimeType = mimeType;
			try {
				this.data = new ByteArrayInputStream(txt.getBytes("UTF-8"));
			} catch (java.io.UnsupportedEncodingException uee) {
				uee.printStackTrace();
			}
		}

		/**
		 * Adds given line to the header.
		 */
		public void addHeader(String name, String value) {
			header.put(name, value);
		}

		/**
		 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
		 */
		public String status;

		/**
		 * MIME type of content, e.g. "text/html"
		 */
		public String mimeType;

		/**
		 * Data of the response, may be null.
		 */
		public InputStream data;

		/**
		 * Headers for the HTTP response. Use addHeader() to add lines.
		 */
		public Properties header = new Properties();
	}

	/**
	 * Handles one session, i.e. parses the HTTP request and returns the
	 * response.
	 */
	private class HTTPSession implements Runnable {
		public HTTPSession(Socket s) {
			mySocket = s;
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}

		public void run() {
			try {
				InputStream is = mySocket.getInputStream();
				if (is == null)
					return;

				// Read the first 8192 bytes.
				// The full header should fit in here.
				// Apache's default header limit is 8KB.
				int bufsize = 8192;
				byte[] buf = new byte[bufsize];
				int rlen = is.read(buf, 0, bufsize);
				if (rlen <= 0)
					return;

				// Create a BufferedReader for parsing the header.
				ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0,
						rlen);
				BufferedReader hin = new BufferedReader(new InputStreamReader(
						hbis));
				Properties pre = new Properties();
				Properties parms = new Properties();
				Properties header = new Properties();
				Properties File = new Properties();

				// Decode the header into parms and header java properties
				decodeHeader(hin, pre, parms, header);
				String method = pre.getProperty("method");
				String uri = pre.getProperty("uri");

				long size = 0x7FFFFFFFFFFFFFFFl;
				String contentLength = header.getProperty("content-length");
				if (contentLength != null) {
					try {
						size = Integer.parseInt(contentLength);
					} catch (NumberFormatException ex) {
						ex.printStackTrace();
					}
				}

				// We are looking for the byte separating header from body.
				// It must be the last byte of the first two sequential new
				// lines.
				int splitbyte = 0;
				boolean sbfound = false;
				while (splitbyte < rlen) {
					if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n'
							&& buf[++splitbyte] == '\r'
							&& buf[++splitbyte] == '\n') {
						sbfound = true;
						break;
					}
					splitbyte++;
				}
				splitbyte++;

				// Write the part of body already read to ByteArrayOutputStream
				// f
				ByteArrayOutputStream f = new ByteArrayOutputStream();
				if (splitbyte < rlen)
					f.write(buf, splitbyte, rlen - splitbyte);

				// While Firefox sends on the first read all the data fitting
				// our buffer, Chrome and Opera sends only the headers even if
				// there is data for the body. So we do some magic here to find
				// out whether we have already consumed part of body, if we
				// have reached the end of the data to be sent or we should
				// expect the first byte of the body at the next read.
				if (splitbyte < rlen)
					size -= rlen - splitbyte + 1;
				else if (!sbfound || size == 0x7FFFFFFFFFFFFFFFl)
					size = 0;

				// Now read all the body and write it to f
				buf = new byte[512];
				while (rlen >= 0 && size > 0) {
					rlen = is.read(buf, 0, 512);
					size -= rlen;
					if (rlen > 0)
						f.write(buf, 0, rlen);
				}

				// Get the raw body as a byte []
				byte[] fbuf = f.toByteArray();

				// Create a BufferedReader for easily reading it as string.
				ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						bin));

				String postContent = "";

				// If the method is POST, there may be parameters
				// in data section, too, read it:
				if (method.equalsIgnoreCase("POST")) {
					String contentType = "";
					String contentTypeHeader = header
							.getProperty("content-type");
					StringTokenizer st = new StringTokenizer(contentTypeHeader,
							"; ");
					if (st.hasMoreTokens()) {
						contentType = st.nextToken();
					}

					if (contentType.equalsIgnoreCase("multipart/form-data")) {
						// multipart/form-data：
						// 窗体数据被编码为一条消息，页上的每个控件对应消息中的一个部分，上传附件用到
						if (!st.hasMoreTokens())
							sendError(HTTP_BADREQUEST,
									"BAD REQUEST: multipart/form-data boundary missing");
						String boundaryExp = st.nextToken();
						st = new StringTokenizer(boundaryExp, "=");
						if (st.countTokens() != 2)
							sendError(HTTP_BADREQUEST,
									"BAD REQUEST: multipart/form-data boundary syntax error");
						st.nextToken();
						String boundary = st.nextToken();

						decodeMultipartData(boundary, fbuf, in, parms, File);
					} else if (contentType.equalsIgnoreCase(MIME_PLAINTEXT)) {
						// text/plain： 窗体数据以纯文本形式进行编码，其中不含任何控件或格式字符。
						postContent = new String(fbuf, "UTF-8");
					} else {
						// application/x-www-form-urlencoded： 窗体数据被编码为名称/值对
						String postLine = "";
						char pbuf[] = new char[512];
						int read = in.read(pbuf);
						while (read >= 0 && !postLine.endsWith("\r\n")) {
							postLine += String.valueOf(pbuf, 0, read);
							read = in.read(pbuf);
						}

						decodeParms(postLine.toString().trim(), parms);
					}
				}

				// Ok, now do the serve()
				Response r = serve(uri, method, header, parms, File,
						postContent);
				if (r == null) {
					sendError(HTTP_INTERNALERROR,
							"SERVER INTERNAL ERROR: null response");
				} else {
					sendResponse(r.status, r.mimeType, r.header, r.data);
				}

				in.close();
				is.close();
			} catch (IOException ioe) {
				try {
					sendError(
							HTTP_INTERNALERROR,
							"SERVER INTERNAL ERROR: IOException: "
									+ ioe.getMessage());
				} catch (Throwable t) {
				}
			} catch (InterruptedException ie) {
				// Thrown by sendError, ignore and exit the thread.
			}
		}

		/**
		 * Decodes the sent headers and loads the data into java Properties' key
		 * - value pairs
		 **/
		private void decodeHeader(BufferedReader in, Properties pre,
				Properties parms, Properties header)
				throws InterruptedException {
			try {
				// Read the request line
				String inLine = in.readLine();
				if (inLine == null)
					return;
				StringTokenizer st = new StringTokenizer(inLine);
				if (!st.hasMoreTokens())
					sendError(HTTP_BADREQUEST,
							"BAD REQUEST: Syntax error. Usage: GET /example/file.html");

				String method = st.nextToken();
				pre.put("method", method);

				if (!st.hasMoreTokens())
					sendError(HTTP_BADREQUEST,
							"BAD REQUEST: Missing URI. Usage: GET /example/file.html");

				String uri = st.nextToken();

				// Decode parameters from the URI
				int qmi = uri.indexOf('?');
				if (qmi >= 0) {
					decodeParms(uri.substring(qmi + 1), parms);
					uri = decodePercent(uri.substring(0, qmi));
				} else
					uri = decodePercent(uri);

				// If there's another token, it's protocol version,
				// followed by HTTP headers. Ignore version but parse headers.
				// NOTE: this now forces header names lowercase since they are
				// case insensitive and vary by client.
				if (st.hasMoreTokens()) {
					String line = in.readLine();
					while (line != null && line.trim().length() > 0) {
						int p = line.indexOf(':');
						if (p >= 0)
							header.put(line.substring(0, p).trim()
									.toLowerCase(), line.substring(p + 1)
									.trim());
						line = in.readLine();
					}
				}

				pre.put("uri", uri);
			} catch (IOException ioe) {
				sendError(
						HTTP_INTERNALERROR,
						"SERVER INTERNAL ERROR: IOException: "
								+ ioe.getMessage());
			}
		}

		/**
		 * Decodes the Multipart Body data and put it into java Properties' key
		 * - value pairs.
		 **/
		private void decodeMultipartData(String boundary, byte[] fbuf,
				BufferedReader in, Properties parms, Properties File)
				throws InterruptedException {
			try {
				int[] bpositions = getBoundaryPositions(fbuf,
						boundary.getBytes());
				int boundarycount = 1;
				String mpline = in.readLine();
				while (mpline != null) {
					if (mpline.indexOf(boundary) == -1)
						sendError(
								HTTP_BADREQUEST,
								"BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html");
					boundarycount++;
					Properties item = new Properties();
					mpline = in.readLine();
					while (mpline != null && mpline.trim().length() > 0) {
						int p = mpline.indexOf(':');
						if (p != -1)
							item.put(mpline.substring(0, p).trim()
									.toLowerCase(), mpline.substring(p + 1)
									.trim());
						mpline = in.readLine();
					}
					if (mpline != null) {
						String contentDisposition = item
								.getProperty("content-disposition");
						if (contentDisposition == null) {
							sendError(
									HTTP_BADREQUEST,
									"BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html");
						}
						StringTokenizer st = new StringTokenizer(
								contentDisposition, "; ");
						Properties disposition = new Properties();
						while (st.hasMoreTokens()) {
							String token = st.nextToken();
							int p = token.indexOf('=');
							if (p != -1)
								disposition.put(token.substring(0, p).trim()
										.toLowerCase(), token.substring(p + 1)
										.trim());
						}
						String pname = disposition.getProperty("name");
						pname = pname.substring(1, pname.length() - 1);

						String value = "";
						if (item.getProperty("content-type") == null) {
							while (mpline != null
									&& mpline.indexOf(boundary) == -1) {
								mpline = in.readLine();
								if (mpline != null) {
									int d = mpline.indexOf(boundary);
									if (d == -1)
										value += mpline;
									else
										value += mpline.substring(0, d - 2);
								}
							}
						} else {
							if (boundarycount > bpositions.length)
								sendError(HTTP_INTERNALERROR,
										"Error processing request");
							int offset = stripMultipartHeaders(fbuf,
									bpositions[boundarycount - 2]);
							String path = saveTmpFile(fbuf, offset,
									bpositions[boundarycount - 1] - offset - 4);
							File.put(pname, path);
							value = disposition.getProperty("filename");
							value = value.substring(1, value.length() - 1);
							do {
								mpline = in.readLine();
							} while (mpline != null
									&& mpline.indexOf(boundary) == -1);
						}
						parms.put(pname, value);
					}
				}
			} catch (IOException ioe) {
				sendError(
						HTTP_INTERNALERROR,
						"SERVER INTERNAL ERROR: IOException: "
								+ ioe.getMessage());
			}
		}

		/**
		 * Find the byte positions where multipart boundaries start.
		 **/
		public int[] getBoundaryPositions(byte[] b, byte[] boundary) {
			int matchcount = 0;
			int matchbyte = -1;
			Vector<Integer> matchbytes = new Vector<Integer>();
			for (int i = 0; i < b.length; i++) {
				if (b[i] == boundary[matchcount]) {
					if (matchcount == 0)
						matchbyte = i;
					matchcount++;
					if (matchcount == boundary.length) {
						matchbytes.addElement(new Integer(matchbyte));
						matchcount = 0;
						matchbyte = -1;
					}
				} else {
					i -= matchcount;
					matchcount = 0;
					matchbyte = -1;
				}
			}
			int[] ret = new int[matchbytes.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = matchbytes.elementAt(i).intValue();
			}
			return ret;
		}

		/**
		 * Retrieves the content of a sent file and saves it to a temporary
		 * file. The full path to the saved file is returned.
		 **/
		private String saveTmpFile(byte[] b, int offset, int len) {
			String path = "";
			if (len > 0) {
				String tmpdir = System.getProperty("java.io.tmpdir");
				try {
					File temp = File.createTempFile("NanoHTTPD", "", new File(
							tmpdir));
					OutputStream fstream = new FileOutputStream(temp);
					fstream.write(b, offset, len);
					fstream.close();
					path = temp.getAbsolutePath();
				} catch (Exception e) { // Catch exception if any
					System.err.println("Error: " + e.getMessage());
				}
			}
			return path;
		}

		/**
		 * It returns the offset separating multipart file headers from the
		 * file's data.
		 **/
		private int stripMultipartHeaders(byte[] b, int offset) {
			int i = 0;
			for (i = offset; i < b.length; i++) {
				if (b[i] == '\r' && b[++i] == '\n' && b[++i] == '\r'
						&& b[++i] == '\n')
					break;
			}
			return i + 1;
		}

		/**
		 * Decodes the percent encoding scheme. <br/>
		 * For example: "an+example%20string" -> "an example string"
		 */
		private String decodePercent(String str) throws InterruptedException {
			try {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < str.length(); i++) {
					char c = str.charAt(i);
					switch (c) {
					case '+':
						sb.append(' ');
						break;
					case '%':
						sb.append((char) Integer.parseInt(
								str.substring(i + 1, i + 3), 16));
						i += 2;
						break;
					default:
						sb.append(c);
						break;
					}
				}
				return sb.toString();
			} catch (Exception e) {
				sendError(HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding.");
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format ( e.g.
		 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
		 * Properties. NOTE: this doesn't support multiple identical keys due to
		 * the simplicity of Properties -- if you need multiples, you might want
		 * to replace the Properties with a Hashtable of Vectors or such.
		 */
		private void decodeParms(String parms, Properties p)
				throws InterruptedException {
			if (parms == null)
				return;

			StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens()) {
				String e = st.nextToken();
				int sep = e.indexOf('=');
				if (sep >= 0)
					p.put(decodePercent(e.substring(0, sep)).trim(),
							decodePercent(e.substring(sep + 1)));
			}
		}

		/**
		 * Returns an error message as a HTTP response and throws
		 * InterruptedException to stop further request processing.
		 */
		private void sendError(String status, String msg)
				throws InterruptedException {
			sendResponse(status, MIME_PLAINTEXT, null,
					new ByteArrayInputStream(msg.getBytes()));
			throw new InterruptedException();
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse(String status, String mime,
				Properties header, InputStream data) {
			try {
				if (status == null)
					throw new Error("sendResponse(): Status can't be null.");

				OutputStream out = mySocket.getOutputStream();
				PrintWriter pw = new PrintWriter(out);
				pw.print("HTTP/1.0 " + status + " \r\n");

				if (mime != null)
					pw.print("Content-Type: " + mime + "\r\n");

				if (header == null || header.getProperty("Date") == null)
					pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");

				if (header != null) {
					Enumeration e = header.keys();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String value = header.getProperty(key);
						pw.print(key + ": " + value + "\r\n");
					}
				}

				pw.print("\r\n");
				pw.flush();

				if (data != null) {
					byte[] buff = new byte[2048];
					while (true) {
						int read = data.read(buff, 0, 2048);
						if (read <= 0)
							break;
						out.write(buff, 0, read);
					}
				}
				out.flush();
				out.close();
				if (data != null)
					data.close();
			} catch (IOException ioe) {
				// Couldn't write? No can do.
				try {
					mySocket.close();
				} catch (Throwable t) {
				}
			}
		}

		private Socket mySocket;
	}

	private Response serve(String uri, String method, Properties header,
			Properties parms, Properties File, String postContent) {
		Log.e(TAG, method + " '" + uri + "' ");

		Enumeration e = header.propertyNames();
		while (e.hasMoreElements()) {
			String value = (String) e.nextElement();
			Log.e(TAG, "  HDR: '" + value + "' = '" + header.getProperty(value)
					+ "'");
		}
		e = parms.propertyNames();
		while (e.hasMoreElements()) {
			String value = (String) e.nextElement();
			Log.e(TAG, "  PRM: '" + value + "' = '" + parms.getProperty(value)
					+ "'");
		}
		e = File.propertyNames();
		while (e.hasMoreElements()) {
			String value = (String) e.nextElement();
			Log.e(TAG,
					"  UPLOADED: '" + value + "' = '" + File.getProperty(value)
							+ "'");
		}

		Log.e(TAG, "  POST CONTENT: '" + postContent + "'");

		// return serveFile(uri, header, new File("."), true);
		return serveFile(uri, method, header, new File(webroot), postContent);
	}

	public InputStream getFileInputStream(String uri)
			throws FileNotFoundException {
		File file = new File(new File(webroot), uri);
		return new FileInputStream(file);
	}

	/**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters.
	 */
	private Response serveFile(String uri, String method, Properties header,
			File homeDir, String postContent) {

		// Remove URL arguments
		uri = uri.trim().replace(File.separatorChar, '/');
		if (uri.indexOf('?') >= 0)
			uri = uri.substring(0, uri.indexOf('?'));

		File f = new File(homeDir, uri);
		if (!f.exists()) {
			if (method.equalsIgnoreCase("POST")) {
				return extendHttpPOST(this, header, uri, postContent);
			} else {
				return extendHttpGET(this, header, uri);
			}
		}

		// List the directory, if necessary
		if (f.isDirectory()) {
			// Browsers get confused without '/' after the
			// directory, send a redirect.
			if (!uri.endsWith("/")) {
				uri += "/";
				Response r = new Response(HTTP_REDIRECT, MIME_HTML,
						"<html><body>Redirected: <a href=\"" + uri + "\">"
								+ uri + "</a></body></html>");
				r.addHeader("Location", uri);
				return r;
			}

			// First try index.html and index.htm
			if (new File(f, "index.html").exists()) {
				f = new File(homeDir, uri + "/index.html");
			} else if (new File(f, "index.htm").exists()) {
				f = new File(homeDir, uri + "/index.htm");
			} else {
				return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
						"FORBIDDEN: No index.html");
			}
		}

		return dealFile(header, f);
	}

	public Response dealFile(Properties header, File f) {
		try {
			// Get MIME type from file name extension, if possible
			String mime = null;
			int dot = f.getCanonicalPath().lastIndexOf('.');
			if (dot >= 0)
				mime = (String) theMimeTypes.get(f.getCanonicalPath()
						.substring(dot + 1).toLowerCase());
			if (mime == null)
				mime = MIME_DEFAULT_BINARY;

			// Support (simple) skipping:
			long startFrom = 0;
			String range = header.getProperty("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					if (minus > 0)
						range = range.substring(0, minus);
					try {
						startFrom = Long.parseLong(range);
					} catch (NumberFormatException nfe) {
					}
				}
			}

			FileInputStream fis = new FileInputStream(f);
			fis.skip(startFrom);
			Response r = new Response(HTTP_OK, mime, fis);
			r.addHeader("Content-length", "" + (f.length() - startFrom));
			r.addHeader("Content-range", "" + startFrom + "-"
					+ (f.length() - 1) + "/" + f.length());
			return r;
		} catch (IOException ioe) {
			return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT,
					"FORBIDDEN: Reading file failed.");
		}
	}

	// //////////////////////////////////////////////////

	public Stack<String> spliteUri(String uri) {
		// Remove URL arguments
		uri = uri.trim().replace(File.separatorChar, '/');
		if (uri.indexOf('?') >= 0)
			uri = uri.substring(0, uri.indexOf('?'));
		Stack<String> ret = new Stack<String>();
		String[] uris = uri.split("/");
		for (int i = uris.length - 1; i >= 0; i--) {
			String str = uris[i].trim();
			if (!str.equalsIgnoreCase("")) {
				ret.push(str);
			}
		}
		return ret;
	}

	public Response httpGetResponse(String content) {
		return new Response(HttpServer.HTTP_OK, HttpServer.MIME_PLAINTEXT,
				content);
	}

	private Response httpGetError(String uri, String error) {
		Response ret = null;
		JSONStringer js = new JSONStringer();
		try {
			js.object();
			js.key("error").value(error);
			js.key("url").value(uri);
			js.endObject();
			Message response = Message.getResponseMessage("/", "httpGet",
					false, new JSONObject(js.toString()));
			ret = new Response(HttpServer.HTTP_NOTFOUND,
					HttpServer.MIME_PLAINTEXT, response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private Response httpPostError(Message request, String error) {
		Response ret = null;
		JSONStringer js = new JSONStringer();
		try {
			js.object();
			js.key("error").value(error);
			js.key("request").value(request.toString());
			js.endObject();
			Message response = Message.getResponseMessage(request.getRegion(),
					request.getCommand(), false, new JSONObject(js.toString()));
			ret = new Response(HttpServer.HTTP_OK, HttpServer.MIME_PLAINTEXT,
					response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private Response httpPostErrorBadRequest(String request) {
		Response ret = null;
		JSONStringer js = new JSONStringer();
		try {
			js.object();
			js.key("error").value("bad request");
			js.key("request").value(request);
			js.endObject();
			Message response = Message.getResponseMessage("/", "httpPost",
					false, new JSONObject(js.toString()));
			ret = new Response(HttpServer.HTTP_OK, HttpServer.MIME_PLAINTEXT,
					response.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private Response extendHttpGET(HttpServer httpServer, Properties header,
			String uri) {
		Response ret = null;
		try {
			Stack<String> uris = spliteUri(uri);
			if (!uris.empty()) {
				String currentUri = uris.pop();
				if (currentUri.equalsIgnoreCase("_extendapis")) {
					currentUri = uris.pop();
					if (currentUri.equalsIgnoreCase("env")) {
						ret = env.get(uri, uris);
					} else if (currentUri.equalsIgnoreCase("app")) {
						ret = app.get(uri, uris);
					} else if (currentUri.equalsIgnoreCase("tel")) {
						ret = tel.get(uri, uris);
					} else if (currentUri.equalsIgnoreCase("media")) {
						ret = media.get(uri, uris);
					} else if (currentUri.equalsIgnoreCase("file")) {
						ret = files.get(uri, uris);
					}
				}
			}

			if (ret == null) {
				ret = httpGetError(uri, "undefine");
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = httpGetError(uri, e.toString());
		}
		return ret;
	}

	private Response extendHttpPOST(HttpServer httpServer, Properties header,
			String uri, String content) {
		Response ret = null;
		Message request = null;
		try {
			request = Message.getMessageFromJsonString(content);
			String region = request.getRegion();
			if (region.equalsIgnoreCase("env")) {
				ret = env.post(request);
			} else if (region.equalsIgnoreCase("app")) {
				return app.post(request);
			} else if (region.equalsIgnoreCase("tel")) {
				ret = tel.post(request);
			} else if (region.equalsIgnoreCase("media")) {
				ret = media.post(request);
			} else if (region.equalsIgnoreCase("file")) {
				ret = files.post(header, request);
			}

			if (ret == null) {
				ret = httpPostError(request, "undefine");
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (request == null) {
				httpPostErrorBadRequest(content);
			} else {
				ret = httpPostError(request, e.toString());
			}
		}
		return ret;
	}
}
