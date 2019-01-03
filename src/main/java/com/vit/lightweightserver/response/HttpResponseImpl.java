package com.vit.lightweightserver.response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Date;

public class HttpResponseImpl implements Response {
	String uri;
	OutputStream output;
	String resource;
	
	public HttpResponseImpl(OutputStream output, String uri) {
		this.output = output;
		this.uri = uri;
	}
	
	public void sendStaticResource() throws IOException {
		sendStaticResource(resource);
	}

	private void sendStaticResource(String resource) throws IOException {
		
		if (resource == null) {
			resource = uri.substring(1);
			if (resource == null) resource = "index.html";
		}
		
		PrintWriter out = new PrintWriter(output);
		BufferedOutputStream dataOut = new BufferedOutputStream(output);
		
		File file = new File(new File("./ROOT"), resource);
		
		if (!file.exists()) {
			String msg = "The requested page doesn't exist";
			out.println("HTTP/1.1 404 Not found\r\n");
			out.println("Content-Type: text/html; charset=utf-8\r\n");
			out.println("Content-Length: " + msg.getBytes().length + "\r\n");
			out.println("\r\n");
			out.println(msg);
			out.flush();
		} else {
			int fileLength = (int) file.length();
			FileInputStream fileStream = new FileInputStream(file);
			byte[] fileBytes = new byte[fileLength];
			fileStream.read(fileBytes);
			fileStream.close();
			
			out.println("HTTP/1.1 200 OK");
			out.println("Server: LightServer : 1.0");
			out.println("Date: " + new Date());
			out.println("Content-type: text/html");
			out.println("Content-length: " + fileLength);
			out.println();
			out.flush();
		
			dataOut.write(fileBytes, 0, fileLength);
			dataOut.flush();
		}
		
		out.close();
		dataOut.close();
	}

	@Override
	public void setResource(String resource) {
		this.resource = resource;
	}
}
