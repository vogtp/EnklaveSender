package ch.almana.android.enklave.sender.connection;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 * Created by vogtp on 7/23/14.
 */
public class StingNumberNameValuePair extends BasicNameValuePair implements NameValuePair {
    private final boolean number;

    public StingNumberNameValuePair(String name, String value, boolean isNumber) {
        super(name, value);
        this.number = isNumber;
    }

    public boolean isNumber() {
        return number;
    }
}
