package com.otheri.comm4and.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Files {

	private static final String TAG = "Files";

	public static ArrayList<File> listFile(String uri) throws IOException {
		ArrayList<File> ret = new ArrayList<File>();
		File root = new File(".");
		File file = new File(root, uri);
		if (file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					ret.add(f);
				}
			}
		}
		return ret;
	}

}
