package com.vit.lightweightserver.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class FormDataPart extends AbstractFormDataPart implements FormPart {

	public FormDataPart(String name, String filename, String contentType, byte[] file) {
		super(name, filename, contentType, file);
	}
	
	public FormDataPart(String name, byte[] file) {
		super(name, null, null, file);
	}
	
	@Override
	public byte[] getPartContent() {
		return file;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(file);
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSubmittedFileName() {
		return filename;
	}

	@Override
	public long getSize() {
		return fileSize;
	}

	@Override
	public void write(String fileName) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void delete() throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
