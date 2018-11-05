package com.vit.lightweightserver.http;

import javax.servlet.http.Part;

public interface FormPart extends Part {
	byte[] getPartContent();
}
