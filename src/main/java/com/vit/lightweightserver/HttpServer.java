package com.vit.lightweightserver;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * My first implementation of http server
 *
 */
public class HttpServer implements Runnable {
	
	static final String MULTIPART = "multipart/form-data";
	static final boolean verbose = true;
	
	Socket socket;
	byte[] inputBuffer;
	StringBuilder headers;
	
	InputStream input;
	OutputStream output;
	
	String method;
	
	public HttpServer(Socket socket) {
		this.socket = socket;
	}
	public static void main(String... args) throws IOException, URISyntaxException {
		
		ServerSocket serverSocket = new ServerSocket(8080);
		System.out.println("Server started.\nListening for connections on port : " + 8080 + " ...\n");
		
		if (Desktop.isDesktopSupported()) {
			Desktop.getDesktop().browse(new URI("http://localhost:8080/uploadForm.html"));
		}
		
		while (true) {
			HttpServer myServer = new HttpServer(serverSocket.accept());
			
			if (verbose) {
				System.out.println("Connecton opened. (" + new Date() + ")");
			}
			
			Thread thread = new Thread(myServer);
			thread.start();
		}
	}
	
	public void run() {
		run: try {
			
			input = socket.getInputStream();
			output = socket.getOutputStream();
			
			InputStream in = new BufferedInputStream(input);
			int availableBytes = in.available();
			inputBuffer = new byte[availableBytes];
			in.read(inputBuffer);
			
			parseHeaders();
			
			String method = getMethod();
			System.out.println("Request method is: " + method);
			boolean isMultipart = this.checkContentType(MULTIPART);
			
			if (method.equals("POST") && isMultipart) {
				int contentLength = Integer.parseInt(this.getHeader("Content-Length"));
				this.checkBufferSize(contentLength);
			}
			
//			int cnt = 0;
//			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//			int nRead;
//			byte[] data = new byte[1024];
//			while ((nRead = in.read(data, 0, data.length)) != -1) {
//				System.out.println("Cnt = " + cnt);
//				buffer.write(data, 0, nRead);
//				cnt++;
//			}
//			buffer.flush();
//			inputBuffer = buffer.toByteArray();
			
			String req = this.getRequestString(inputBuffer).toString();
			
			if (isMultipart) {
				byte[] file = this.getFile(inputBuffer, req, this.getBoundary(req));
				this.uploadFile(file);
				break run;
			}
			
			System.out.println("...Request doesn't contain form data...");
			sendStaticResource(req);
			
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				input.close();
				output.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (verbose) {
				System.out.println("Connection closed.\n");
			}
		}
		
	}
	
	public String getMethod() {
		
		if (method != null) {
			return method;
		}
		
		String[] headersLines = this.headers.toString().split("\r\n");
		int methodStart = 0;
		int methodEnd = headersLines[0].indexOf(" ");
		return headersLines[0].substring(methodStart, methodEnd); 
	}
	
	public String getBoundary(String req) {
		String headersBlock = req.split("\r\n\r\n")[0]; 
		String[] headers = headersBlock.split("\r\n");
		for (String header : headers) {
			if (header.startsWith("Content-Type")) {
				if (header.contains("boundary")) {
					int boundaryPos = header.indexOf("boundary");
					return header.substring(boundaryPos + "boundary=".length());
				}
			}
		}
		return null;
	}
	
	public StringBuilder getRequestString(byte[] inputBuffer) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < inputBuffer.length; i++) {
			sb.append((char)  inputBuffer[i]);
		}
		return sb;
	}
	
	public void saveRequest(byte[] inputBuffer) throws IOException {
		File file = new File("uploads/info.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fis = new FileOutputStream(file);
		fis.write(inputBuffer);
	}
	
	public void uploadFile(byte[] fileBuffer) throws IOException {
		File uploadDir = new File("upload");
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}
		
		File file = new File(uploadDir.getName() + System.getProperty("file.separator") + "testPhoto.jpg");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fis = new FileOutputStream(file);
		fis.write(fileBuffer);
	}
	
	public byte[] getFile(byte[] inputBuffer, String req, String boundary) {
		String splitter = "--" + boundary;
		int bodyStartPos = req.indexOf("\r\n\r\n") + 4;
		String bodyStr = req.substring(bodyStartPos);
		
		int varStart = bodyStr.indexOf(splitter) + splitter.length();
		int fileStart = bodyStartPos + bodyStr.indexOf("\r\n\r\n", varStart) + "\r\n\r\n".length();
		int nextMatch = bodyStr.indexOf(splitter, fileStart);
		
		int fileEnd;
		int fileLength;
		
		if (nextMatch <= 0) {
			fileLength = req.length() - fileStart;
		} else {
			fileEnd = bodyStartPos + nextMatch;
			fileLength = fileEnd - fileStart;
		}
		
		
		byte[] file = new byte[fileLength];
		for (int i = 0; i < fileLength; i++) {
			file[i] = inputBuffer[fileStart + i];
		}
		return file;
	}
	
	public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
	    ByteArrayOutputStream os = new ByteArrayOutputStream(); 
	    byte[] buffer = new byte[0xFFFF];
	    for (int len = is.read(buffer); len != -1; len = is.read(buffer)) { 
	        os.write(buffer, 0, len);
	    }
	    return os.toByteArray();
	}
	
	public boolean checkContentType(String contentType) {
		String header = getHeader("Content-Type");
		
		if (header != null && header.equals(contentType)) {
			return true;
		}
		
		return false;
	}
	
	public String getHeader(String header) {
		String[] headersLines = headers.toString().split("\r\n");
		for (int i = 0; i < headersLines.length; i++) {
			String headerLine = headersLines[i];
			if (headerLine.startsWith(header)) {
				int valueStartIndex = headerLine.indexOf(" ") + 1;
				
				if (header == "Content-Type") {
					int semicolon = headerLine.indexOf(";", valueStartIndex);
					if (semicolon == -1) {
						return headerLine.substring(valueStartIndex);
					}
					return headerLine.substring(valueStartIndex, semicolon);
				}
				
				return headerLine.substring(valueStartIndex);
			}
		}
		return null;
	}
	
	public void parseHeaders() {
		int headersEnd = indexOf(inputBuffer, "\r\n\r\n".getBytes());
		headers = new StringBuilder();
		for (int i = 0; i < headersEnd; i++) {
			headers.append((char) inputBuffer[i]);
		}
	}
	
	public void checkBufferSize(int contentLength) {
		int bufferLength = inputBuffer.length;
		
		if (bufferLength < contentLength) {
			InputStream in = new BufferedInputStream(input);
			byte[] oldSmallBuffer = inputBuffer;
			inputBuffer = new byte[contentLength];
			System.arraycopy(oldSmallBuffer, 0, inputBuffer, 0, oldSmallBuffer.length);
			try {
				System.out.println("Content length is " + contentLength + "\r\nbuffer length is " + bufferLength);
				in.read(inputBuffer, bufferLength, contentLength - bufferLength);
				System.out.println("InputBuffer length is " + inputBuffer.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendStaticResource(String req) throws IOException {
		String[] headersLines = this.headers.toString().split("\r\n");
		int locationStart = headersLines[0].indexOf(" ") + 1;
		int locationEnd = headersLines[0].indexOf(" ", locationStart);
		String location = headersLines[0].substring(locationStart, locationEnd);
		
		PrintWriter out = new PrintWriter(this.output);
		BufferedOutputStream dataOut = new BufferedOutputStream(this.output);
		
		File file = new File(new File("./ROOT"), location);
		System.out.println(file.getAbsolutePath());
		
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
	
	public static int indexOf(byte[] data, byte[] pattern) {
        int[] failure = computeFailure(pattern);

        int j = 0;
        if (data.length == 0) return -1;

        for (int i = 0; i < data.length; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) { j++; }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    private static int[] computeFailure(byte[] pattern) {
        int[] failure = new int[pattern.length];

        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }
}
