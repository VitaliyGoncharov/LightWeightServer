package com.vit.lightweightserver.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
	
	/**
	 * Save byte buffer into "upload" folder with specified file name
	 * 
	 * @param inputBuffer
	 * @param fileName
	 * @throws IOException
	 */
	public static void write(byte[] inputBuffer, String filename) {
		File file = new File("upload/" + filename);
		FileOutputStream fos = null;
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			fos.write(inputBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
