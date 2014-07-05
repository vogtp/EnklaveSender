package ch.almana.android.enklave.enklavesender.connection;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Created by vogtp on 7/4/14.
 */
public class HttpClientEnklaveSubmit extends BaseEnklaveSubmit implements EnklaveSubmit {
    public HttpClientEnklaveSubmit(String url) {
        super();
    }

    @Override
    protected void addParam(String location_name, String name) {

    }

    @Override
    public void doPost() throws IOException {

    }

    @Override
    public String getResponse() throws IOException {
        return null;
    }

    @Override
    public void setEnklaveImage(Bitmap bitmap) {

    }

    @Override
    public void finish() {

    }
}
