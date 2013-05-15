package com.otheri.comm4and.api;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.RawContacts;
import android.telephony.SmsMessage;
import android.util.Log;

import com.otheri.comm4and.consts.CanonicalAddressesColumns;

/**
 * 
 * @author cloud
 * 
 *         系统环境，包含一些常用接口。 每次启动，只会被调用一次。
 */
public class Environment {

	private static final String TAG = "Environment";

	/**
	 * 会话表（threads）里的recipient_ids表示 参与会话的联系人（如果一个会话有多个联系人，则该字段中的id按空格分隔），
	 * recipient_ids表中的id值对应canonical_address表中的id。
	 * canonical_address表中的address则对应联系人的电话号码。
	 * 
	 */
	private static HashMap<String, String> canonicalAddress;

	public static HashMap<String, String> _getCanonicalAddress(
			ContentResolver cr) {
		if (canonicalAddress == null) {
			canonicalAddress = new HashMap<String, String>();
			canonicalAddress = _initCanonicalAddress(cr);
		}
		return canonicalAddress;
	}

	private static HashMap<String, String> _initCanonicalAddress(
			ContentResolver cr) {
		HashMap<String, String> ret = new HashMap<String, String>();
		Cursor cursorCanonicalAddress = null;
		try {
			cursorCanonicalAddress = cr.query(
					Uri.parse("content://mms-sms/canonical-addresses"),
					new String[] { CanonicalAddressesColumns._ID,
							CanonicalAddressesColumns.ADDRESS }, null, null,
					null);

			ret = new HashMap<String, String>();

			int indexId = cursorCanonicalAddress
					.getColumnIndex(CanonicalAddressesColumns._ID);
			int indexAddress = cursorCanonicalAddress
					.getColumnIndex(CanonicalAddressesColumns.ADDRESS);
			while (cursorCanonicalAddress.moveToNext()) {
				ret.put(cursorCanonicalAddress.getString(indexId),
						cursorCanonicalAddress.getString(indexAddress));
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorCanonicalAddress != null) {
				cursorCanonicalAddress.close();
			}
		}
		return ret;
	}

	private static HashMap<String, String> contactIdAndRawContactIds;

	public static HashMap<String, String> _getContactIdByRawContactId(
			ContentResolver cr) {
		if (contactIdAndRawContactIds == null) {
			contactIdAndRawContactIds = new HashMap<String, String>();
			contactIdAndRawContactIds = _initContactIdByRawContactId(cr);
		}
		return contactIdAndRawContactIds;
	}

	private static HashMap<String, String> _initContactIdByRawContactId(
			ContentResolver cr) {
		HashMap<String, String> ret = new HashMap<String, String>();
		Cursor cursorRawContact = null;
		try {
			cursorRawContact = cr.query(RawContacts.CONTENT_URI,
					Telephony.PROJECTION_RAW_CONTACT, null, null, null);
			int indexContactId = cursorRawContact
					.getColumnIndex(RawContacts.CONTACT_ID);
			int indexRawContactId = cursorRawContact
					.getColumnIndex(RawContacts._ID);
			while (cursorRawContact.moveToNext()) {
				ret.put(cursorRawContact.getString(indexContactId),
						cursorRawContact.getString(indexRawContactId));
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			if (cursorRawContact != null) {
				cursorRawContact.close();
			}
		}
		return ret;
	}

	private static ArrayList<SmsMessage[]> messageList = new ArrayList<SmsMessage[]>();

	public static void _receiveSMSMessage(SmsMessage[] smss) {
		messageList.add(smss);
		events[EVENT_RECEIVE_SMS][1] = EVENT_TRUE;
	}

	public static final int EVENT_RECEIVE_SMS = 0;
	public static final int EVENT_RECEIVE_MMS = 1;

	private static final String EVENT_TRUE = "true";
	private static final String EVENT_FALSE = "false";

	/**
	 * 事件二维数组，第一列表示事件名，如sms,mms 第二列表示是否有事件，true或者false，目前只有
	 */
	private static String[][] events = { { "sms", EVENT_FALSE },
			{ "mms", EVENT_FALSE } };

	public static String[][] _heartbeat() {
		return events;
	}
}
