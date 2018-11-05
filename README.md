## LightWeightServer

Project was built in Eclipse.

## How to start
1. Download zip-archive from [Google Drive](https://drive.google.com/open?id=1l1kJ2DGcUFhuzaCls3vR7GlJ4ZAcGIYS)
2. Unpack it in any folder
3. Click Starter.bat to run server
4. Open your browser on http://localhost:8080/uploadForm.html

You can upload file only one per request. File will be uploaded to "./upload/" folder. Programm will create this folder automatically if it doesn't exist.
Once you uploaded photo you can find it here: "./upload/<your file name>"
Check that file was uploaded correctly.

## Worth to note
### Request is not available for being read immediatelly
When we submit form and send binary data, we try to get it on the server like this:
```Java
// input is our input stream from socket
InputStream in = new BufferedInputStream(input);
int availableBytes = in.available();
inputBuffer = new byte[availableBytes];
in.read(inputBuffer);
```

But available doesn't mean all data. Also we should remember that it is a socket connection and we can't read bytes until it returns `-1`. I found the following solution:
1. Read header from `tempBuffer` to find `Content-Length` header
2. Find the position of header block end
4. Calculate how many bytes of content (after header block end position) we already read
3. Create new byte[] array with `header block length` + `Content-Length` capacity
4. Copy the first (small) byte array into big new one.
5. Read the remaining bytes from the same input stream into that expanded array with offset of first (small) buffer length.

```Java
public void getBytesFromStream() throws IOException {
	InputStream in = new BufferedInputStream(inputStream);
	byte[] tempBuffer = new byte[in.available()];
	in.read(tempBuffer, 0, tempBuffer.length);

	int contentLength = RequestUtils.getContentLength(tempBuffer);

	if (contentLength == -1) {
		inputBuffer = tempBuffer;
		return;
	}
	int msgBodyDataStartPos = RequestUtils.getDataStartPos(tempBuffer);
	int contentReceivedLength = tempBuffer.length - msgBodyDataStartPos;

	if (contentReceivedLength < contentLength) {
		int remaining = contentLength - contentReceivedLength;
		int oldBufferEndPos = tempBuffer.length;
		int newBufferLength = tempBuffer.length + remaining;
		tempBuffer = Arrays.copyOf(tempBuffer, newBufferLength);

		int nRead;
		while (remaining > 0 && (nRead = in.read(tempBuffer, oldBufferEndPos, remaining)) != -1) {
		    oldBufferEndPos += nRead;
		    remaining -= nRead;
		};
	}

	inputBuffer = tempBuffer;
}
```

## How to build
1. Clone repository:
```
git clone https://github.com/VitaliyGoncharov/light-weight-server.git
```

2. Run maven command in LightWeightServer directory to generate eclipse project settings files.
```
mvn eclipse:eclipse
```

3. Open Eclipse or Spring Tool Suite.
4. Import project: File->Import->Maven->Existing Maven Projects
5. Export project: File->Export->Java->Runnable jar
6. Choose launch class `HttpServer - LightWeightServer`
7. Choose export destination, for ex: C:\jars\LightWeightServer.jar
8. Choose "Package required libraries into generated JAR" in Library handling section
9. You built a jar, but to launch it you need MANUALLY to create `ROOT` folder in the same directory where your jar was exported.
10. Place uploadForm.html and upload.html to the `ROOT` directory.
11. Open console and run
```Java
java -jar LightWeightServer.jar
```
12. Open browser on `http://localhost:8080/uploadForm.html`

Actually, you can place almost any file in ROOT directory and access it via browser `http://localhost:8080/<your filename>`
