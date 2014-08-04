package ch.almana.android.enklave.sender;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;

import ch.almana.android.enklave.sender.connection.EnklaveSumbitAsyncTask;
import ch.almana.android.enklave.sender.utils.BitmapScaler;
import ch.almana.android.enklave.sender.utils.Logger;
import ch.almana.android.enklave.sender.utils.Settings;

public class SubmitActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private static final int REQUEST_CODE_TAKE_PICTURE = 1;
    private static final int REQUEST_CODE_SELECT_PHOTO = 2;
    private static final String EXTRA_IMAGE = "EXTRA_IMAGE";
    private static final String EXTRA_NAME = "EXTRA_NAME";
    private static final String EXTRA_LATLON = "EXTRA_LATLON";
    private static final String EXTRA_HAS_IMAGE = "EXTRA_HAS_IMAGE";
    private final Settings settings = Settings.getInstance(SubmitActivity.this);
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ImageView imageView;
    private MarkerOptions marker;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private LatLng enklaveLatLng = null;
    private Button buSend;
    private EditText etName;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean hasImage = false;
    //    private Bitmap photoBitmap;
    private CheckLogin checkLogin;
    private Uri cameraResultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_submit);
        setTitle(getString(R.string.submitActivityTitle));
        setSubtitle();

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

                if (settings.isDebugMode()) {
                    name = "TestEnklave_öäü_" + name;
                }

                new EnklaveSumbitAsyncTask(SubmitActivity.this, name, enklaveLatLng, ((BitmapDrawable) imageView.getDrawable()).getBitmap()).execute();

//                submitEnklave(name,enklaveLatLng, ((BitmapDrawable) imageView.getDrawable()).getBitmap());
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera();
            }
        });
        setUpMapIfNeeded();

        if (getIntent().hasExtra(Intent.EXTRA_STREAM)) {
            loadImageUri((Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
        }
        if (savedInstanceState != null) {
            Bitmap photoBitmap = savedInstanceState.getParcelable(EXTRA_IMAGE);
            if (photoBitmap != null) {
                imageView.setImageBitmap(photoBitmap);
                hasImage = true;
            }
            enklaveLatLng = savedInstanceState.getParcelable(EXTRA_LATLON);
            if (enklaveLatLng != null) {
                updateMarker(enklaveLatLng);
            }
            etName.setText(savedInstanceState.getString(EXTRA_NAME));
            hasImage = savedInstanceState.getBoolean(EXTRA_HAS_IMAGE);
            enableSendButton();
        }
    }

    private void loadImageUri(Uri photoUri) {
        Logger.i("Got image from intent: " + photoUri);

        try {
//                ExifInterface exif = new ExifInterface(photoUri.getPath());
            Cursor cursor = this.getContentResolver().query(photoUri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            cursor.moveToFirst();
            String filePath = cursor.getString(0);
            cursor.close();

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath,bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int scaleFactor = Math.min(photoW / BitmapScaler.MAX_IMAGE_SIZE, photoH / BitmapScaler.MAX_IMAGE_SIZE);

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap =  BitmapFactory.decodeFile(filePath,bmOptions);
            imageView.setImageBitmap(bitmap);
            scalePhoto(imageView);
            hasImage = true;


            ExifInterface exif = new ExifInterface(filePath);
            float[] ll = new float[2];
            exif.getLatLong(ll);
            String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            if (lat != null && lon != null) {
                enklaveLatLng = new LatLng(ll[0], ll[1]);
                updateMarker(enklaveLatLng);
            }
            Logger.w("Got lat/lon from image: " + lat + "/" + lon);
        } catch (Exception e) {
            Logger.w("Cannot get lat/lon from image", e);
        }
    }

    private void startCamera() {
        if (hasImageCaptureBug()) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUEST_CODE_SELECT_PHOTO);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraResultUri = Uri.fromFile(getCameraFile());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraResultUri);
            startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PICTURE);
        }
    }


    private File getCameraFile() {
        final File directory = Environment.getExternalStorageDirectory();
        String root = directory.toString();
        Logger.i("Camera root dir: " + directory.toString());
        File myDir = new File(root + "/enklave/");
        //noinspection ResultOfMethodCallIgnored
        myDir.mkdirs();
        String fname = "Enklave-" + DateFormat.getDateTimeInstance().format(System.currentTimeMillis()) + ".jpg";
        final File file = new File(myDir, fname);
        Logger.i("Camera file: " + file.toString());
        return file;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setSubtitle() {
        if (settings.hasHoloTheme()) {
            String subtitle = getString(R.string.version, settings.getVersionName());
            if (settings.isDebugMode()) {
                subtitle += " (DEBUG)";
            }
            if (getActionBar() != null) {
                getActionBar().setSubtitle(subtitle);
            }
        }
    }

    private void scalePhoto(ImageView iv) {
        new BitmapScaler().execute(iv);
    }

    public boolean hasImageCaptureBug() {
        if (settings.hasCameraIssues()) {
            return true;
        }
        // list of known devices that have the bug
        ArrayList<String> devices = new ArrayList<String>();
        devices.add("android-devphone1/dream_devphone/dream");
        devices.add("generic/sdk/generic");
        devices.add("vodafone/vfpioneer/sapphire");
        devices.add("tmobile/kila/dream");
        devices.add("verizon/voles/sholes");
        devices.add("google_ion/google_ion/sapphire");

        return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
                + android.os.Build.DEVICE);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_IMAGE, ((BitmapDrawable) imageView.getDrawable()).getBitmap());
        outState.putString(EXTRA_NAME, etName.getText().toString());
        outState.putParcelable(EXTRA_LATLON, enklaveLatLng);
        outState.putBoolean(EXTRA_HAS_IMAGE, hasImage);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (cameraResultUri != null) {
                    imageView.setImageURI(cameraResultUri);
                    hasImage = true;
                    scalePhoto(imageView);
                } else {
                    if (data == null){
                        Logger.e("No intent data",new Exception());
                        return;
                    }
                    Bitmap photoBitmap = (Bitmap) data.getExtras().get("data");
                    if (photoBitmap != null) {
                        if (photoBitmap.getHeight() > 100 || photoBitmap.getWidth() > 100) {
                            imageView.setImageBitmap(photoBitmap);
                            scalePhoto(imageView);
                            hasImage = true;
                        }
                        if (photoBitmap.getHeight() < BitmapScaler.MAX_IMAGE_SIZE-1 || photoBitmap.getWidth() < BitmapScaler.MAX_IMAGE_SIZE-1) {
                            Toast.makeText(this, getString(R.string.problem_with_camera_image_size), Toast.LENGTH_LONG).show();
                            hasCameraIssues();
                        }
                    } else {
                        hasImage = false;
                        hasCameraIssues();
                    }
                }
            } else if (resultCode == RESULT_CANCELED){
                Logger.i("Taking picture canceled..");
            } else {
                hasCameraIssues();
            }

            enableSendButton();
        } else if (requestCode == REQUEST_CODE_SELECT_PHOTO) {
            if (data == null){
                Logger.e("No intent data",new Exception());
                return;
            }
            Uri selectedImage = data.getData();
            loadImageUri(selectedImage);
        }
    }

    private void hasCameraIssues() {
        Toast.makeText(this, getString(R.string.camer_issues), Toast.LENGTH_LONG).show();
        settings.setHasCameraIssues(true);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showDebugInfo() {
        setSubtitle();
        if (settings.isDebugMode()) {
            buSend.setText(getString(R.string.send_test_data));
        } else {
            buSend.setText(R.string.send);
        }
    }

    public void clearForm() {
        etName.setText(null);
        enklaveLatLng = null;
        imageView.setImageResource(R.drawable.camera1);
        if (mMap != null) {
            mMap.clear();
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
        getMenuInflater().inflate(R.menu.map, menu);
        getMenuInflater().inflate(R.menu.website, menu);
        getMenuInflater().inflate(R.menu.changelog, menu);
        final Settings settings = this.settings;
        if (settings.enableDebugOption()) {
            getMenuInflater().inflate(R.menu.debug, menu);
            menu.findItem(R.id.action_debug).setChecked(settings.isDebugMode());
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final Settings settings = this.settings;
        if (settings.enableDebugOption()) {
            menu.findItem(R.id.action_debug).setChecked(settings.isDebugMode());
        }
        menu.findItem(R.id.action_map_satelite).setChecked(settings.isMapSatelit());
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
            boolean debugMode = !item.isChecked();
            item.setChecked(debugMode);
            Settings.getInstance(this).setDebugMode(debugMode);
            showDebugInfo();
        } else if (id == R.id.action_map_satelite) {
            boolean mapSat = !item.isChecked();
            item.setChecked(mapSat);
            Settings.getInstance(this).setMapSatelitMode(mapSat);
            setMapType();
        } else if (id == R.id.action_changelog) {
            startActivity(new Intent(this, ChangelogActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void setMapType() {
        mMap.setMapType(settings.isMapSatelit() ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
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
        setMapType();
    }

    private void updateLocation() {
        if (enklaveLatLng == null) {
            Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location == null) {
                locationManager.requestSingleUpdate(LocationManager.PASSIVE_PROVIDER, locationListener, getMainLooper());
            } else {
                final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                zoomToPosition(latLng);
            }
        } else {
            zoomToPosition(enklaveLatLng);
        }
    }

    private void zoomToPosition(LatLng latLng) {
        if (mMap == null) {
            return;
        }
        int zoom = 16;
        if (settings.isDebugMode()) {
            zoom = 1;
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
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
            zoomToPosition(latLng);
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
