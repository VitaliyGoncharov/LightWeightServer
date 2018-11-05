package com.vit.lightweightserver.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
	public static void closeQuietly(OutputStream os) {
		try { os.close(); } catch (IOException e) {}
	}
	
	public static void closeQuietly(InputStream is) {
		try { is.close(); } catch (IOException e) {}
	}
}
