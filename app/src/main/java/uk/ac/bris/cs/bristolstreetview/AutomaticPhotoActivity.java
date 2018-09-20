package uk.ac.bris.cs.bristolstreetview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutomaticPhotoActivity extends AppCompatActivity implements PhotoTakerObserver {

    private static final String TAG = "AutomaticPhotoActivity";

    private Button mStartLocationButton;
    private Button mStopLocationButton;
    private Button mStartTimeButton;
    private Button mStopTimeButton;

    private EditText mTimeIntervalField;
    private ImageView mResponseImageView;

    private LinearLayout mLog;
    private TextView[] textviews = new TextView[2];

    private Location mLastPhotoLocation;

    private int mTimeInterval = 10;

    private PhotoTaker mPhotoTaker;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private ScheduledExecutorService mScheduledExecutorService;

    //    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_photo);

        findAllViews();
        setAllOnClickListeners();
        setUpLinearLayoutLog();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mPhotoTaker = new ConcretePhotoTaker(this);
        mPhotoTaker.registerObserver(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onCreate: LOCATION permission was not granted");
            logDistance(TAG, "onCreate: LOCATION permission was not granted");
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener((location) -> {
                Log.d(TAG, "Location: " + location);
//                logDistance(TAG, "Location: " + location);
                mLastPhotoLocation = location;
            });
//            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoTaker.onDestroy();
    }

    private void findAllViews() {
        mStartLocationButton = findViewById(R.id.start_auto_location_button);
        mStopLocationButton = findViewById(R.id.stop_auto_location_button);
        mStartTimeButton = findViewById(R.id.start_auto_time_button);
        mStopTimeButton = findViewById(R.id.stop_auto_time_button);

        mTimeIntervalField = findViewById(R.id.interval_edittext);
        mResponseImageView = findViewById(R.id.auto_response_image_view);

        mLog = findViewById(R.id.log_linear_layout);

    }

    private void setAllOnClickListeners() {

        mStartLocationButton.setOnClickListener(this::startLocationPhotoTaking);
        mStopLocationButton.setOnClickListener(this::stopLocationPhotoTaking);
        mStartTimeButton.setOnClickListener(this::startTimePhotoTaking);
        mStopTimeButton.setOnClickListener(this::stopTimePhotoTaking);

        mTimeIntervalField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mTimeInterval = Integer.parseInt(s.toString());
                } else {
                    mTimeInterval = 10;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    //----------------------------------------------------------------------------------------------

    private void startLocationPhotoTaking(View view) {
        startLocationUpdates();
    }

    private void stopLocationPhotoTaking(View view) {
//        mFusedLocationClient.removeLocationUpdates(new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                Log.d(TAG, "onLocationResult: stoplocation: Done?");
//            }
//        });
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Log.d(TAG, "stopLocationPhotoTaking: Stop signal sent");
        logDistance(TAG, "stopLocationPhotoTaking: Stop signal sent");
    }

    private void startTimePhotoTaking(View view) {
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(() -> {
            if (!Thread.interrupted()) {
                Log.d(TAG, "startTimePhotoTaking: SCHEDULED");
                mPhotoTaker.sendTakePhotoRequest(new PhotoRequest());
            }
        }, 0, mTimeInterval, TimeUnit.SECONDS);
    }

    private void stopTimePhotoTaking(View view) {
        Log.d(TAG, "stopTimePhotoTaking: Stopping");
        logDistance(TAG, "stopTimePhotoTaking: Stopping");
        mScheduledExecutorService.shutdownNow();
        Log.d(TAG, "stopTimePhotoTaking: Stop singnal sent");
        logDistance(TAG, "stopTimePhotoTaking: Stop singnal sent");
    }


    //----------------------------------------------------------------------------------------------
    //    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates: Starting location updates");
        logDistance(TAG, "startLocationUpdates: Starting location updates");
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Log.d(TAG, "startLocationUpdates: HERE >>>>>>>>>>>>>");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onCreate: LOCATION permission was not granted");
            logDistance(TAG, "onCreate: LOCATION permission was not granted");
        } else {
            mLocationCallback = getStandardLocationCallback();
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
        }
    }

    @NonNull
    private LocationCallback getStandardLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e(TAG, "onLocationResult: Location was null!");
                    logDistance(TAG, "onLocationResult: Location was null!");
                } else {
                    for (Location location : locationResult.getLocations()) {
//                            location.
                        Log.d(TAG, "onLocationResult: location: " + location);
//                        logDistance(TAG, "onLocationResult: location: " + location);

                        double locationAccuracy = location.getAccuracy();
                        double bearing = location.getBearing();
                        double bearingAccuracy = -1;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            bearingAccuracy = location.getBearingAccuracyDegrees();
                        }

                        onCurrentLocationUpdated(location, locationAccuracy, bearing, bearingAccuracy);
                    }
                }
            }
        };
    }

    private void onCurrentLocationUpdated(Location location, double locationAccuracy, double bearing, double bearingAccuracy) {
        Log.d(TAG, "onCurrentLocationUpdated: Location updated");
//        logDistance(TAG, "onCurrentLocationUpdated: Location updated");
        boolean isFirstPhoto = mLastPhotoLocation == null;
        float distance = 0;
        if (!isFirstPhoto) {
            Log.d(TAG, "onCurrentLocationUpdated: Last location was NOT null");
            distance = mLastPhotoLocation.distanceTo(location);
        }
        Log.i(TAG, "onCurrentLocationUpdated: Distance walked: " + distance);
        logDistance(TAG, "Distance walked: " + distance);
        if ((distance >= 15) || isFirstPhoto) {
            PhotoRequest photoRequest = new PhotoRequest();
            photoRequest.setLocation(location);
            photoRequest.setLocationAccuracy(locationAccuracy);
            photoRequest.setBearing(bearing);
            photoRequest.setBearingAccuracy(bearingAccuracy);

            mPhotoTaker.sendTakePhotoRequest(photoRequest);
            mLastPhotoLocation = location;
        }
    }

    @Override
    public void onPhotoTaken(PhotoRequest photoRequest) {
        Log.d(TAG, "onPhotoTaken: Got a url...");
//        displayImage(photoRequest.getCameraUrl());
    }

    private void displayImage(String url) {
        Picasso
                .get()
                .load(url)
                .resize(500, 500)
                .into(mResponseImageView);
    }

    @Override
    public void onPhotoSavedAndProcessed(PhotoRequest photoRequest) {
        Log.i(TAG, "onPhotoSavedAndProcessed: DONE!!! " + photoRequest.getDevicePath());
        logLastPhoto(TAG, "NEW PHOTO DONE!!! " + photoRequest.getDevicePath());
    }

    private void setUpLinearLayoutLog() {
        int i = 0;
//        setContentView(mLog);
        while (i < textviews.length) {
            textviews[i] = new TextView(this);
//            textviews[i].setText("test " + i);
            mLog.addView(textviews[i]);
            i++;
        }
    }

    private void logDistance(String tag, String message) {
        textviews[0].setText(message);
    }

    private void logLastPhoto(String Tag, String message) {
        textviews[1].setText(message);
    }
}
