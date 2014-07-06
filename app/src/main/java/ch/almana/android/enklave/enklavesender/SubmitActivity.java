package ch.almana.android.enklave.enklavesender;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.almana.android.enklave.enklavesender.connection.EnklaveSubmit;
import ch.almana.android.enklave.enklavesender.connection.UrlConnectionEnklaveSubmit;
import ch.almana.android.enklave.enklavesender.utils.Debug;
import ch.almana.android.enklave.enklavesender.utils.Logger;

public class SubmitActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView imageView;
    private MarkerOptions marker;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private LatLng enklaveLatLng;
    private Button buSend;
    private EditText etName;
    private boolean isDebugMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        setTitle(getString(R.string.submitActivityTitle));

        etName = (EditText) findViewById(R.id.etName);
        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        imageView = (ImageView) findViewById(R.id.imageView);
        buSend = (Button) findViewById(R.id.buSend);

        isDebugMode = Debug.isUnsinedPackage(this);
        showDebugInfo();

        buSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URL url = null;
                String name = etName.getText().toString();
                if (name == null || name.trim().length() < 1) {
                    Toast.makeText(SubmitActivity.this, R.string.enter_name, Toast.LENGTH_LONG).show();
                    return;
                }
                buSend.setEnabled(false);

                if (isDebugMode) {
                    name = name + "_BANANA_from_tille";
                }


                final String finalName = name;
                Handler h = new Handler();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            //FIXME hack
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
                            EnklaveSubmit es = new UrlConnectionEnklaveSubmit("http://www.enklave-mobile.com/location_add#locationform");

                            es.setEnklaveName(finalName);

                            es.setEnklaveImage(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                            es.setLatitude(enklaveLatLng.latitude);
                            es.setLongitude(enklaveLatLng.longitude);
                            es.doPost();
                            String response = es.getResponse();
                            es.finish();
                            Intent i = new Intent(SubmitActivity.this, WebsiteActivity.class);
                            i.putExtra(WebsiteActivity.EXTRA_HTML, response);
                            startActivity(i);
                        } catch (Exception e) {
                            Logger.e("Error posting enklave", e);
                            Toast.makeText(SubmitActivity.this, "Error posting enklave: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });
        setUpMapIfNeeded();

        if (getIntent().hasExtra(Intent.EXTRA_STREAM)) {
            Uri photo = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            Logger.i("Got image from intent: " + photo);
            imageView.setImageURI(photo);
            //         ().setImageBitmap(BitmapFactory.decodeStream(photo));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showDebugInfo() {
        if (isDebugMode) {
            getActionBar().setSubtitle("********* Debug *********");
            buSend.setText("Send Test Data");
        } else {
            getActionBar().setSubtitle(null);
            buSend.setText(R.string.send);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Handler h = new Handler();
//        h.post(new Runnable() {
//                   @Override
//                   public void run() {

        if (!isLoggedIn()) {
            Toast.makeText(SubmitActivity.this, getString(R.string.msg_login_todo), Toast.LENGTH_LONG).show();
            startWebsite();
        }
//                   }
//               });

        setUpMapIfNeeded();
    }

    private void startWebsite() {
        startActivity(new Intent(this, WebsiteActivity.class));
    }

    private boolean isLoggedIn() {
        BufferedReader reader = null;
        HttpURLConnection conn = null;
        try {

            //FIXME hack
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            URL url = new URL(WebsiteActivity.URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();

            while (line != null) {
                Logger.i("Line: " + line);
                if (line.contains("location_add#locationform")) {
                    return true;
                }
                if (line.contains("Maximum file size: 8Mb")) {
                    return true;
                }
                if (line.contains("Logout")) {
                    return true;
                }
                if (line.contains("Login")) {
                    return false;
                }
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.website, menu);
        //   if (Debug.isUnsinedPackage(this)){
        getMenuInflater().inflate(R.menu.debug, menu);
        menu.findItem(R.id.action_debug).setChecked(isDebugMode);
        //  }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_debug).setChecked(isDebugMode);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_website) {
            startWebsite();
            return true;
        } else if (id == R.id.action_debug) {
            isDebugMode = !item.isChecked();
            item.setChecked(isDebugMode);
            showDebugInfo();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            updateMarker(latLng);

        }
    }


    private void updateMarker(LatLng latLng) {
        if (marker == null) {
            marker = new MarkerOptions().position(latLng);
        } else {
            mMap.clear();
        }
        enklaveLatLng = latLng;
        tvLatitude.setText(latLng.latitude + "");
        tvLongitude.setText(latLng.longitude + "");
        marker.position(latLng);
        mMap.addMarker(marker);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        updateMarker(latLng);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        updateMarker(latLng);
    }
}
