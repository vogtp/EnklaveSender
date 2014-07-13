package ch.almana.android.enklave.enklavesender.connection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import ch.almana.android.enklave.enklavesender.SubmitActivity;
import ch.almana.android.enklave.enklavesender.WebsiteActivity;
import ch.almana.android.enklave.enklavesender.utils.Logger;
import ch.almana.android.enklave.enklavesender.utils.Settings;

final class Result {

    public String response;
    public boolean success = false;
    public String message;
}

/**
 * Created by vogtp on 7/13/14.
 *
 */
public class EnklaveSumbitAsyncTask extends AsyncTask<Void, Void, Result> {


    private final SubmitActivity act;
    private final String name;
    private final LatLng latlng;
    private final Bitmap image;

    public EnklaveSumbitAsyncTask(SubmitActivity act, String name, LatLng enklaveLatLng, Bitmap bitmap) {
        super();
        this.act = act;
        this.name = name;
        this.latlng = enklaveLatLng;
        this.image = bitmap;
    }

    @Override
    protected void onPostExecute(Result result) {
        if (act != null) {
            act.setProgressBarIndeterminateVisibility(false);
            if (result.success) {
                Toast.makeText(act, "Your Enklave has been submitted, please check your e-mail!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(act, "Error posting Enklave: " + result.message, Toast.LENGTH_LONG).show();
            }
            if (Settings.getInstance(act).isDebugMode()) {
                Intent i = new Intent(act, WebsiteActivity.class);
                i.putExtra(WebsiteActivity.EXTRA_HTML, result.response);
                act.startActivity(i);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (act != null) {
            act.setProgressBarIndeterminateVisibility(true);
        }
    }

    @Override
    protected Result doInBackground(Void... p) {
        Result result = new Result();
        EnklaveSubmit es = null;
        try {
            es = new EnklaveSubmitConnection();

            es.setEnklaveName(name);

            es.setEnklaveImage(image);
            es.setLatitude(latlng.latitude);
            es.setLongitude(latlng.longitude);
            es.doPost();
            result.response = es.getResponse();
            es.finish();
            result.success = true;
        } catch (Exception e) {
            Logger.e("Error posting enklave", e);
            result.message = e.getMessage();
        } finally {
            if (es != null) {
                es.finish();
            }
        }
        return result;
    }
}
