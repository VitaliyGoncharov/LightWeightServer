package com.vit.lightweightserver.request;

import java.util.Map;

import com.vit.lightweightserver.http.FormPart;

public interface Request {
	String getRequestURI();
	String getMethod();
	Map<String, String> getHeaders();
	Map<String, FormPart> getParts();
	String getHeader(String header);
	boolean isMultipart();
}
