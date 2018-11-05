package com.vit.lightweightserver.util;

import java.io.IOException;
import java.net.Socket;

public class SocketUtils {
	public static void closeQuietly(Socket socket) {
		try { socket.close(); } catch (IOException e) {}
	}
}
