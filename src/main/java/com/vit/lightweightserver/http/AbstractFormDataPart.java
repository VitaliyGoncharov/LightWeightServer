package com.vit.lightweightserver.http;

public class AbstractFormDataPart {
	protected final String name;
	protected final String filename;
	protected final String contentType;
	protected final byte[] file;
	protected final long fileSize;
	
	public AbstractFormDataPart(String name, String filename, String contentType, byte[] file) {
		this.name = name;
		this.filename = filename;
		this.contentType = contentType;
		this.file = file;
		this.fileSize = file.length;
	}
}
