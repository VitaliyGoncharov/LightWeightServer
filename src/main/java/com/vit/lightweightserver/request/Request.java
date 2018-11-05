package com.vit.lightweightserver.request;

import java.net.URI;
import java.util.Map;

import com.vit.lightweightserver.http.FormPart;
import com.vit.lightweightserver.util.FileUtils;
import com.vit.lightweightserver.util.RequestPartUtils;
import com.vit.lightweightserver.util.RequestUtils;

public class Request {
	
	static final String MULTIPART = "multipart/form-data";
	
	final URI uri;
	final String method;
	final Map<String, String> headers;
	final Map<String, FormPart> parts;
	
	public Request(URI uri, String method, Map<String, String> headers, Map<String, FormPart> parts) {
		this.uri = uri;
		this.method = method;
		this.headers = headers;
		this.parts = parts;
		
		if (isMultipart()) {
			this.saveData();
		}
	}
	
	public void saveData() {
		FormPart myFile = parts.get("myfile");
		if (myFile != null) {
			FileUtils.write(myFile.getPartContent(), myFile.getSubmittedFileName());
		}
		
		FormPart name = parts.get("name");
		if (name != null) {
			System.out.println("Your name: " + RequestPartUtils.getPartContentAsString(name.getPartContent()));
		}
	}

	public URI getUri() {
		return uri;
	}

	public String getMethod() {
		return method;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public Map<String, FormPart> getParts() {
		return parts;
	}
	
	public boolean isMultipart() {
		String header = getHeader("Content-Type");
		if (header == null) return false;
		String contentType = RequestUtils.getContentType(header);
		
		if (contentType != null && contentType.equals(MULTIPART)) {
			return true;
		}
		
		return false;
	}
	
	public String getHeader(String header) {
		String headerValue = headers.get(header);
		if (headerValue == null) {
			return null;
		}
		return headerValue;
	}
}
