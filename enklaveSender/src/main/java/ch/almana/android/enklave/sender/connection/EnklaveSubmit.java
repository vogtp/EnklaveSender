package ch.almana.android.enklave.sender.connection;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Created by vogtp on 7/4/14.
 */
public interface EnklaveSubmit {
    void setEnklaveName(String name);

    void setLatitude(double latitude);

    void setLongitude(double longitude);

    void doPost() throws IOException;

    String getResponse() throws IOException;

    void setEnklaveImage(Bitmap bitmap);

    void finish();
}
