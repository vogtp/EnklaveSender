package ch.almana.android.enklave.sender.connection;

import java.io.IOException;

/**
 * Created by vogtp on 7/8/14.
 */
public class LoginConnection extends BaseEnklaveConnection {
    public LoginConnection() throws IOException {
        super("http://www.enklave-mobile.com/login_check");
        addParam("_remember_me","true");
    }


    public void setUsername(String name) {
        addParam("_username", name);
    }


    public void setPassword(String pw) {
        addParam("_password", pw);
    }
}
