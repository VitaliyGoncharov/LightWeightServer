package com.vit.lightweightserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.vit.lightweightserver.exception.NoStartingLineException;
import com.vit.lightweightserver.http.Servlet;
import com.vit.lightweightserver.request.HttpRequestImpl;
import com.vit.lightweightserver.request.Request;
import com.vit.lightweightserver.request.RequestProcessor;
import com.vit.lightweightserver.response.HttpResponseImpl;
import com.vit.lightweightserver.response.Response;
import com.vit.lightweightserver.util.IOUtils;
import com.vit.lightweightserver.util.SocketUtils;

/**
 * 
 * @author vitgon
 *
 */
public class Client implements Runnable {
	private static Logger log = Logger.getLogger(Client.class.getName());
	
	private final Socket socket;
	private InputStream input;
	private OutputStream output;
	
	public Client(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
			input = socket.getInputStream();
			output = socket.getOutputStream();
			
			RequestProcessor reqProcessor = new RequestProcessor(input);
			Request request = reqProcessor.getRequest();
			
			if (HttpServer.VERBOSE) {
				log.info("Requested method: " + request.getMethod());
				log.info("Requested uri: " + request.getRequestURI());
			}
			
			Servlet servlet = null;
			
			Map<String, Servlet> servletMap = HttpServer.getServletMap();
			for (String uri : servletMap.keySet()) {
				if (uri.equals(request.getRequestURI())) {
					System.out.println("We defined such uri with servlet!");
					servlet = servletMap.get(uri);
				}
			}
			
			Response response = new HttpResponseImpl(output, request.getRequestURI());
			
			if (servlet != null) {
				servlet.create(request, response);
				response.sendStaticResource();
			} else {
				response.setResource("404.html");
				response.sendStaticResource();
			}
			
		} catch (NoStartingLineException e) {
			log.info("ThreadID: " + Thread.currentThread().getId() + " | Request was made without starting line!");
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			SocketUtils.closeQuietly(socket);
			
			if (HttpServer.VERBOSE) {
				log.info("ThreadID: " + Thread.currentThread().getId() + " | Connection closed.\n");
			}
		}
		
	}
}
