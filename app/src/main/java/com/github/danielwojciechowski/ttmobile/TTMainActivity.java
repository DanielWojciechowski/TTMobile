package com.github.danielwojciechowski.ttmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.github.danielwojciechowski.ttmobile.tabapi.SlidingTabLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class TTMainActivity extends FragmentActivity implements LocationListener{

    private static TTMainActivity instance;

    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"Home","Map"};
    int numberOfTabs = 2;

    private Location location;
    private static GoogleMap googleMap;
    private static final int ZOOM_LEVEL = 17;
    private boolean mapPrepared = false;

    private static boolean trackingMode = false;
    private static String currentTravelUri;
    private static List<GeoPoint> points = new ArrayList<>();
    private static double distance = 0;

    //img
    private static String picturePath;
    private static List<JSONObject> images = new ArrayList<>();
    private static ImageView imageView;

    //DROPBOX
    final static private String APP_KEY = "f85ls6dgmuswl33";
    final static private String APP_SECRET = "es8rsn6691wjffs";
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private boolean mLoggedIn = false;
    private static DropboxAPI<AndroidAuthSession> mApi;
    private static String travelDirName;
    private static String secret;
    private static String userUID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttmain);

        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles, numberOfTabs);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);


        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        tabs.setViewPager(pager);

        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<>(session);
        if (!mLoggedIn) {
            mApi.getSession().startOAuth2Authentication(TTMainActivity.this);
        }
        new DropBoxConnector().execute();

        instance = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();

        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                storeAuth(session);
                mLoggedIn = true;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void storeAuth(AndroidAuthSession session) {
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.apply();
            secret = oauth2AccessToken;
        }
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            TTMainActivity.secret = secret;
            session.setOAuth2AccessToken(secret);
            mLoggedIn = true;
        } else {
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    public void prepareGPS(){

        googleMap = Tab2.getMap();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);

        locationManager.requestLocationUpdates(bestProvider, 5000, 0, this);

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(locationManager.getLastKnownLocation(bestProvider) != null) {
            location = locationManager.getLastKnownLocation(bestProvider);
        }
        if (location != null) {
            initializeLocation(location);
        }
    }

    private void initializeLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        updateCamera(new LatLng(latitude, longitude));

        updateLocationText(latitude, longitude);
    }

    private void updateCamera(LatLng latLng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
    }

    private void updateLocationText(double latitude, double longitude) {
        Tab2.updateLocationText(latitude, longitude);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!mapPrepared){
            initializeLocation(location);
            mapPrepared = true;
        }
        updateLocationText(location.getLatitude(), location.getLongitude());
        this.location = location;

        if(trackingMode){
            //new ApiFetcher(3).execute();
            points.add(new GeoPoint(location.getLatitude(), location.getLongitude(), new Date()));
            List<LatLng> latLngs = convertGeoPointsToLatLgns(points);
            addPolylineToMap(latLngs); //TODO: czy to jest dobrze rysowane?
            updateCamera(latLngs.get(latLngs.size() - 1));
            if(latLngs.size() > 1){
                Location locationFrom = new Location("start");
                LatLng latLngFrom = latLngs.get(latLngs.size() - 2);
                locationFrom.setLatitude(latLngFrom.latitude);
                locationFrom.setLongitude(latLngFrom.longitude);
                distance += locationFrom.distanceTo(location);
                Tab1.getDistanceValText().setText(distance < 1000 ? String.format("%.0f", distance) + " m" : String.format("%.2f", distance/1000) + " km");
            }
        }

    }

    public void addPolylineToMap(List<LatLng> checkpoints) {
        PolylineOptions options = new PolylineOptions();
        for (LatLng checkpoint : checkpoints) {
            options.add(checkpoint);
        }
        googleMap.addPolyline(options);
    }

    public void upload() {
        new DropBoxConnector(this).execute();

    }

    public void clickpic() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 100);

        } else {
            Toast.makeText(getApplication(), "Aparat nie jest wspierany w tym urządzeniu", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {

            try {
                if (!images.isEmpty() && images.get(images.size()-1).get("path") == null){
                    images.remove(images.size()-1);
                }
                JSONObject image = new JSONObject();
                JSONObject imageLocation = new JSONObject();
                imageLocation.put("latitude", location.getLatitude());
                imageLocation.put("longitude", location.getLongitude());
                imageLocation.put("date", new Date().getTime());
                image.put("location", imageLocation);
                images.add(image);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Uri selectedImage = data.getData();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            photo = rotateImage(photo);
            imageView = (ImageView) findViewById(R.id.Imageprev);
            imageView.setImageBitmap(photo);
        }
    }

    private Bitmap rotateImage(Bitmap photo) {
        try {
            ExifInterface exif = new ExifInterface(picturePath);
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;
            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            if(rotationAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);
                photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return photo;
    }

    private List<LatLng> convertGeoPointsToLatLgns(List<GeoPoint> geoPoints){
        List<LatLng> resultList = new LinkedList<>();
        for(GeoPoint geoPoint : geoPoints){
            resultList.add(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
        }
        return resultList;
    }

    public static void showToast() {
        Toast error = Toast.makeText(TTMainActivity.getInstance().getApplicationContext(), "Wycieczkę zapisano!", Toast.LENGTH_LONG);
        error.show();
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

    public static TTMainActivity getInstance() {
        return instance;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        TTMainActivity.googleMap = googleMap;
    }

    public static void setTrackingMode(boolean trackingMode) {
        TTMainActivity.trackingMode = trackingMode;
    }

    public Location getLocation() {
        return location;
    }

    public static void setCurrentTravelUri(String currentTravelUri) {
        TTMainActivity.currentTravelUri = currentTravelUri;
    }

    public static List<GeoPoint> getPoints() {
        return points;
    }

    public static String getTravelDirName() {
        return travelDirName;
    }

    public static void setTravelDirName(String travelDirName) {
        TTMainActivity.travelDirName = travelDirName;
    }

    public static String getPicturePath() {
        return picturePath;
    }

    public static String getUserUID() {
        return userUID;
    }

    public static void setUserUID(String userUID) {
        TTMainActivity.userUID = userUID;
    }

    public static List<JSONObject> getImages() {
        return images;
    }

    public static DropboxAPI<AndroidAuthSession> getmApi() {
        return mApi;
    }

    public static ImageView getImageView() {
        return imageView;
    }

    public static double getDistance() {
        return distance;
    }

    public static void setDistance(double distance) {
        TTMainActivity.distance = distance;
    }
}