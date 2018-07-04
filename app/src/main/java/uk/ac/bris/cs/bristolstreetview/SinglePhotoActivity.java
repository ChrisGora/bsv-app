package uk.ac.bris.cs.bristolstreetview;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SinglePhotoActivity extends AppCompatActivity implements CameraConnectorObserver{

    private static final String TAG = "SinglePhotoActivity";

    private Button mTakePhotoButton;
    private Button mUpdateInfoButton;
    private Button mUpdateStateButton;
    private Button mMuteButton;
    private Button mFullVolumeButton;
    private ImageView mResponseImageView;

    private CameraConnector mCameraConnector;
    private DownloadManager mDownloadManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo);

        findAllViews();
        setAllOnClickListeners();

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.INTERNET);
        permissions.add((Manifest.permission.READ_EXTERNAL_STORAGE));
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        checkPermissions(permissions);
//        checkPermissions(permissions);

        mCameraConnector = new ConcreteCameraConnector(Volley.newRequestQueue(this), "http://192.168.1.1");
        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        mCameraConnector.registerObserver(this);

    }

    private void findAllViews() {
        mTakePhotoButton = findViewById(R.id.take_photo_button);
        mUpdateInfoButton = findViewById(R.id.update_info_button);
        mUpdateStateButton = findViewById(R.id.update_state_button);
        mMuteButton = findViewById(R.id.mute_button);
        mFullVolumeButton = findViewById(R.id.full_volume_button);

        mResponseImageView = findViewById(R.id.response_image_view);
    }

    private void setAllOnClickListeners() {
        mTakePhotoButton.setOnClickListener((view) -> {
            Log.v(TAG, "Take photo pressed");
            mCameraConnector.sendTakePhotoRequest();
        });

        mUpdateInfoButton.setOnClickListener((view) -> {
            Log.v(TAG, "Update info pressed");
            mCameraConnector.updateCameraInfo();
        });

        mUpdateStateButton.setOnClickListener((view) -> {
            Log.v(TAG, "Update state pressed");
            mCameraConnector.updateCameraState();
        });

        mMuteButton.setOnClickListener((view) -> {
            Log.v(TAG, "Mute pressed");
            mCameraConnector.setShutterVolume(0);
        });

        mFullVolumeButton.setOnClickListener((view) -> {
            Log.v(TAG, "Full volume pressed");
            mCameraConnector.setShutterVolume(100);
        });
    }

    private void checkPermissions(List<String> permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, permission + " NOT granted");
                ActivityCompat.requestPermissions(this, new String[] {permission}, 0);
            } else {
                Log.v(TAG, permission + " granted");
            }
        }
    }

    @Override
    public void onCameraInfoUpdated(CameraInfo newCameraInfo) {
        Log.v(TAG, "CALLED ON INFO UPDATED");
        Log.i(TAG, "Serial Number: " + newCameraInfo.getSerialNumber());
        Log.i(TAG, "Firmware Version: " + newCameraInfo.getFirmwareVersion());
        Log.i(TAG, "GPS Present? : " + newCameraInfo.getGpsPresent());
        Log.i(TAG, "Gyro Present? : " + newCameraInfo.getGyroPresent());
        Log.i(TAG, "Model: " + newCameraInfo.getModel());

    }

    @Override
    public void onCameraStateUpdated(CameraState newCameraState) {
        Log.i(TAG, "fingerprint: " + newCameraState.getFingerprint());
        Log.i(TAG, "session ID: " + newCameraState.getState().getSessionId());
        Log.i(TAG, "battery level: " + newCameraState.getState().getBatteryLevel());
    }


    @Override
    public void onTakePhotoInProgress(CameraOutput output) {
        Log.i(TAG, "onTakePhotoInProgress: id: " + output.getId());
        Log.i(TAG, "onTakePhotoInProgress: name: " + output.getName());
        Log.i(TAG, "onTakePhotoInProgress: completion: " + output.getProgress().getCompletion());
        Log.i(TAG, "onTakePhotoInProgress: state: " + output.getState());
    }

    @Override
    public void onTakePhotoError(CameraOutput output) {
//        Log.e(TAG, "onTakePhotoError: " + output.getError().getCode());
//        Log.e(TAG, "onTakePhotoError: " + output.getError().getMessage());
    }

    @Override
    public void onTakePhotoDone(CameraOutput output) {
        Log.i(TAG, "onTakePhotoDone: " + output.getResults().getFileUrl());
        displayImage(output.getResults().getFileUrl());
        downloadImage(output.getResults().getFileUrl(), output.getId());
    }

    private void displayImage(String url) {
        Picasso
                .get()
                .load(url)
                .resize(500, 500)
                .into(mResponseImageView);
    }

/*
    private void downloadImage(String url, String id) {
        Log.d(TAG, "downloadImage: url " + url);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        Log.d(TAG, "downloadImage: uri " + Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
//        request.setTitle("Downloading photo");
//        request.setDescription("Bristol StreetView is downloading an image...");
//        request.setVisibleInDownloadsUi(true);
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM, "photo.jpg");
        long refid = mDownloadManager.enqueue(request);
    }
*/

    private void downloadImage(String url, String id) {
        Target target = new ImageDownloaderTarget(url);
        Picasso
                .get()
                .load(url)
                .into(target);
    }

    /*private void sendStringGetRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mUrl + "/osc/info",
                (response) -> mResponseTextView.setText(response),
                (response) -> mResponseTextView.setText("That didn't work :-(")
        );
        mQueue.add(stringRequest);
    }

    private void sendJsonPostRequest() {
        String url = mUrl + "/osc/state";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                (response) -> {
            mResponseTextView.setText(response.toString());
                    try {
                        String fingerprint = response.getString("fingerprint");
                        JSONObject state = response.getJSONObject("state");
                        Number batteryLevel = state.getDouble("batteryLevel");
                        Log.v("TAG", ">>>> " + fingerprint);
                        Log.v("TAG", ">>>> BATTERY LEVEL: " + batteryLevel);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                (response) -> mResponseTextView.setText("That didn't work :-(")
        );
        mQueue.add(request);
    }
*/




}
