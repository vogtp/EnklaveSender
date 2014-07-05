package ch.almana.android.enklave.enklavesender;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URL;

import ch.almana.android.enklave.enklavesender.R;

public class WebsiteActivity extends ActionBarActivity {

    public static final String EXTRA_HTML = "EXTRA_HTML";
    public static final String URL = "http://www.enklave-mobile.com/";
    private WebView webView;
    private WebViewClient webViewClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        webViewClient = new WebViewClient();
        webView.setWebViewClient(webViewClient);
//        webView.addJavascriptInterface(new JsInterface(), "callbacks");
        webView.setWebChromeClient(new WebChromeClient());
        if (getIntent().hasExtra(EXTRA_HTML)){
            final String mimeType = "text/html";
            final String encoding = "UTF-8";
            webView.loadDataWithBaseURL("", getIntent().getStringExtra(EXTRA_HTML), mimeType, encoding, "");
        }else{
            webView.loadUrl(URL);
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
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
