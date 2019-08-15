package com.suomee.csp.lib.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	public static void zip(List<String> srcFiles, String desFile) throws Exception {
		if (srcFiles == null || srcFiles.isEmpty() || desFile == null || desFile.isEmpty()) {
			return;
		}
		ZipOutputStream zipOutput = null;
		try {
			zipOutput = new ZipOutputStream(new FileOutputStream(desFile));
			for (String srcFile : srcFiles) {
				File file = new File(srcFile);
				if (!file.exists() || file.isDirectory()) {
					continue;
				}
				ZipEntry zipEntry = new ZipEntry(file.getName());
				zipOutput.putNextEntry(zipEntry);
				FileInputStream in = null;
				try {
					in = new FileInputStream(file);
					int len = 0;
					byte[] buf = new byte[1024];
					while ((len = in.read(buf)) != -1) {
						zipOutput.write(buf, 0, len);
					}
				}
				finally {
					if (in != null) {
						in.close();
					}
				}
				zipOutput.closeEntry();
				zipOutput.flush();
			}
		}
		finally {
			if (zipOutput != null) {
				zipOutput.close();
			}
		}
	}
}
