package com.vit.lightweightserver;

import com.vit.lightweightserver.request.Request;
import com.vit.lightweightserver.response.Response;

public class TestServer {
	
	public static void main(String... args) {
		HttpServer.post("/main", (Request req, Response resp) -> {
			System.out.printf("Our method: %s%n", req.getMethod());
			System.out.printf("Our url: %s%n", req.getRequestURI());
			resp.setResource("test.html");
		});
		
		HttpServer.post("/lala", (Request req, Response resp) -> {
			System.out.printf("Our method: %s%n", req.getMethod());
			System.out.printf("Our url: %s%n", req.getRequestURI());
			resp.setResource("test1.html");
		});
	}
}
