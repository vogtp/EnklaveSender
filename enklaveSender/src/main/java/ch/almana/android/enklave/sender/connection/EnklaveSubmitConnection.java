package ch.almana.android.enklave.sender.connection;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Created by vogtp on 7/4/14.
 */
public class EnklaveSubmitConnection extends BaseEnklaveConnection implements EnklaveSubmit {

    public EnklaveSubmitConnection() throws IOException {
        super("http://www.enklave-mobile.com/location_add#locationform");
    }

    @Override
    public void setEnklaveImage(Bitmap bitmap) {
        image = bitmap;
    }

    @Override
    public void setEnklaveName(String name) {
        addParam("location_name", name);
    }

    @Override
    public void setLatitude(double latitude) {
        addParam("latitude", latitude + "");
    }

    @Override
    public void setLongitude(double longitude) {
        addParam("longitude", longitude + "");
    }


}
