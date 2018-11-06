package com.vit.lightweightserver;

import java.awt.Desktop;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class Starter {
	private static Logger log = Logger.getLogger(Starter.class.getName());
	
	static final boolean VERBOSE = true;
	static final boolean OPEN_WEB_BROWSER = true;
	static final int PORT = 8080;
	
	@SuppressWarnings("unused")
	public static void main(String... args) throws IOException, URISyntaxException {
		
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
		
		if (OPEN_WEB_BROWSER && Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI("http://localhost:"+ PORT +"/uploadForm.html"));
		}
		
		while (true) {
			HttpServer myServer = new HttpServer(serverSocket.accept());
			
			Thread thread = new Thread(myServer);
			thread.start();
			
			if (VERBOSE) {
				log.info("ThreadID: " + thread.getId() + " | Connecton opened.");
			}
		}
	}
}
