package uk.ac.bris.cs.bristolstreetview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;

public class SinglePhotoActivity extends AppCompatActivity implements CameraConnectorObserver{

    private static final String TAG = "SinglePhotoActivity";

    private Button mTakePhotoButton;
    private TextView mResponseTextView;

    private CameraConnector mCameraConnector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo);

        findAllViews();
        setAllOnClickListeners();

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.INTERNET);
        checkPermissions(permissions);

        mCameraConnector = new ConcreteCameraConnector(Volley.newRequestQueue(this), "http://192.168.1.1");

        mCameraConnector.registerObserver(this);

    }

    private void findAllViews() {
        mTakePhotoButton = findViewById(R.id.take_photo_button);
        mResponseTextView = findViewById(R.id.response_text_view);
    }

    private void setAllOnClickListeners() {
        mTakePhotoButton.setOnClickListener((view) -> {
            Log.v(TAG, "Button pressed");
//            sendStringGetRequest();
//            getCameraInfo();
//            sendJsonPostRequest();
            mCameraConnector.updateCameraInfo();
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            CameraInfo cameraInfo = mCameraConnector.getCameraInfo();
//            Log.v(TAG, cameraInfo.getFirmwareVersion());

        });
    }

    private void checkPermissions(List<String> permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, permission + " NOT granted");
//                ActivityCompat.requestPermissions(this, new String[permission], MY_PER);
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

    }

    @Override
    public void onCameraStateUpdated(CameraState newCameraState) {

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
