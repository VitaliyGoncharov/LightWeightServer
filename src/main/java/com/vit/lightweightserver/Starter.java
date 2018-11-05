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
	static final boolean OPEN_WEB_BROWSER = false;
	
	@SuppressWarnings("unused")
	public static void main(String... args) throws IOException, URISyntaxException {
		
		@SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(8080);
		System.out.println("Server started.\nListening for connections on port : " + 8080 + " ...\n");
		
		if (OPEN_WEB_BROWSER && Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI("http://localhost:8080/uploadForm.html"));
		}
		
		while (true) {
			HttpServer myServer = new HttpServer(serverSocket.accept());
			
			if (VERBOSE) {
				log.info("Connecton opened.");
			}
			
			Thread thread = new Thread(myServer);
			thread.start();
		}
	}
}
