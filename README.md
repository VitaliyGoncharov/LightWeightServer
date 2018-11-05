## LightWeightServer

Project was built in Eclipse.

## How to start
1. Download zip-archive from [Google Drive](https://drive.google.com/open?id=16m5uqR7IA6ASdU0hL1wOyJMfBDIc1Hul)
2. Unpack it in any folder
3. Click Starter.bat to run server
4. It will automatically open your default browser on http://localhost:8080/uploadForm.html

You can upload file (photo | JPG) only one per request. File will be uploaded to "./upload/" folder. Programm will create this folder automatically if it doesn't exist.
Once you uploaded photo you can find it here: "./upload/testPhoto.jpg"
Check that image was uploaded correctly.

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

But I found out that available doesn't mean all data. I didn't find more elegant solution than:
1. Read header from `inputBuffer` to find `Content-Length` header
2. Substract `inputBuffer.length` from `Content-Length`
3. Create new byte[] array with `Content-Length` capacity
4. Copy the first (small) byte array into big new one.
5. Read the remaining bytes `contentLength - bufferLength` from the same input stream into that expanded array with offset of first (small) buffer length.

```Java
int bufferLength = inputBuffer.length;
		
if (bufferLength < contentLength) {
  InputStream in = new BufferedInputStream(input);
  byte[] oldSmallBuffer = inputBuffer;
  inputBuffer = new byte[contentLength];
  System.arraycopy(oldSmallBuffer, 0, inputBuffer, 0, oldSmallBuffer.length);
  try {
    System.out.println("Content length is " + contentLength + "\r\nbuffer length is " + bufferLength);
    in.read(inputBuffer, bufferLength, contentLength - bufferLength);
    System.out.println("InputBuffer length is " + inputBuffer.length);
  } catch (IOException e) {
    e.printStackTrace();
  }
}
```
### When we get file, we need to get it directly from stream, from byte array
```Java
public byte[] getFile(byte[] inputBuffer, String req, String boundary) {
  String splitter = "--" + boundary;
  int bodyStartPos = req.indexOf("\r\n\r\n") + 4;
  String bodyStr = req.substring(bodyStartPos);

  int varStart = bodyStr.indexOf(splitter) + splitter.length();
  int fileStart = bodyStartPos + bodyStr.indexOf("\r\n\r\n", varStart) + "\r\n\r\n".length();
  int nextMatch = bodyStr.indexOf(splitter, fileStart);

  int fileEnd;
  int fileLength;

  if (nextMatch <= 0) {
    fileLength = req.length() - fileStart;
  } else {
    fileEnd = bodyStartPos + nextMatch;
    fileLength = fileEnd - fileStart;
  }


  byte[] file = new byte[fileLength];
  for (int i = 0; i < fileLength; i++) {
    file[i] = inputBuffer[fileStart + i];
  }
  return file;
}
```

## How to build
1. Clone repository:
```
git clone https://github.com/VitaliyGoncharov/LightWeightServer.git
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
8. Choose "Copy required libraries into a sub-folder next to the generated JAR" in Library handling section
9. You built a jar, but to launch it you need MANUALLY to create `ROOT` folder in the same directory where your jar was exported.
10. Place uploadForm.html in the `ROOT` directory.
11. Open console and run
```Java
java -jar LightWeightServer.jar
```
12. Program will open browser on `http://localhost:8080/uploadForm.html`

Actually, you can place almost any file in ROOT directory and access it via browser `http://localhost:8080/<your filename>`
