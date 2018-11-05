package com.vit.lightweightserver.util;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RequestPartUtils {
	public static String getContentDispositionHeader(byte[] partBuffer) {
		int contentDispEndPos = BinaryUtils.indexOf(partBuffer, "\r\n".getBytes());
		
		byte[] contentDispArr = Arrays.copyOfRange(partBuffer, 0, contentDispEndPos);
		return new String(contentDispArr, Charset.forName("UTF-8"));
	}
	
	public static String getDispositionType(String contentDispHeader) {
		int startPos = contentDispHeader.indexOf(" ") + 1;
		int endPos = contentDispHeader.indexOf(";", startPos);
		return contentDispHeader.substring(startPos, endPos);
	}
	
	public static String getParamName(String contentDispHeader) {
		int startPos = contentDispHeader.indexOf("name") + "name".length() + 2;
		int endPos = contentDispHeader.indexOf("\"", startPos);
		return contentDispHeader.substring(startPos, endPos);
	}
	
	public static String getFilename(String contentDispHeader) {
		int filenameStartPos = contentDispHeader.indexOf("filename");
		if (filenameStartPos == -1) return null;
		
		int startPos = filenameStartPos + "filename".length() + 2; 
		int endPos = contentDispHeader.indexOf("\"", startPos);
		return contentDispHeader.substring(startPos, endPos);
	}
	
	public static byte[] getPartContent(byte[] part) {
		int startPos = BinaryUtils.indexOf(part, "\r\n\r\n".getBytes()) + 4;
		int endPos = part.length;
		int partContentLength = endPos - startPos;
		byte[] partContent = new byte[partContentLength];
		System.arraycopy(part, startPos, partContent, 0, partContentLength);
		return partContent;
	}
	
	public static String getContentType(byte[] partBuffer) {
		String contentTypeHeader = getContentTypeHeader(partBuffer);
		int startPos = contentTypeHeader.indexOf(" ") + 1;
		int endPos = contentTypeHeader.length();
		return contentTypeHeader.substring(startPos, endPos);
	}
	
	public static String getContentTypeHeader(byte[] partBuffer) {
		String contentTypeHeader = null;
		int contentDispEndPos = BinaryUtils.indexOf(partBuffer, "\r\n".getBytes());
		int contentTypeHeaderStartPos = contentDispEndPos + 2;
		int contentTypeHeaderEndPos = BinaryUtils.indexOf(partBuffer, contentTypeHeaderStartPos, "\r\n".getBytes());
		for (int i = contentTypeHeaderStartPos; i < contentTypeHeaderEndPos; i++) {
			contentTypeHeader += (char) partBuffer[i];
		}
		return contentTypeHeader;
	}
	
	public static String getPartContentAsString(byte[] partContent) {
		return IntStream
			.range(0, partContent.length)
			.mapToObj(i -> {
				return (char) partContent[i];
			})
			.map(String::valueOf)
			.collect(Collectors.joining());
	}
}
