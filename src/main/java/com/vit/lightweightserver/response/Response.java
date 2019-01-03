package com.vit.lightweightserver.response;

import java.io.IOException;

public interface Response {
	void sendStaticResource() throws IOException;
	void setResource(String resource);
}
