package com.vit.lightweightserver;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.vit.lightweightserver.http.Servlet;

public class HttpServer {
	private static Logger log = Logger.getLogger(HttpServer.class.getName());
	private static Map<String, Servlet> servletMap = new ConcurrentHashMap<>();
	private static boolean RUNNING = false;
	
	static final boolean VERBOSE = true;
	static final boolean OPEN_WEB_BROWSER = false;
	static final int PORT = 80;
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, URISyntaxException {
		
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket( PORT, 0, InetAddress.getByName("192.168.1.238"));
		System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
		RUNNING = true;
		
		if (OPEN_WEB_BROWSER && Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI("http://localhost:"+ PORT +"/uploadForm.html"));
		}
		
		while (true) {
			Client client = new Client(serverSocket.accept());
			
			Thread thread = new Thread(client);
			thread.start();
			
			if (VERBOSE) {
				log.info("ThreadID: " + thread.getId() + " | Connecton opened.");
			}
		}
	}
	
	public static void post(String uri, Servlet servlet) {
		servletMap.put(uri, servlet);
		
		if (RUNNING == false) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						HttpServer.main(null);
					} catch (Exception e) {}
				}
			});
			thread.start();
		}
	}
	
	public static Map<String, Servlet> getServletMap() {
		return servletMap;
	}
}
