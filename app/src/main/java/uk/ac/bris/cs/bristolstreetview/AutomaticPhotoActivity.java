package uk.ac.bris.cs.bristolstreetview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.sql.Time;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutomaticPhotoActivity extends AppCompatActivity {

    private static final String TAG = "AutomaticPhotoActivity";

    private Button mStartLocationButton;
    private Button mStopLocationButton;
    private Button mStartTimeButton;
    private Button mStopTimeButton;

    private EditText mTimeIntervalField;

    private Location mLastPhotoLocation;

    private int mTimeInterval;

    private FusedLocationProviderClient mFusedLocationClient;
    private ScheduledExecutorService mScheduledExecutorService;

    //    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_photo);

        findAllViews();
        setAllOnClickListeners();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.e(TAG, "onCreate: LOCATION permission was not granted");
//        } else {
//            mFusedLocationClient.getLastLocation().addOnSuccessListener((location) -> {
//                Log.d(TAG, "Location: " + location);
//                mLastPhotoLocation = location;
//            });
//            startLocationUpdates();
//        }
    }

    private void findAllViews() {
        mStartLocationButton = findViewById(R.id.start_auto_location_button);
        mStopLocationButton = findViewById(R.id.stop_auto_location_button);
        mStartTimeButton = findViewById(R.id.start_auto_time_button);
        mStopTimeButton = findViewById(R.id.stop_auto_time_button);

        mTimeIntervalField = findViewById(R.id.IntervalEditText);
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

    private void startLocationPhotoTaking(View view) {

    }

    private void stopLocationPhotoTaking(View view) {

    }

    private void startTimePhotoTaking(View view) {
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(() -> {
            if (!Thread.interrupted()) {
                Log.d(TAG, "startTimePhotoTaking: TEST");
            }
        }, 0, mTimeInterval, TimeUnit.SECONDS);
    }

    private void stopTimePhotoTaking(View view) {
        Log.d(TAG, "stopTimePhotoTaking: Stopping");
        mScheduledExecutorService.shutdownNow();
        Log.d(TAG, "stopTimePhotoTaking: Stop singnal sent");
    }


    //    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(100);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Log.d(TAG, "startLocationUpdates: HERE >>>>>>>>>>>>>");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "onCreate: LOCATION permission was not granted");
        } else {

            mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Log.e(TAG, "onLocationResult: Location was null!");
                    } else {
                        for (Location location : locationResult.getLocations()) {
                            Log.d(TAG, "onLocationResult: location: " + location);
                            onCurrentLocationUpdated(location);
                        }
                    }
                }
            }, null);
        }
    }

    private void onCurrentLocationUpdated(Location location) {
        float distance = mLastPhotoLocation.distanceTo(location);
        Log.i(TAG, "onCurrentLocationUpdated: Distance walked: " + distance);
    }
}
