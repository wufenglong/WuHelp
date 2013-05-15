package com.otheri.comm4and;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;

import com.otheri.io.Output;

public class FileHelper {

	public static final int MODE_SDCARD = 0;
	public static final int MODE_FILE = 1;

	private static String ROOT_SDCARD;
	private static String ROOT_FILE;

	private FileHelper() {
	}

	static {
		ROOT_SDCARD = new StringBuilder(Environment
				.getExternalStorageDirectory().getPath()).append('/')
				.toString();
		ROOT_FILE = new StringBuilder(Environment.getDataDirectory().getPath())
				.append('/').toString();
	}

	public static String getRoot(int mode) {
		switch (mode) {
		case MODE_SDCARD:
			return ROOT_SDCARD;
		case MODE_FILE:
		default:
			return ROOT_FILE;
		}
	}

	public static File getFile(String fileName, int mode) {
		return new File(new StringBuilder(getRoot(mode)).append(fileName)
				.toString());
	}

	public static boolean exist(String fileName, int mode) {
		return getFile(fileName, mode).exists();
	}

	public static boolean mkdirs(String fileName, int mode) {
		return getFile(fileName, mode).mkdirs();
	}

	public static boolean delete(String fileName, int mode) throws IOException {
		return getFile(fileName, mode).delete();
	}

	public static Output openOutput(String fileName, int mode)
			throws IOException {
		return new Output(new FileOutputStream(getFile(fileName, mode)));
	}

	public static InputStream openInputStream(String fileName, int mode)
			throws IOException {
		return new FileInputStream(getFile(fileName, mode));
	}

	public static boolean assetsExist(String fileName, Context context) {
		try {
			InputStream is = context.getAssets().open(fileName);
			if (is != null) {
				is.close();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static InputStream assetsOpenInputStream(String fileName,
			Context context) throws IOException {
		return context.getAssets().open(fileName);
	}

}
