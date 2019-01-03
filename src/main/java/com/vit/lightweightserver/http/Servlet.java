package com.vit.lightweightserver.http;

import com.vit.lightweightserver.request.Request;
import com.vit.lightweightserver.response.Response;

@FunctionalInterface
public interface Servlet {
	void create(Request request, Response response);
}
