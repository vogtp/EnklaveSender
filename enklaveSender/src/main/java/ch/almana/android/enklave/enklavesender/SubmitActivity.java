package ch.almana.android.enklave.enklavesender;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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

import ch.almana.android.enklave.enklavesender.connection.EnklaveSubmit;
import ch.almana.android.enklave.enklavesender.connection.EnklaveSubmitConnection;
import ch.almana.android.enklave.enklavesender.utils.BitmapScaler;
import ch.almana.android.enklave.enklavesender.utils.Debug;
import ch.almana.android.enklave.enklavesender.utils.Logger;

public class SubmitActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private static final int REQUEST_CODE_TAKE_PICTURE = 1;
    private static final String EXTRA_IMAGE = "EXTRA_IMAGE";
    private static final String EXTRA_NAME = "EXTRA_NAME";
    private static final String EXTRA_LATLON = "EXTRA_LATLON";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView imageView;
    private MarkerOptions marker;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private LatLng enklaveLatLng = null;
    private Button buSend;
    private EditText etName;
    private boolean isDebugMode;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean hasImage = false;
    private Uri photoUri;
    private Bitmap photoBitmap;
    private CheckLogin checkLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        isDebugMode = Debug.isUnsinedPackage(this);
        setContentView(R.layout.activity_submit);
        setTitle(getString(R.string.submitActivityTitle));

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    updateMarker(latLng);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        etName = (EditText) findViewById(R.id.etName);
        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);
        imageView = (ImageView) findViewById(R.id.imageView);
        buSend = (Button) findViewById(R.id.buSend);

        buSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                if (name == null || name.trim().length() < 1) {
                    Toast.makeText(SubmitActivity.this, R.string.enter_name, Toast.LENGTH_LONG).show();
                    return;
                }
                buSend.setEnabled(false);
                setProgressBarIndeterminateVisibility(true);

                if (isDebugMode) {
                    name = name + "_BANANA_from_tille";
                }


                final String finalName = name;
                Handler h = new Handler();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        try {

//                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//                            StrictMode.setThreadPolicy(policy);
                            EnklaveSubmit es = new EnklaveSubmitConnection();

                            es.setEnklaveName(finalName);

                            es.setEnklaveImage(((BitmapDrawable) imageView.getDrawable()).getBitmap());
                            es.setLatitude(enklaveLatLng.latitude);
                            es.setLongitude(enklaveLatLng.longitude);
                            es.doPost();
                            String response = es.getResponse();
                            es.finish();
                            if (isDebugMode) {
                                Intent i = new Intent(SubmitActivity.this, WebsiteActivity.class);
                                i.putExtra(WebsiteActivity.EXTRA_HTML, response);
                                startActivity(i);
                            }
                            Toast.makeText(getApplicationContext(), "Your Enklave has been submitted, please check your e-mail!", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Logger.e("Error posting enklave", e);
                            Toast.makeText(SubmitActivity.this, "Error posting enklave: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } finally {
                            setProgressBarIndeterminateVisibility(false);
                        }

                    }
                });
            }
        });
        setUpMapIfNeeded();

        if (getIntent().hasExtra(Intent.EXTRA_STREAM)) {
            photoUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            Logger.i("Got image from intent: " + photoUri);
            imageView.setImageURI(photoUri);
            scalePhoto(imageView);
            hasImage = true;
        } else {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PICTURE);
                }
            });
        }
        if (savedInstanceState != null) {
            photoBitmap = savedInstanceState.getParcelable(EXTRA_IMAGE);
            if (photoBitmap != null) {
                imageView.setImageBitmap(photoBitmap);
            }
            enklaveLatLng = savedInstanceState.getParcelable(EXTRA_LATLON);
            if (enklaveLatLng != null) {
                updateMarker(enklaveLatLng);
            }
            etName.setText(savedInstanceState.getString(EXTRA_NAME));
        }
    }

    private void scalePhoto(ImageView iv) {
        new BitmapScaler().execute(iv);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_IMAGE, ((BitmapDrawable) imageView.getDrawable()).getBitmap());
        outState.putString(EXTRA_NAME, etName.getText().toString());
        outState.putParcelable(EXTRA_LATLON, enklaveLatLng);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == RESULT_OK) {
            photoBitmap = (Bitmap) data.getExtras().get("data");
            if (photoBitmap != null) {
                imageView.setImageBitmap(photoBitmap);
                scalePhoto(imageView);
                photoUri = null;
                hasImage = true;
            } else {
                hasImage = false;
            }
            enableSendButton();
        }
    }
    public boolean hasHoloTheme() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showDebugInfo() {
         ActionBar actionBar = null;
        if (hasHoloTheme()) {
            actionBar = getActionBar();
        }
        if (isDebugMode) {
            if (actionBar != null) {
                actionBar.setSubtitle("********* Debug *********");
            }
            buSend.setText("Send Test Data");
        } else {
            if (actionBar != null) {
                actionBar.setSubtitle(null);
            }
            buSend.setText(R.string.send);
        }
    }

    private class CheckLogin extends AsyncTask<Object, Object, Boolean> {

        @Override
        protected Boolean doInBackground(Object[] params) {
            return WebsiteActivity.isLoggedIn();
        }

        @Override
        protected void onPostExecute(Boolean loggedin) {
            if (!loggedin) {
                //  Toast.makeText(SubmitActivity.this, getString(R.string.msg_login_todo), Toast.LENGTH_LONG).show();
                final Intent intent = new Intent(SubmitActivity.this, WebsiteActivity.class);
                intent.putExtra(WebsiteActivity.EXTRA_LOGIN, true);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDebugInfo();
        checkLogin = new CheckLogin();
        checkLogin.execute();
//        Handler h = new Handler();
//        h.post(new Runnable() {
//            @Override
//            public void run() {
//
//                if (!WebsiteActivity.isLoggedIn()) {
//                    //  Toast.makeText(SubmitActivity.this, getString(R.string.msg_login_todo), Toast.LENGTH_LONG).show();
//                    final Intent intent = new Intent(SubmitActivity.this, WebsiteActivity.class);
//                    intent.putExtra(WebsiteActivity.EXTRA_LOGIN, true);
//                    startActivity(intent);
//                }
//            }
//        });
        setUpMapIfNeeded();
        updateLocation();
        enableSendButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (checkLogin != null) {
            checkLogin.cancel(true);
        }
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.website, menu);
        if (Logger.DEBUG) {
            getMenuInflater().inflate(R.menu.debug, menu);
            menu.findItem(R.id.action_debug).setChecked(isDebugMode);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Logger.DEBUG) {
            menu.findItem(R.id.action_debug).setChecked(isDebugMode);
        }
        return super.onPrepareOptionsMenu(menu);
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

//        if (marker != null){
//            mMap.addMarker(marker);
//        }
    }

    private void updateLocation() {
//        Criteria criteria = new Criteria();
//        criteria.setCostAllowed(false);
//        final String bestProvider = locationManager.getBestProvider(criteria, false);


        Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (location == null) {
            locationManager.requestSingleUpdate(LocationManager.PASSIVE_PROVIDER, locationListener, getMainLooper());
        } else {
            final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
//            updateMarker(latLng);
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
        if (mMap != null) {
            mMap.addMarker(marker);
        }
        enableSendButton();
    }

    private void enableSendButton() {
        buSend.setEnabled(enklaveLatLng != null && hasImage);
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
