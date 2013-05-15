package com.otheri.comm4and.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.telephony.SmsManager;
import android.util.Log;

import com.otheri.assistant.remote.AssistantService.MessageReceiver;
import com.otheri.comm.Utils;
import com.otheri.comm4and.consts.BaseMmsColumns;
import com.otheri.comm4and.consts.Part;
import com.otheri.comm4and.consts.TextBasedSmsColumns;
import com.otheri.comm4and.consts.ThreadsColumns;
import com.otheri.comm4and.model.ModelContact;

public class Telephony {

	private static final String TAG = "Telephony";

	/**
	 * 按电话号码查询联系人 联系人模块中，推荐使用Lookup系列的api来筛选联系人。
	 * 返回一个字符串数组序列，其中，数组第一位是RawId，第二位是显示名称（DisplayName），第三位是头像编号（PhotoID）
	 */
	public static List<String[]> _getContactsByPhoneNumber(ContentResolver cr,
			String phoneNumber) {
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri
				.encode(phoneNumber));
		Cursor cursorContacts = null;
		ArrayList<String[]> ret = new ArrayList<String[]>();
		try {
			cursorContacts = cr.query(uri, new String[] { PhoneLookup._ID,
					PhoneLookup.DISPLAY_NAME, PhoneLookup.PHOTO_ID }, null,
					null, null);
			int indexId = cursorContacts.getColumnIndex(PhoneLookup._ID);
			int indexDisplayName = cursorContacts
					.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			int indexPhotoId = cursorContacts
					.getColumnIndex(PhoneLookup.PHOTO_ID);
			while (cursorContacts.moveToNext()) {
				String id = cursorContacts.getString(indexId);
				String displayName = cursorContacts.getString(indexDisplayName);
				String photoId = cursorContacts.getString(indexPhotoId);
				ret.add(new String[] { id, Utils.fixNull(displayName),
						Utils.fixNull(photoId) });
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorContacts != null) {
				cursorContacts.close();
			}
		}
		return ret;
	}

	public static final String[] PROJECTION_CONVERSATION = new String[] {
			ThreadsColumns._ID, ThreadsColumns.DATE,
			ThreadsColumns.MESSAGE_COUNT, ThreadsColumns.RECIPIENT_IDS,
			ThreadsColumns.SNIPPET, ThreadsColumns.SNIPPET_CHARSET,
			ThreadsColumns.READ };

	/**
	 * 获取会话列表
	 * 
	 * @return HashMap key为ConversationId, Object[]为会话信息以及联系人及电话信息
	 */
	public static ArrayList<Object[]> _getConversations(ContentResolver cr) {
		ArrayList<Object[]> ret = new ArrayList<Object[]>();
		Cursor cursorConversation = null;
		HashMap<String, String> canonicalAddresses = Environment
				._getCanonicalAddress(cr);
		try {
			cursorConversation = cr.query(Uri
					.parse("content://mms-sms/conversations?simple=true"),
					PROJECTION_CONVERSATION, null, null, null);
			int[] indexs = Utils.getColumnIndexs(PROJECTION_CONVERSATION,
					cursorConversation);
			while (cursorConversation.moveToNext()) {
				String[] contentValues = new String[PROJECTION_CONVERSATION.length];// 需要join两项
				String[] recipientIds = null;
				for (int i = 0; i < PROJECTION_CONVERSATION.length; i++) {
					String value = cursorConversation.getString(indexs[i]);
					contentValues[i] = value;
					if (PROJECTION_CONVERSATION[i]
							.equals(ThreadsColumns.RECIPIENT_IDS)) {
						recipientIds = value.split(" ");
					}
				}
				ArrayList<String[]> contactInfos = new ArrayList<String[]>();
				if (recipientIds != null && recipientIds.length > 0) {
					for (int i = 0; i < recipientIds.length; i++) {
						// address,rowId,displayName,photoId
						String[] temp = { "", "", "", "" };
						String address = canonicalAddresses
								.get(recipientIds[i]);
						temp[0] = address;
						if (address != null) {
							// rowId,displayName,photoId
							List<String[]> contacts = _getContactsByPhoneNumber(
									cr, address);
							if (contacts != null && contacts.size() > 0) {
								int len = contacts.size();
								for (int j = 0; j < len; j++) {
									String[] contact = contacts.get(j);
									temp[1] = contact[0];
									temp[2] = contact[1];
									temp[3] = contact[2];
								}
							}
						}
						contactInfos.add(temp);
					}
				}
				ret.add(new Object[] { contentValues, contactInfos });
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorConversation != null) {
				cursorConversation.close();
			}
		}
		return ret;
	}

	/**
	 * 
	 * 根据会话编号conversionId删除会话
	 */
	public static int _delConversation(ContentResolver cr, String conversationId) {
		int count = cr.delete(Uri.parse("content://mms-sms/conversations/"
				+ conversationId), null, null);
		return count;
	}

	public static final String[] PROJECTION_CONVERSATION_SMS = {
			TextBasedSmsColumns._ID, TextBasedSmsColumns.THREAD_ID,
			TextBasedSmsColumns.TYPE, TextBasedSmsColumns.SUBJECT,
			TextBasedSmsColumns.BODY, TextBasedSmsColumns.DATE,
			TextBasedSmsColumns.READ };

	public static final String[] PROJECTION_CONVERSATION_MMS = {
			BaseMmsColumns._ID, BaseMmsColumns.THREAD_ID,
			BaseMmsColumns.MESSAGE_BOX, BaseMmsColumns.SUBJECT,
			BaseMmsColumns.SUBJECT_CHARSET, BaseMmsColumns.DATE,
			BaseMmsColumns.READ };

	/**
	 * 修正日期问题，彩信的时间精确到秒，而短信是毫秒，所以排序时会有问题，需要用此函统一将精度修正到秒
	 */
	private static String fixDate(String date) {
		char[] tempChar = new char[13];
		int len = date.length();
		if (len < 13) {
			// 补零
			for (int ind = 0; ind < tempChar.length; ind++) {
				if (ind < len) {
					tempChar[ind] = date.charAt(ind);
				} else {
					tempChar[ind] = '0';
				}
			}
			return new String(tempChar);
		}
		return date;
	}

	/**
	 * 根据会话编号conversationId获取全部对话
	 */
	public static List<String[]> _getMessages(ContentResolver cr,
			String conversationId) {
		// 获取短信
		ArrayList<String[]> smsList = new ArrayList<String[]>();
		Cursor cursorConversationSms = null;
		try {
			cursorConversationSms = cr.query(Uri
					.parse("content://sms/conversations/" + conversationId),
					Telephony.PROJECTION_CONVERSATION_SMS, null, null, null);

			int[] indexs = Utils.getColumnIndexs(PROJECTION_CONVERSATION_SMS,
					cursorConversationSms);
			while (cursorConversationSms.moveToNext()) {
				String[] contentValues = new String[Telephony.PROJECTION_CONVERSATION_SMS.length + 1];
				for (int i = 0; i < PROJECTION_CONVERSATION_SMS.length; i++) {
					String value = cursorConversationSms.getString(indexs[i]);
					if (PROJECTION_CONVERSATION_SMS[i]
							.equals(TextBasedSmsColumns.DATE)) {
						value = fixDate(value);
					}
					contentValues[i] = value;
				}
				contentValues[Telephony.PROJECTION_CONVERSATION_SMS.length + 0] = "sms";
				smsList.add(contentValues);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorConversationSms != null) {
				cursorConversationSms.close();
			}
		}

		// 获取彩信
		ArrayList<String[]> mmsList = new ArrayList<String[]>();
		Cursor cursorConversationMms = null;
		try {
			cursorConversationMms = cr.query(Uri.parse("content://mms"),
					PROJECTION_CONVERSATION_MMS, BaseMmsColumns.THREAD_ID
							+ "=?", new String[] { conversationId }, null);

			int[] indexs = Utils.getColumnIndexs(PROJECTION_CONVERSATION_MMS,
					cursorConversationMms);
			while (cursorConversationMms.moveToNext()) {
				String[] contentValues = new String[PROJECTION_CONVERSATION_MMS.length + 1];
				for (int i = 0; i < PROJECTION_CONVERSATION_MMS.length; i++) {
					String value = cursorConversationMms.getString(indexs[i]);
					if (PROJECTION_CONVERSATION_MMS[i]
							.equals(BaseMmsColumns.DATE)) {
						value = fixDate(value);
					}
					contentValues[i] = value;
				}
				contentValues[PROJECTION_CONVERSATION_MMS.length + 0] = "mms";
				mmsList.add(contentValues);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorConversationMms != null) {
				cursorConversationMms.close();
			}
		}

		// 合并短信和彩信列表，并排序
		ArrayList<String[]> ret = smsList;
		ret.addAll(mmsList);

		Collections.sort(ret, new Comparator<String[]>() {
			@Override
			public int compare(String[] c1, String[] c2) {
				try {
					long l1 = Long.parseLong(c1[5]);
					long l2 = Long.parseLong(c2[5]);
					return (l1 < l2 ? 1 : (l1 == l2 ? 0 : -1));
				} catch (Exception e) {
					return 0;
				}
			}
		});
		return ret;
	}

	public static final String[] PROJECTION_PART = new String[] { Part._ID,
			Part.CONTENT_TYPE, Part.CONTENT_ID, Part.TEXT, Part._DATA };

	/**
	 * 根据彩信编号mmsId获取彩信模块part
	 */
	public static List<String[]> _getMmsPart(ContentResolver cr, String mmsId) {
		ArrayList<String[]> ret = new ArrayList<String[]>();
		Cursor cursorMmsPart = null;
		try {
			cursorMmsPart = cr.query(Uri.parse("content://mms/" + mmsId
					+ "/part"), PROJECTION_PART, null, null, null);
			int[] indexs = Utils
					.getColumnIndexs(PROJECTION_PART, cursorMmsPart);
			while (cursorMmsPart.moveToNext()) {
				String[] contentValues = new String[PROJECTION_PART.length];
				for (int i = 0; i < PROJECTION_PART.length; i++) {
					String value = cursorMmsPart.getString(indexs[i]);
					contentValues[i] = value;
				}
				ret.add(contentValues);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorMmsPart != null) {
				cursorMmsPart.close();
			}
		}
		return ret;
	}

	/**
	 * 发送短信,含群发
	 * 
	 * @param number
	 *            电话号码
	 * @param content
	 *            短信内容
	 */
	public static void _sendSmsMessage(Context context, List<String> numbers,
			String content) {
		int count = 0;
		for (int i = 0; i < numbers.size(); i++) {
			SmsManager smsManager = SmsManager.getDefault();
			Intent sendIntent = new Intent(MessageReceiver.ACTION_SMS_SENDED);
			sendIntent.putExtra("num", numbers.get(i));
			sendIntent.putExtra("content", content);

			PendingIntent sentPendingIntent = PendingIntent.getBroadcast(
					context, count, sendIntent, count);
			smsManager.sendTextMessage(numbers.get(i), null, content,
					sentPendingIntent, null);
			count++;
		}
	}

	/**
	 * 根据短信编号列表，删除短信
	 */
	public static int _delSmsMessage(ContentResolver cr, List<String> ids) {
		if (ids == null || ids.size() == 0)
			return 0;
		StringBuilder where = new StringBuilder(TextBasedSmsColumns._ID);
		where = Utils.appendWhereInString(ids, where);
		return cr.delete(Uri.parse("content://sms"), where.toString(), null);

	}

	public static void _sendMmsMessage() {
		// 暂时不做，比较复杂，得考虑界面怎么生成参数
	}

	/**
	 * 根据彩信编号列表，删除彩信
	 */
	public static void _delMmsMessage(ContentResolver cr, List<String> ids) {
		if (ids == null || ids.size() == 0)
			return;
		StringBuilder where = new StringBuilder(BaseMmsColumns._ID);
		where = Utils.appendWhereInString(ids, where);
		cr.delete(Uri.parse("content://mms"), where.toString(), null);
	}

	public static final String[] PROJECTION_CALLLOG = new String[] { Calls._ID,
			Calls.NUMBER, Calls.DATE, Calls.DURATION, Calls.TYPE,
			Calls.CACHED_NAME };

	/**
	 * 根据电话号码获取通话记录，如果电话号码为空或者等于“”，则返回全部通话记录
	 */
	public static List<String[]> _getCallLogs(ContentResolver cr,
			String phoneNumber) {
		Cursor cursorCallLog = null;
		ArrayList<String[]> ret = new ArrayList<String[]>();
		try {
			if (phoneNumber != null && !phoneNumber.trim().equals("")) {
				cursorCallLog = cr.query(Uri.withAppendedPath(
						Calls.CONTENT_FILTER_URI, phoneNumber.trim()),
						PROJECTION_CALLLOG, null, null, null);
			} else {
				cursorCallLog = cr.query(Calls.CONTENT_URI, PROJECTION_CALLLOG,
						null, null, null);
			}
			int[] indexs = Utils.getColumnIndexs(PROJECTION_CALLLOG,
					cursorCallLog);
			while (cursorCallLog.moveToNext()) {
				String[] contentValue = new String[PROJECTION_CALLLOG.length];
				for (int i = 0; i < PROJECTION_CALLLOG.length; i++) {
					String value = cursorCallLog.getString(indexs[i]);
					contentValue[i] = value;
				}
				ret.add(contentValue);
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorCallLog != null) {
				cursorCallLog.close();
			}
		}
		return ret;
	}

	/**
	 * 根据通话记录编号删除通话记录，如果编号为空或等于“”，则全部清空
	 */
	public static int _removeCallLogs(ContentResolver cr, List<String> ids)
			throws JSONException {
		int count = 0;
		if (ids != null && ids.size() > 0) {
			StringBuilder where = new StringBuilder(Calls._ID);
			where = Utils.appendWhereInString(ids, where);
			count = cr.delete(Calls.CONTENT_URI, where.toString(), null);
		} else {
			// 全部清空
			count = cr.delete(Calls.CONTENT_URI, null, null);
		}
		return count;
	}

	public static final String[] PROJECTION_RAW_CONTACT = new String[] {
			RawContacts._ID, RawContacts.CONTACT_ID };

	/** 根据RawContactId查ContactId */
	private static String _getContactIdByRawContactId(ContentResolver cr,
			String rawContactId) {
		Cursor c = null;
		try {
			c = cr.query(RawContacts.CONTENT_URI, PROJECTION_RAW_CONTACT,
					RawContacts._ID + "=?", new String[] { String
							.valueOf(rawContactId) }, null);
			c.moveToFirst();
			String contactId = c.getString(c
					.getColumnIndex(RawContacts.CONTACT_ID));
			return contactId;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return "-1";
	}

	public static final String[] PROJECTION_CONTACTS = { Contacts._ID,
			Contacts.PHOTO_ID, Contacts.IN_VISIBLE_GROUP,
			Contacts.HAS_PHONE_NUMBER, Contacts.DISPLAY_NAME,
			Contacts.CUSTOM_RINGTONE };

	/**
	 * wu0wu
	 * 
	 * 功能：查询所有联系人PROJECTION_CONTACTS信息，Phones
	 * 
	 * @return HashMap key为ContactId, Object[]为info[]+Phone[]信息
	 * */
	public static HashMap<String, Object[]> _getContacts(ContentResolver cr) {
		Cursor cursorContact = null;
		HashMap<String, Object[]> ret = new HashMap<String, Object[]>();
		try {

			HashMap<String, String> contactIdAndRawContactIds = Environment
					._getContactIdByRawContactId(cr);

			cursorContact = cr.query(ContactsContract.Contacts.CONTENT_URI,
					PROJECTION_CONTACTS, Contacts.IN_VISIBLE_GROUP + "=1",
					null, null);

			int[] indexs = Utils.getColumnIndexs(PROJECTION_CONTACTS,
					cursorContact);
			ArrayList<String[]> arrayInfos = new ArrayList<String[]>();// 存放info信息
			/* 把全部info信息读出来，放入mapInfos,Contacts._ID为key */
			while (cursorContact.moveToNext()) {
				// 单条info信息
				String[] infoValue = new String[PROJECTION_CONTACTS.length + 1];

				for (int i = 0; i < PROJECTION_CONTACTS.length; i++) {
					String value = cursorContact.getString(indexs[i]);
					infoValue[i] = (value == null ? "" : value);
				}

				// 加上RawContactId
				infoValue[PROJECTION_CONTACTS.length] = contactIdAndRawContactIds
						.get(infoValue[0]);

				arrayInfos.add(infoValue);
			}
			/*
			 * 从mapInfos取出所有info的Contacts._ID，Contacts.HAS_PHONE_NUMBER,把
			 * Contacts.HAS_PHONE_NUMBER标记为1的所有Contacts._ID放入一个ArrayList<String>
			 */
			ArrayList<String> contactIds = new ArrayList<String>();
			//
			Iterator<String[]> it_mapInfos = arrayInfos.iterator();
			while (it_mapInfos.hasNext()) {
				String[] _info = it_mapInfos.next();
				if (_info[3].equals("1")) {// 1=有；0=无
					contactIds.add(_info[0]);
				}
			}

			HashMap<String, ArrayList<String[]>> phones = getContactsPhonesNumber(
					cr, contactIds);
			/*
			 * 遍历arrayInfos中的Contacts._ID分别从arrayInfos，phones的HashMap中查出数据放入Object数组
			 */
			Iterator<String[]> it_arrayInfos2 = arrayInfos.iterator();
			while (it_arrayInfos2.hasNext()) {
				String[] _info2 = it_arrayInfos2.next();
				if (_info2[3].equals("1")) {
					ArrayList<String[]> phonesList = phones.get(_info2[0]);
					Object[] objs = { _info2, phonesList };
					ret.put(_info2[0], objs);
				} else {
					ArrayList<String[]> phonesList = new ArrayList<String[]>();
					Object[] objs = { _info2, phonesList };
					ret.put(_info2[0], objs);
				}
			}

		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorContact != null) {
				cursorContact.close();
			}
		}
		return ret;
	}

	public static final String[] PROJECTION_PHONE_CONTACT = new String[] {
			Data._ID, Data.CONTACT_ID, Phone.NUMBER, Phone.TYPE, Phone.LABEL };

	/**
	 * wu0wu
	 * 
	 * 功能：根据contactId数组，查询数组中所有ID的phone信息
	 * 
	 * 
	 * @return HashMap key为ContactId, ArrayList<String[]>为Phone信息
	 * */
	private static HashMap<String, ArrayList<String[]>> getContactsPhonesNumber(
			ContentResolver cr, ArrayList<String> contactIds) {
		Cursor c = null;
		HashMap<String, ArrayList<String[]>> ret = new HashMap<String, ArrayList<String[]>>();
		try {

			StringBuilder where = new StringBuilder();
			where.append(Data.CONTACT_ID);
			where = Utils.appendWhereInString(contactIds, where);
			where.append(" AND ");
			where.append(Data.MIMETYPE);
			where.append("='");
			where.append(Phone.CONTENT_ITEM_TYPE);
			where.append("'");

			c = cr.query(Data.CONTENT_URI, PROJECTION_PHONE_CONTACT, where
					.toString(), null, null);

			int[] indexs = Utils.getColumnIndexs(PROJECTION_PHONE_CONTACT, c);
			while (c.moveToNext()) {
				String[] data = new String[5];
				for (int i = 0; i < PROJECTION_PHONE_CONTACT.length; i++) {
					String value = c.getString(indexs[i]);
					data[i] = (value == null ? "" : value);
				}

				ArrayList<String[]> temp = ret.get(data[1]);
				if (temp == null) {
					temp = new ArrayList<String[]>();
					temp.add(data);

					// 放到结果集
					ret.put(data[1], temp);
				} else {
					temp.add(data);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return ret;
	}

	public static int _delContacts(ContentResolver cr, List<String> ridList) {
		StringBuilder sb = new StringBuilder(RawContacts._ID);
		sb = Utils.appendWhereInString(ridList, sb);
		int count = cr.delete(RawContacts.CONTENT_URI, sb.toString(), null);
		return count;
	}

	/**
	 * 功能:新建联系人（单个）
	 * 
	 * 可以是只有displayName的contact
	 * 
	 * @param String
	 *            :displayName
	 * 
	 * @return long: rawContactId
	 * 
	 * 
	 *         返回插入的rawContact id，用这个id再调用updateContact接口完成插入完整contact数据
	 * */
	private static long insertRawContact(ContentResolver cr, String displayName) {
		if (displayName != "") {
			ContentValues values = new ContentValues();
			Uri rawContactUri = cr.insert(RawContacts.CONTENT_URI, values);
			long rawContactId = ContentUris.parseId(rawContactUri);

			values.clear();
			values.put(Data.RAW_CONTACT_ID, rawContactId);
			values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
			values.put(StructuredName.DISPLAY_NAME, displayName);
			cr.insert(Data.CONTENT_URI, values);
			return rawContactId;
		}
		return -1;
	}

	/**
	 * 快速新建联系人
	 * 
	 * @param displayName
	 *            string 姓名
	 * @param mobilePhone
	 *            string 手机号
	 * @return contactId
	 * */
	public static String _flashInsertContact(ContentResolver cr,
			String displayName, String mobilePhone) {
		long rawContactId = insertRawContact(cr, displayName);

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValue(
				Data.RAW_CONTACT_ID, rawContactId).withValue(Data.MIMETYPE,
				Phone.CONTENT_ITEM_TYPE).withValue(Phone.NUMBER, mobilePhone)
				.withValue(Phone.TYPE, Phone.TYPE_MOBILE).build());
		try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (RemoteException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}
		return _getContactIdByRawContactId(cr, String.valueOf(rawContactId));
	}

	private static final String[] PROJECTION_PHONENUMBER_CONTACT = {
			Phone.NUMBER, Phone.TYPE, Phone.LABEL };
	/* DISPLAY_NAME唯一性 */
	private static final String[] PROJECTION_DISPLAYNAME_CONTACT = { StructuredName.DISPLAY_NAME };
	private static final String[] PROJECTION_EAMIL_CONTACT = { Email.DATA1,
			Email.TYPE, Email.LABEL };
	private static final String[] PROJECTION_IM_CONTACT = new String[] {
			Im.DATA, Im.TYPE, Im.LABEL, Im.PROTOCOL };
	private static final String[] PROJECTION_ADDRESS_CONTACT = new String[] {
			StructuredPostal.STREET, StructuredPostal.CITY,
			StructuredPostal.REGION, StructuredPostal.POSTCODE,
			StructuredPostal.COUNTRY, StructuredPostal.TYPE,
			StructuredPostal.LABEL, StructuredPostal.POBOX,
			StructuredPostal.NEIGHBORHOOD, };
	private static final String[] PROJECTION_ORGANIZATION_CONTACT = new String[] {
			Organization.COMPANY, Organization.TYPE, Organization.LABEL,
			Organization.TITLE };

	private static final String[] PROJECTION_NOTES_CONTACT = new String[] { Note.NOTE };
	private static final String[] PROJECTION_NICKNAMES_CONTACT = new String[] {
			Nickname.NAME, Nickname.TYPE, Nickname.LABEL };
	private static final String[] PROJECTION_WEBSITES_CONTACT = new String[] {
			Website.URL, Website.TYPE, Website.LABEL };

	/**
	 * 功能：根据contactId查询联系人详细
	 * */
	public static ModelContact _getContactByContactId(ContentResolver cr,
			String contactId) {
		ModelContact cU = new ModelContact();
		Cursor c = null;
		c = cr.query(Data.CONTENT_URI, null, Data.CONTACT_ID + "=?",
				new String[] { contactId }, null);
		String mimeType = null;
		String[] contentValue = null;
		while (c.moveToNext()) {
			mimeType = c.getString(c.getColumnIndex(Data.MIMETYPE));
			if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_DISPLAYNAME_CONTACT);
				cU.addArray_Displayname(contentValue);
			} else if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_PHONENUMBER_CONTACT);
				cU.addArray_phone(contentValue);
			} else if (Email.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_EAMIL_CONTACT);
				cU.addArray_email(contentValue);
			} else if (Im.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_IM_CONTACT);
				cU.addArray_Im(contentValue);
			} else if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_ADDRESS_CONTACT);
				cU.addArray_Address(contentValue);
			} else if (Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_ORGANIZATION_CONTACT);
				cU.addArray_Organization(contentValue);
			} else if (Note.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_NOTES_CONTACT);
				cU.addArray_Notes(contentValue);
			} else if (Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_NICKNAMES_CONTACT);
				cU.addArray_Nicknames(contentValue);
			} else if (Website.CONTENT_ITEM_TYPE.equals(mimeType)) {
				contentValue = getStringInContactCursor(c,
						PROJECTION_WEBSITES_CONTACT);
				cU.addArray_Websites(contentValue);
			}
		}
		c.close();
		return cU;
	}

	private static String[] getStringInContactCursor(Cursor c,
			String[] projection) {
		String[] contentValue = new String[projection.length];
		for (int i = 0; i < contentValue.length; i++) {
			String value = c.getString(c.getColumnIndex(projection[i]));
			if (value == null) {
				contentValue[i] = "";
			} else {
				contentValue[i] = value;
			}
		}
		return contentValue;
	}

	/**
	 * 新建联系人的接口
	 * 
	 * @param String
	 *            accountName，accountType 为账号名账号类型，一般为NULL
	 * @throws RemoteException
	 * @throws OperationApplicationException
	 */

	public static String _insertContact(ContentResolver cr, String accountName,
			String accountType, String displayName, ArrayList<String[]> phone,
			ArrayList<String[]> email, ArrayList<String[]> im,
			ArrayList<String[]> address, ArrayList<String[]> organization,
			ArrayList<String[]> notes, ArrayList<String[]> nickname,
			ArrayList<String[]> website) throws RemoteException,
			OperationApplicationException {

		String rawId = "";
		long rawContactId = insertRawContact(cr, accountName, accountType);
		rawId = Long.toString(rawContactId);
		if (displayName != null) {
			insertContactDisplayname(cr, StructuredName.CONTENT_ITEM_TYPE,
					rawId, displayName);
		}
		if (phone != null) {
			for (int j = 0; j < phone.size(); j++) {
				String[] item = phone.get(j);
				insertItemToContact(cr, Phone.CONTENT_ITEM_TYPE, rawId,
						PROJECTION_PHONENUMBER_CONTACT, item);
			}
		}
		if (email != null) {
			for (int j = 0; j < email.size(); j++) {
				String[] item = email.get(j);
				insertItemToContact(cr, Email.CONTENT_ITEM_TYPE, rawId,
						PROJECTION_EAMIL_CONTACT, item);
			}
		}
		if (im != null) {
			for (int j = 0; j < im.size(); j++) {
				String[] item = im.get(j);
				insertItemToContact(cr, Im.CONTENT_ITEM_TYPE, rawId,
						PROJECTION_IM_CONTACT, item);
			}
		}
		if (address != null) {
			for (int j = 0; j < address.size(); j++) {
				String[] item = address.get(j);
				insertItemToContact(cr, StructuredPostal.CONTENT_ITEM_TYPE,
						rawId, PROJECTION_ADDRESS_CONTACT, item);
			}
		}
		if (organization != null) {
			for (int j = 0; j < organization.size(); j++) {
				String[] item = organization.get(j);
				insertItemToContact(cr, Organization.CONTENT_ITEM_TYPE, rawId,
						PROJECTION_ORGANIZATION_CONTACT, item);
			}
		}
		if (notes != null) {
			for (int j = 0; j < notes.size(); j++) {
				String[] item = notes.get(j);
				insertItemToContact(cr, Note.CONTENT_ITEM_TYPE, rawId,
						PROJECTION_NOTES_CONTACT, item);
			}
		}
		if (nickname != null) {
			for (int j = 0; j < nickname.size(); j++) {
				String[] item = nickname.get(j);
				insertItemToContact(cr, Nickname.CONTENT_ITEM_TYPE, rawId,
						PROJECTION_NICKNAMES_CONTACT, item);
			}
		}
		if (website != null) {
			for (int j = 0; j < website.size(); j++) {
				String[] item = website.get(j);
				insertItemToContact(cr, Website.CONTENT_ITEM_TYPE, rawId,
						PROJECTION_WEBSITES_CONTACT, item);
			}
		}
		return rawId;
	}

	/**
	 * 通过往ROWCONTACT里插入数据，获得rawId
	 * 
	 * @param cr
	 * @param accountName
	 *            一般为NULL
	 * @param accountType
	 *            一般为NULL
	 * @return
	 */

	private static long insertRawContact(ContentResolver cr,
			String accountName, String accountType) {

		ContentValues values = new ContentValues();
		values.put(RawContacts.ACCOUNT_NAME, accountName);
		values.put(RawContacts.ACCOUNT_TYPE, accountType);
		// values.put(Contacts.DISPLAY_NAME, displayName);
		Uri rawContactUri = cr.insert(RawContacts.CONTENT_URI, values);
		long rawContactId = ContentUris.parseId(rawContactUri);
		return rawContactId;
	}

	/**
	 * 更新联系人接口
	 * 
	 * @throws RemoteException
	 * @throws OperationApplicationException
	 */
	public static String _updateContact(ContentResolver cr, String rawId,
			String accountName, String accountType, String displayName,
			ArrayList<String[]> phone, ArrayList<String[]> email,
			ArrayList<String[]> im, ArrayList<String[]> address,
			ArrayList<String[]> organization, ArrayList<String[]> notes,
			ArrayList<String[]> nickname, ArrayList<String[]> website)
			throws RemoteException, OperationApplicationException {
		ArrayList<String> ridList = new ArrayList<String>();
		ridList.add(rawId);
		_delContacts(cr, ridList);
		return _insertContact(cr, accountName, accountType, displayName, phone,
				email, im, address, organization, notes, nickname, website);

	}

	private static void insertContactDisplayname(ContentResolver cr,
			String mimeType, String rawContactId, String displayName)
			throws RemoteException, OperationApplicationException {
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValue(
				Data.MIMETYPE, mimeType).withValue(Data.RAW_CONTACT_ID,
				rawContactId).withValue(StructuredName.DISPLAY_NAME,
				displayName).build());
		cr.applyBatch(ContactsContract.AUTHORITY, ops);
	}

	private static void insertItemToContact(ContentResolver cr,
			String mimeType, String rawContactId, String[] PROJECTION_CONTACT,
			String[] item) throws RemoteException,
			OperationApplicationException {
		ContentValues values = new ContentValues();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, mimeType);
		for (int i = 0; i < PROJECTION_CONTACT.length; i++) {
			values.put(PROJECTION_CONTACT[i], item[i]);
		}
		Uri dataUri = cr.insert(Data.CONTENT_URI, values);
		// ArrayList<ContentProviderOperation> ops = new
		// ArrayList<ContentProviderOperation>();
		// ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValue(
		// Data.MIMETYPE, mimeType).withValue(Data.RAW_CONTACT_ID,
		// rawContactId).withValue(Phone.NUMBER, phoneNumber).withValue(
		// Phone.TYPE, type).withValue(Phone.LABEL, label).build());
		// cr.applyBatch(ContactsContract.AUTHORITY, ops);
	}
}
