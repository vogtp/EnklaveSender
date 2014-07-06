package ch.almana.android.enklave.enklavesender.connection;

/**
 * Created by vogtp on 7/4/14.
 */
public abstract class BaseEnklaveSubmit implements EnklaveSubmit {

    @Override
    public void setEnklaveName(String name) {
        addParam("location_name", name);
    }

    protected abstract void addParam(String location_name, String name);

    @Override
    public void setLatitude(double latitude) {
        addParam("latitude",latitude+"");
    }

    @Override
    public void setLongitude(double longitude) {
        addParam("longitude",longitude+"");
    }
}
