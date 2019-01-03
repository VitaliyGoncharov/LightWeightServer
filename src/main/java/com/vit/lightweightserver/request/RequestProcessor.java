package com.vit.lightweightserver.request;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vit.lightweightserver.exception.NoBoundaryException;
import com.vit.lightweightserver.exception.NoMethodException;
import com.vit.lightweightserver.exception.NoStartingLineException;
import com.vit.lightweightserver.http.FormDataPart;
import com.vit.lightweightserver.http.FormPart;
import com.vit.lightweightserver.util.BinaryUtils;
import com.vit.lightweightserver.util.RequestPartUtils;
import com.vit.lightweightserver.util.RequestUtils;

public class RequestProcessor {
	
	static final String MULTIPART = "multipart/form-data";
	
	String uri;
	String method;
	byte[] inputBuffer;
	InputStream inputStream;
	Map<String, String> headers = new HashMap<>();
	Map<String, FormPart> parts = new HashMap<>();
	
	public RequestProcessor(InputStream inputStream) throws Exception {
		this.inputStream = inputStream;
		process();
	}
	
	public void process() throws NoMethodException, IOException, NoBoundaryException, URISyntaxException, NoStartingLineException {
		try {
			this.getBytesFromStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.parseURI();
		this.parseHeaders();
		
		method = RequestUtils.getMethod(inputBuffer);
		if (method == null) { throw new NoMethodException("HTTP request doesn't contain any method (GET, POST, etc.)"); }
		
		boolean isMultipart = this.isMultipart();
		if (isMultipart) {
			this.parseContent();
		}
	}
	
	public void getBytesFromStream() throws IOException {
		InputStream in = new BufferedInputStream(inputStream);
		byte[] tempBuffer = new byte[in.available()];
		in.read(tempBuffer, 0, tempBuffer.length);
		
		int contentLength = RequestUtils.getContentLength(tempBuffer);
		
		if (contentLength == -1) {
			inputBuffer = tempBuffer;
			return;
		}
		int msgBodyDataStartPos = RequestUtils.getDataStartPos(tempBuffer);
		int contentReceivedLength = tempBuffer.length - msgBodyDataStartPos;
		
		if (contentReceivedLength < contentLength) {
			int remaining = contentLength - contentReceivedLength;
			int oldBufferEndPos = tempBuffer.length;
			int newBufferLength = tempBuffer.length + remaining;
			tempBuffer = Arrays.copyOf(tempBuffer, newBufferLength);
			
			int nRead;
			while (remaining > 0 && (nRead = in.read(tempBuffer, oldBufferEndPos, remaining)) != -1) {
			    oldBufferEndPos += nRead;
			    remaining -= nRead;
			};
		}
		
		inputBuffer = tempBuffer;
	}
	
	public void parseURI() throws URISyntaxException, NoStartingLineException {
		int startingLineEndPos = BinaryUtils.indexOf(inputBuffer, "\r\n".getBytes());
		StringBuilder startingLineStrBuilder = new StringBuilder();
		for (int i = 0; i < startingLineEndPos; i++) {
			startingLineStrBuilder.append((char) inputBuffer[i]);
		}
		String startingLine = startingLineStrBuilder.toString();
		if (startingLine.equals("")) throw new NoStartingLineException("Starting line is required!");
		int uriStart = startingLine.indexOf(" ") + 1;
		int uriEnd = startingLine.indexOf(" ", uriStart);
		uri = startingLine.substring(uriStart, uriEnd);
	}
	
	public void parseHeaders() {
		int headersStartPos = BinaryUtils.indexOf(inputBuffer, "\r\n".getBytes()) + 2;
		int headersEndPos = BinaryUtils.indexOf(inputBuffer, headersStartPos, "\r\n\r\n".getBytes());
		StringBuilder headersStrBuilder = new StringBuilder();
		for (int i = headersStartPos; i < headersEndPos; i++) {
			headersStrBuilder.append((char) inputBuffer[i]);
		}
		String[] headerLines = headersStrBuilder.toString().split("\r\n");
		for (String headerLine : headerLines) {
			int headerNameEndPos = headerLine.indexOf(":");
			String headerName = headerLine.substring(0, headerNameEndPos);
			int headerValueStartPos = headerNameEndPos + 2;
			String headerValue = headerLine.substring(headerValueStartPos, headerLine.length());
			headers.put(headerName, headerValue);
		}
	}
	
	public void parseContent() throws IOException, NoBoundaryException {
		int contentStartPos = BinaryUtils.indexOf(inputBuffer, "\r\n\r\n".getBytes()) + 4;
		int contentLength = RequestUtils.getContentLength(inputBuffer);
		byte[] contentBuffer = new byte[contentLength];
		System.arraycopy(inputBuffer, contentStartPos, contentBuffer, 0, contentLength);
		
		String boundary = RequestUtils.getBoundary(headers.get("Content-Type"));
		if (boundary == null) throw new NoBoundaryException("Boundary is required, but was not found");
		String splitter = "--" + boundary;
		
		List<byte[]> partsBuffers = this.getPartsBuffers(contentBuffer, splitter);
		
		// parse each part of the form
		this.parsePartsBuffers(partsBuffers);
	}
	
	public List<byte[]> getPartsBuffers(byte[] contentBuffer, String splitter) {
		List<byte[]> partsBuffers = new ArrayList<>();
		int lastParamStartPos = 0;
		int index = 0;
		while ((index = BinaryUtils.indexOf(contentBuffer, lastParamStartPos, splitter.getBytes())) != -1) {
			
			int splitterLength = splitter.getBytes().length + "\r\n".getBytes().length;
			String endMarker = splitter + "--";
			int parameterStartPos = index + splitterLength;
						
			int nextOccur = BinaryUtils.indexOf(contentBuffer, parameterStartPos, splitter.getBytes());

			int parameterEndPos = nextOccur - "\r\n".getBytes().length;
			lastParamStartPos = parameterStartPos;
			
			
			int j = 0;
			byte[] partBuffer = new byte[parameterEndPos - parameterStartPos];
			for (int i = parameterStartPos; i < parameterEndPos; i++) {
				partBuffer[j] = contentBuffer[i];
				j++;
			}
			partsBuffers.add(partBuffer);
			
			// check if it is the last parameter
			int endMarkerPos = BinaryUtils.indexOf(contentBuffer, nextOccur, endMarker.getBytes());
			if (endMarkerPos == nextOccur) {
				break;
			}
		}
		return partsBuffers;
	}
	
	public void parsePartsBuffers(List<byte[]> partsBuffers) {
		for (byte[] partBuffer : partsBuffers) {
			String contentDispHeader = RequestPartUtils.getContentDispositionHeader(partBuffer);
//			String dispositionType = RequestPartUtils.getDispositionType(contentDispHeader);
			String name = RequestPartUtils.getParamName(contentDispHeader);
			String filename = RequestPartUtils.getFilename(contentDispHeader);
			
			byte[] partContent = RequestPartUtils.getPartContent(partBuffer);
			
			// normal form field with name and value
			if (filename == null) {
				FormPart part = new FormDataPart(name, partContent);
				parts.put(name, part);
			}
			
			// field with type "file"
			if (filename != null) {
				String contentType = RequestPartUtils.getContentType(partBuffer);
				FormPart part = new FormDataPart(name, filename, contentType, partContent);
				parts.put(name, part);
			}
		}
	}
	
	public boolean isMultipart() {
		String header = headers.get(("Content-Type"));
		if (header == null) return false;
		String contentType = RequestUtils.getContentType(header);
		
		if (contentType != null && contentType.equals(MULTIPART)) {
			return true;
		}
		
		return false;
	}
	
	public HttpRequestImpl getRequest() {
		return new HttpRequestImpl(uri, method, headers, parts);
	}
}
