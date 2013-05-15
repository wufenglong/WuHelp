package com.otheri.comm;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.database.Cursor;
import android.util.Log;

public class Utils {

	public static List<String> getStringListFromJSONArray(JSONArray smsIds)
			throws JSONException {
		ArrayList<String> ids = new ArrayList<String>();
		if (smsIds == null) {
			return ids;
		} else {
			for (int i = 0; i < smsIds.length(); i++) {
				ids.add(smsIds.getString(i).trim());
			}
			return ids;
		}
	}

	// 解析[[,,],[,,],[,,],[,,]]或[]
	public static ArrayList<String[]> getArrayList2StringFromString(
			String strSrc) {
		ArrayList<String[]> lists = new ArrayList<String[]>();
		// 去掉最外层方括号[]
		int startOutIndex = strSrc.indexOf("[");
		int endOutIndex = strSrc.lastIndexOf("]");
		strSrc = strSrc.substring(startOutIndex + 1, endOutIndex);// [,,],[,,],[,,],[,,]
		// 循环取出每个数组数据
		boolean hasNext = true;
		while (hasNext) {
			int startIndex = strSrc.indexOf("[");
			if (startIndex == -1) {// 结束
				hasNext = false;
				break;
			}
			int endIndex = strSrc.indexOf("]");
			String strSub = strSrc.substring(startIndex + 1, endIndex);// ,,
			int index = endIndex + 2;
			if (index < strSrc.length()) {
				strSrc = strSrc.substring(index);
			} else {
				strSrc = "";
			}
			String[] strArray = strSub.split(",");
			lists.add(strArray);
		}
		return lists;
	}

	public static String fixNull(String str) {
		return str == null ? "" : str;
	}

	public static StringBuilder appendWhereInString(List<String> ids,
			StringBuilder sb) {
		sb.append(" in (");
		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			if (id.equals("")) {
				if (i == ids.size() - 1) {
					sb.deleteCharAt(sb.length() - 1);
				}
			} else {
				sb.append(id);
				if (i < ids.size() - 1) {
					sb.append(",");
				}
			}
		}
		sb.append(")");
		return sb;
	}

	public static int[] getColumnIndexs(String[] projections, Cursor c) {
		int[] ret = new int[projections.length];
		for (int i = 0; i < projections.length; i++) {
			ret[i] = c.getColumnIndex(projections[i]);
		}
		return ret;
	}

	/* 修改软件版本号：1.0.0格式 */
	public static String modify_VersionName(String versionName) {
		if (versionName == null) {
			// Log.v(Service139.TAG, versionName + "=null");
			versionName = "0.0.0";
			return versionName;
		}
		int start = 0;
		int index = 0;
		int sedindex = 0;
		int num = 0;

		while (index != -1) {
			index = versionName.indexOf(".", start);
			// Log.v(Service139.TAG, "index=" + index);
			if (index != -1) {
				start = index + 1;
				num++;
			}
			if (num == 2) {
				sedindex = start;
			}
		}

		if (num == 1) {
			versionName += ".0";
			// Log.v(Service139.TAG, "versionName=" + versionName);
		} else if (num > 2) {
			versionName = versionName.substring(0, sedindex) + "0";
			// Log.v(Service139.TAG, "versionName=" + versionName);
		}
		return versionName;
	}

	/* 计算文件大小 */
	public static String accountFileSize(Long lSize) {
		long m = lSize / (1024 * 1024);
		if (m > 0) {
			long k = lSize % (1024 * 1024) / 1024 / 10;
			return m + "." + k + "M";
		} else {
			long k = lSize % (1024 * 1024) / 1024;
			return k + "k";
		}

	}
}
