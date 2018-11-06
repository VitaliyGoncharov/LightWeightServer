package com.vit.lightweightserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import com.vit.lightweightserver.exception.NoStartingLineException;
import com.vit.lightweightserver.request.Request;
import com.vit.lightweightserver.request.RequestProcessor;
import com.vit.lightweightserver.response.Response;
import com.vit.lightweightserver.util.IOUtils;
import com.vit.lightweightserver.util.SocketUtils;

/**
 * My first implementation of http server
 *
 */
public class HttpServer implements Runnable {
	private static Logger log = Logger.getLogger(HttpServer.class.getName());
	
	private final Socket socket;
	private InputStream input;
	private OutputStream output;
	
	public HttpServer(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
			input = socket.getInputStream();
			output = socket.getOutputStream();
			
			RequestProcessor reqProcessor = new RequestProcessor(input);
			Request request = reqProcessor.getRequest();
			
			if (Starter.VERBOSE) {
				log.info("Requested method: " + request.getMethod());
				log.info("Requested uri: " + request.getUri());
			}
			
			Response response = new Response(output, request.getUri());
			response.sendStaticResource();
		} catch (NoStartingLineException e) {
			log.info("ThreadID: " + Thread.currentThread().getId() + " | Request was made without starting line!");
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(input);
			IOUtils.closeQuietly(output);
			SocketUtils.closeQuietly(socket);
			
			if (Starter.VERBOSE) {
				log.info("ThreadID: " + Thread.currentThread().getId() + " | Connection closed.\n");
			}
		}
		
	}
}
