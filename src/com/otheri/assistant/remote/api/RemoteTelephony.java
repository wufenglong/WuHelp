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
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.otheri.assistant.remote.HttpServer;
import com.otheri.assistant.remote.HttpServer.Response;
import com.otheri.assistant.remote.Message;
import com.otheri.comm.Utils;
import com.otheri.comm4and.api.Telephony;
import com.otheri.comm4and.model.ModelContact;

public class RemoteTelephony {

	private static final String TAG = "RemoteTelephony";

	private Context context;
	private HttpServer httpServer;

	private ContentResolver cr;

	public RemoteTelephony(Context context, HttpServer httpServer) {
		this.context = context;
		this.httpServer = httpServer;

		cr = context.getContentResolver();
	}

	private Response getContactPhoto(String uri, Stack<String> uris) {
		String contactId = uris.pop();
		try {
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
					Long.parseLong(contactId));
			InputStream is = Contacts.openContactPhotoInputStream(cr,
					contactUri);
			if (is != null) {
				return httpServer.new Response(HttpServer.HTTP_OK, "image/png",
						is);
			} else {
				return httpServer.new Response(
						HttpServer.HTTP_OK,
						"image/png",
						httpServer
								.getFileInputStream("/img/default_contact_icon.png"));
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}

	public Response get(String uri, Stack<String> uris) {
		String currentUri = uris.pop();
		if (currentUri.equalsIgnoreCase("getContactPhoto")) {
			return getContactPhoto(uri, uris);
		} else {
			return null;
		}
	}

	public Response post(Message request) throws JSONException {
		String command = request.getCommand();
		if (command.equalsIgnoreCase("getCallLogs")) {
			Message response = getCallLogs(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("removeCallLogs")) {
			Message response = removeCallLogs(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getConversations")) {
			Message response = getConversations(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("delConversations")) {
			Message response = delConversations(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("delSmsMessage")) {
			Message response = delSmsMessage(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("sendSmsMessage")) {
			Message response = sendSmsMessage(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getMessages")) {
			Message response = getMessages(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getContacts")) {
			Message response = getContacts(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("delContacts")) {
			Message response = delContacts(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getContactsByPhoneNumber")) {
			Message response = getContactsByPhoneNumber(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("flashInsertContact")) {
			Message response = flashInsertContact(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("getContactByContactId")) {
			Message response = getContactByContactId(request);
			return httpServer.httpGetResponse(response.toString());
		} else if (command.equalsIgnoreCase("insertContact")) {
			Message response = insertContact(request);
			return httpServer.httpGetResponse(response.toString());
		} else {
			return null;
		}
	}

	// wu0wu
	public Message flashInsertContact(Message request) throws JSONException {

		String req_displayName = "displayName";
		String req_mobilePhone = "mobilePhone";
		String resp_contactId = "contactId";
		String displayName = request.getContent().getString(req_displayName);
		String mobilePhone = request.getContent().getString(req_mobilePhone);

		String contactId = Telephony._flashInsertContact(cr, displayName,
				mobilePhone);
		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_contactId).value(contactId);
		js.endObject();
		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));
	}

	// wu0wu
	public Message getContactsByPhoneNumber(Message request)
			throws JSONException {
		String req_phoneNumber = "phoneNumber";
		String resp_contactsByPhoneNumber = "contactsByPhoneNumber";
		String phoneNumber = request.getContent().getString(req_phoneNumber);

		List<String[]> cursorCantactsByPhoneNumber = Telephony
				._getContactsByPhoneNumber(cr, phoneNumber);

		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_contactsByPhoneNumber);
		js.array();
		for (String[] contactsValue : cursorCantactsByPhoneNumber) {
			js.array();
			for (String value : contactsValue) {
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		js.endObject();
		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));
	}

	public Message getConversations(Message request) throws JSONException {
		String resp_conversations = "conversations";
		ArrayList<Object[]> cursorConversation = Telephony
				._getConversations(cr);
		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_conversations);
		js.array();

		for (Object[] contentValue : cursorConversation) {
			js.object();

			js.key("infos");
			js.array();
			String[] infos = (String[]) contentValue[0];
			for (String info : infos) {
				js.value(info);
			}
			js.endArray();

			js.key("contactInfos");
			js.array();
			ArrayList<String[]> contactInfos = (ArrayList<String[]>) contentValue[1];
			for (String[] contactInfo : contactInfos) {
				js.array();
				for (String info : contactInfo) {
					js.value(info);
				}
				js.endArray();
			}
			js.endArray();

			js.endObject();
		}
		js.endArray();
		js.endObject();
		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));
	}

	public Message delConversations(Message request) throws JSONException {
		String req_conversationId = "conversationId";
		String resp_count = "count";

		String conversationId = request.getContent().getString(
				req_conversationId);

		int count = Telephony._delConversation(cr, conversationId);

		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_count).value(count);
		js.endObject();
		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));

	}

	public Message getMessages(Message request) throws JSONException {

		String req_conversationId = "conversationId";

		String resp_messages = "messages";

		List<String[]> cursorMessage = null;

		String conversationId = request.getContent().getString(
				req_conversationId);

		cursorMessage = Telephony._getMessages(cr, conversationId);

		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_messages);
		js.array();

		for (String[] contentValue : cursorMessage) {
			js.array();
			for (String value : contentValue) {
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		js.endObject();
		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));

	}

	// wu0wu
	public Message getMmsPart(Message request) throws JSONException {
		String req_mmsId = "mmsId";

		String resp_MmsPart = "mmsPart";

		List<String[]> cursorMmsPart = null;

		String mmsId = request.getContent().getString(req_mmsId);

		cursorMmsPart = Telephony._getMmsPart(cr, mmsId);

		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_MmsPart);
		js.array();

		for (String[] contentValue : cursorMmsPart) {
			js.array();
			for (String value : contentValue) {
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		js.endObject();
		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));
	}

	// wu0wu
	public Message sendSmsMessage(Message request) throws JSONException {
		String req_number = "numbers";
		String req_smsBody = "body";
		JSONArray json_numbers = request.getContent().getJSONArray(req_number);
		List<String> numbers = Utils.getStringListFromJSONArray(json_numbers);
		// String number = request.getContent().getString(req_number);
		String smsBody = request.getContent().getString(req_smsBody);

		Telephony._sendSmsMessage(context, numbers, smsBody);
		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject());
	}

	// wu0wu
	public Message delSmsMessage(Message request) throws JSONException {

		String req_SmsId = "smsIds";
		JSONArray smsIds = request.getContent().getJSONArray(req_SmsId);
		List<String> ids = Utils.getStringListFromJSONArray(smsIds);
		int count = Telephony._delSmsMessage(cr, ids);

		JSONStringer js = new JSONStringer();
		js.object();
		js.key("count").value(count);
		js.endObject();

		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));
	}

	public void delMmsMessage(Message request) {

		String req_MmsId = "mmsIds";
		try {
			JSONArray mmsIds = request.getContent().getJSONArray(req_MmsId);
			List<String> ids = Utils.getStringListFromJSONArray(mmsIds);
			Telephony._delMmsMessage(cr, ids);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Message getCallLogs(Message request) throws JSONException {

		String req_phoneNumber = "phoneNumber";

		String resp_calls = "calls";

		String phoneNumber = request.getContent().getString(req_phoneNumber);
		List<String[]> cursorCallLog = Telephony._getCallLogs(cr, phoneNumber);

		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_calls);
		js.array();
		for (String[] contentValue : cursorCallLog) {
			js.array();
			for (String value : contentValue) {
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		js.endObject();
		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));

	}

	public Message removeCallLogs(Message request) throws JSONException {
		String req_ids = "ids";
		String resp_count = "count";

		JSONArray ids = request.getContent().getJSONArray(req_ids);
		int count = 0;
		int len = ids.length();
		ArrayList<String> ids2 = new ArrayList<String>(len);
		for (int i = 0; i < len; i++) {
			ids2.add(ids.getString(i));
		}
		count = Telephony._removeCallLogs(cr, ids2);

		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_count).value(count);
		js.endObject();

		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));
	}

	/**
	 * wu0wu 将HashMap<String, String[]>的Sring[]包装起来
	 * 
	 * @throws JSONException
	 */
	public Message getContacts(Message request) throws JSONException {
		String resp_contacts = "contacts";
		String resp_info = "info";
		String resp_phones = "phones";
		HashMap<String, Object[]> hm = Telephony._getContacts(cr);

		Iterator<Entry<String, Object[]>> iterator = hm.entrySet().iterator();

		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_contacts);
		js.array();
		while (iterator.hasNext()) {
			Entry<String, Object[]> entry = iterator.next();
			Object[] array = entry.getValue();
			String[] infos = (String[]) array[0];

			// info
			js.object();
			js.key(resp_info);

			js.array();
			for (int i = 0; i < Telephony.PROJECTION_CONTACTS.length + 1; i++) {
				js.value(infos[i]);
			}

			js.endArray();

			js.key(resp_phones);

			js.array();
			ArrayList<String[]> phones = (ArrayList<String[]>) array[1];
			for (int i = 0; i < phones.size(); i++) {
				js.array();
				for (int j = 0; j < 5; j++) {
					String[] tempPhones = phones.get(i);
					js.value(tempPhones[j]);
				}
				js.endArray();
			}
			js.endArray();

			js.endObject();
		}
		js.endArray();
		js.endObject();

		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));
	}

	public Message delContacts(Message request) {

		String req_contactIds = "rawIds";

		String resp_deleted = "deleted";

		Cursor cursorId = null;

		try {

			JSONArray jsRawIds = request.getContent().getJSONArray(
					req_contactIds);

			int len = jsRawIds.length();
			ArrayList<String> ids2 = new ArrayList<String>(len);
			for (int i = 0; i < len; i++) {
				ids2.add(jsRawIds.getString(i));
			}

			if (ids2.size() > 0) {
				int count = Telephony._delContacts(cr, ids2);
				JSONStringer js = new JSONStringer();
				js.object();
				js.key(resp_deleted).value(count);
				js.endObject();

				return Message.getResponseMessage(request.getRegion(), request
						.getCommand(), true, new JSONObject(js.toString()));
			} else {
				return Message.getResponseErrorMessage(request,
						"nothing deleted");
			}

		} catch (Exception e) {
			return Message.getResponseErrorMessage(request, e.toString());
		} finally {
			if (cursorId != null) {
				cursorId.close();
			}
		}
	}

	/** 根据ContactId获取联系人详细信息 */
	public Message getContactByContactId(Message request) throws JSONException {
		String req_contactId = "contactId";
		String resp_contactUnit = "contactByContactId";
		String resp_email = "email";
		String resp_displayName = "displayName";
		String resp_phones = "phones";
		String resp_im = "im";
		String resp_address = "address";
		String resp_organization = "organization";
		String resp_note = "note";
		String resp_nikename = "nikename";
		String resp_website = "website";
		// 参数contactId
		String contactId = request.getContent().getString(req_contactId);

		ModelContact cU = Telephony._getContactByContactId(cr, contactId);
		JSONStringer js = new JSONStringer();
		js.object();
		js.key(resp_contactUnit);

		js.object();
		// 包displayName
		ArrayList<String[]> displayName = cU.getArray_Displayname();
		js.key(resp_displayName).value(displayName.get(0)[0]);

		// 包phones
		js.key(resp_phones);
		js.array();
		ArrayList<String[]> phones = cU.getArray_phone();
		for (int i = 0; i < phones.size(); i++) {
			String[] phone = phones.get(i);
			js.array();
			for (String value : phone) {
				Log.e(TAG, "value=" + value);
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		// 包Phones end

		// 包email
		js.key(resp_email);
		js.array();
		ArrayList<String[]> emails = cU.getArray_email();
		for (int i = 0; i < emails.size(); i++) {
			String[] email = emails.get(i);
			js.array();
			for (String value : email) {
				Log.e(TAG, "value=" + value);
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		// 包email end

		// 包IM
		js.key(resp_im);
		js.array();
		ArrayList<String[]> ims = cU.getArray_Im();
		for (int i = 0; i < ims.size(); i++) {
			String[] im = ims.get(i);
			js.array();
			for (String value : im) {
				Log.e(TAG, "value=" + value);
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		// 包IM end

		// 包address
		js.key(resp_address);
		js.array();
		ArrayList<String[]> addresses = cU.getArray_Address();
		for (int i = 0; i < addresses.size(); i++) {
			String[] address = addresses.get(i);
			js.array();
			for (String value : address) {
				Log.e(TAG, "value=" + value);
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		// 包address end

		// 包organization
		js.key(resp_organization);
		js.array();
		ArrayList<String[]> organizations = cU.getArray_Organization();
		for (int i = 0; i < organizations.size(); i++) {
			String[] organization = organizations.get(i);
			js.array();
			for (String value : organization) {
				Log.e(TAG, "value=" + value);
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		// 包organization end

		// 包NOTE 唯一（自定义：虚拟器上可多个）
		ArrayList<String[]> notes = cU.getArray_Notes();
		js.key(resp_note).value(notes.get(0)[0]);
		// 包NOTE end

		// 包nikeName 唯一（自定义：虚拟器上可多个）
		js.key(resp_nikename);
		ArrayList<String[]> nikenames = cU.getArray_Nicknames();
		js.array();
		String[] nikename = nikenames.get(0);
		for (String value : nikename) {
			Log.e(TAG, "value=" + value);
			js.value(value);
		}
		js.endArray();
		// 包nikeName end

		// 包website
		js.key(resp_website);
		js.array();
		ArrayList<String[]> websites = cU.getArray_Websites();
		for (int i = 0; i < websites.size(); i++) {
			String[] website = websites.get(i);
			js.array();
			for (String value : website) {
				Log.e(TAG, "value=" + value);
				js.value(value);
			}
			js.endArray();
		}
		js.endArray();
		// 包website end
		js.endObject();
		js.endObject();

		return Message.getResponseMessage(request.getRegion(), request
				.getCommand(), true, new JSONObject(js.toString()));
	}

	/**
	 * 注意：ArrayList<String[]> 暂时用String拼的
	 * 
	 * */
	public Message insertContact(Message request) throws JSONException {
		String req_phones = "phones";
		String jsPhones = request.getContent().getString(req_phones);//
		ArrayList<String[]> phones = Utils
				.getArrayList2StringFromString(jsPhones);
		return null;
	}
}
