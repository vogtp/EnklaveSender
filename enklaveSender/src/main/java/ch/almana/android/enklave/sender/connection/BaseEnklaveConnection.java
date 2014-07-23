package ch.almana.android.enklave.sender.connection;

import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vogtp on 7/4/14.
 */
public abstract class BaseEnklaveConnection {
    private final static String charset = "utf-8";
   // private final static String charset = "UTF-8";
    private final URL url;
    private final HttpURLConnection conn;
    private final List<StingNumberNameValuePair> params;
    String attachmentName = "location_picture";
    String attachmentFileName = "location_picture.jpg";
    String crlf = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    protected Bitmap image;

    public BaseEnklaveConnection(String urlString) throws IOException {
        url = new URL(urlString);
        conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(20000);
        conn.setConnectTimeout(20000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("charset",charset);
        conn.setRequestProperty("accept-charset", charset);
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + this.boundary);
        params = new ArrayList<StingNumberNameValuePair>();
    }

    protected void addParam(String key, String value, boolean isNumber) {
        params.add(new StingNumberNameValuePair(key, value, isNumber));
    }

    public void doPost() throws IOException {

        OutputStream os = conn.getOutputStream();
        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        dos.writeBytes(this.twoHyphens + this.boundary + this.crlf);
        addFields(dos, params);

        if (image != null) {
            addImage(dos);
        }
        dos.flush();
        dos.close();
        os.flush();
        os.close();
    }

    private void addImage(DataOutputStream request) throws IOException {
        request.writeBytes("Content-Disposition: form-data; name=\"" + this.attachmentName + "\";filename=\"" + this.attachmentFileName + "\"" + this.crlf);
        request.writeBytes("Content-Type: image/jpeg" + crlf);
        request.writeBytes(this.crlf);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        request.write(bos.toByteArray());
        request.writeBytes(this.crlf);
        request.writeBytes(this.twoHyphens + this.boundary + this.twoHyphens + this.crlf);
    }

    public String getResponse() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = reader.readLine();
        String response = "";
        while (line != null) {
            response += line;
            line = reader.readLine();
        }
        conn.connect();
        return response;
    }

    public void finish() {
        conn.disconnect();
    }

    private void addFields(DataOutputStream dos, List<StingNumberNameValuePair> params) throws IOException {
        for (StingNumberNameValuePair pair : params) {
            dos.writeBytes("Content-Disposition: form-data; name=\"" + pair.getName() + "\"" + crlf);
            dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + crlf);
            dos.writeBytes("Content-Length: " + pair.getValue().length() + crlf);
            dos.writeBytes(crlf);
            if (pair.isNumber()){
                 dos.writeBytes(pair.getValue() + crlf);
            }else{
                dos.writeUTF(pair.getValue() + crlf);
            }
            dos.writeBytes(twoHyphens + boundary + crlf);
        }
    }
}
