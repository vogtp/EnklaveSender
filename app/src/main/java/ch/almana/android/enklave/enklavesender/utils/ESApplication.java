package ch.almana.android.enklave.enklavesender.utils;

import android.app.Application;

public class ESApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i("Loading enklave sender application");
        initCookieStore();

    }

    private void initCookieStore() {
        Logger.i("loading sync cookie manager...");
        android.webkit.CookieSyncManager.createInstance(getApplicationContext());
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(coreCookieManager);
    }
}
