package ch.almana.android.enklave.sender.connection;

import android.app.Activity;
import android.os.Handler;
import android.webkit.WebView;

import ch.almana.android.enklave.sender.WebsiteActivity;

/**
 * Created by vogtp on 7/5/14.
 *
 */
public class LoginWebViewClient extends android.webkit.WebViewClient {

    private Activity act;

    protected LoginWebViewClient() {
        super();
    }

    public LoginWebViewClient(Activity act) {
        this();
        this.act = act;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        // in case we are redirected, we are signed in
//        if (WebsiteActivity.URL.equals(url)){
//            Logger.i("We were redirected to the website ("+url+"), this means we are signed in");
//            act.finish();
//        }else{
        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {

                if (WebsiteActivity.isLoggedIn()) {
                    if (act != null) {
                        act.finish();
                    }
                }
            }
        });
//        }
    }
}
