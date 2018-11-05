package com.vit.lightweightserver.util;

public class RequestUtils {
	public static int getContentLength(byte[] tempBuffer) {
		int headerStartPos = BinaryUtils.indexOf(tempBuffer, "Content-Length:".getBytes());
		if (headerStartPos == -1) return -1;
		int headerEndPos = BinaryUtils.indexOf(tempBuffer, headerStartPos, "\r\n".getBytes());
		
		int contentLengthStartPos = headerStartPos + "Content-Length: ".getBytes().length;
		int lengthSize = headerEndPos - contentLengthStartPos;
		StringBuilder sb = new StringBuilder(lengthSize);
		for (int i = contentLengthStartPos; i < headerEndPos; i++) {
			sb.append((char) tempBuffer[i]);
		}
		return Integer.parseInt(sb.toString());
	}
	
	public static int getDataStartPos(byte[] tempBuffer) {
		int emptyLineStartPos = BinaryUtils.indexOf(tempBuffer, "\r\n\r\n".getBytes()) + 2;
		int dataStartPos = emptyLineStartPos + 2;
		return dataStartPos;
	}
	
	public static String getMethod(byte[] inputBuffer) {
		int firstLineEnd = BinaryUtils.indexOf(inputBuffer, "\r\n".getBytes());
		StringBuilder firstLineStrBuilder = new StringBuilder();
		for (int i = 0; i < firstLineEnd; i++) {
			firstLineStrBuilder.append((char) inputBuffer[i]);
		}
		
		String firstLineStr = firstLineStrBuilder.toString();
		int methodStart = 0;
		int methodEnd = firstLineStr.indexOf(" ");
		
		if (methodEnd == -1) {
			return null;
		}
		
		return firstLineStr.substring(methodStart, methodEnd); 
	}
	
	public static String getContentType(String header) {
		int semicolon = header.indexOf(";", 0);
		if (semicolon == -1) {
			return header.substring(0);
		}
		return header.substring(0, semicolon);
	}
	
	public static String getBoundary(String header) {
		if (header.contains("boundary")) {
			int boundaryStartPos = header.indexOf("boundary") + "boundary=".length();
			return header.substring(boundaryStartPos);
		}
		
		return null;
	}
	
	public static void saveRequest(byte[] contentBuffer, String filename) {
		FileUtils.write(contentBuffer, filename);
	}
}
