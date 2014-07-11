package ch.almana.android.enklave.enklavesender;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.almana.android.enklave.enklavesender.connection.LoginWebViewClient;
import ch.almana.android.enklave.enklavesender.utils.Logger;

public class WebsiteActivity extends ActionBarActivity {

    public static final String EXTRA_HTML = "EXTRA_HTML";
    public static final String EXTRA_LOGIN = "EXTRA_LOGIN";
    public static final String URL = "http://www.enklave-mobile.com/";
    public static final String URL_LOGIN = "http://www.enklave-mobile.com/login";
    private WebView webView;
    private WebViewClient webViewClient;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLogin = getIntent().getBooleanExtra(EXTRA_LOGIN, false);

        setContentView(R.layout.activity_website);
        webView = ((WebView) findViewById(R.id.webview));
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
        webSettings.setLoadWithOverviewMode(true);

        if (isLogin) {
            webViewClient = new LoginWebViewClient(this);
        } else {
            webViewClient = new WebViewClient();
        }
        webView.setWebViewClient(webViewClient);
//        webView.addJavascriptInterface(new JsInterface(), "callbacks");
        webView.setWebChromeClient(new WebChromeClient());

        if (getIntent().hasExtra(EXTRA_HTML)) {
            final String mimeType = "text/html";
            final String encoding = "UTF-8";
            webView.loadDataWithBaseURL(URL, getIntent().getStringExtra(EXTRA_HTML), mimeType, encoding, "");

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    view.loadUrl("javascript:{scrollToDiv('#locationform')}");
                }
            });
        } else {
            if (isLogin) {
                setTitle(getString(R.string.title_enklave_login));
                webView.loadUrl(URL_LOGIN);
            } else {
                webView.loadUrl(URL);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.website, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_website) {
            startActivity(new Intent(this, WebsiteActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean isLoggedIn() {
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            java.net.URL url = new URL(WebsiteActivity.URL_LOGIN);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            Logger.i("Resp URL: >" + conn.getURL() + "<");
//            if (WebsiteActivity.URL.equals(conn.getURL().toString())){
//                Logger.i("Authenticated with enklave wensite");
//                return true;
//            }
//             else{
//                Logger.i("Not authenticated with enklave wensite");
//                return false;
//            }
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                Logger.i("Line: " + line);
                if (line.contains("LOGIN")) {
                    return false;
                }
                if (line.contains("You are logged in.")) {
                    return true;
                }
//                if (line.contains("location_add#locationform")) {
//                    return true;
//                }
//                if (line.contains("Maximum file size: 8Mb")) {
//                    return true;
//                }
//                if (line.contains("Logout")) {
//                    return true;
//                }
//                if (line.contains("Login")) {
//                    return false;
//                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            Logger.e("Cannot check if login");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.disconnect();
            }

        }
        return false;
    }

}
