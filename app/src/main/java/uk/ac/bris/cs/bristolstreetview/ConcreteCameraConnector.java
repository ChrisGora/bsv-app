package uk.ac.bris.cs.bristolstreetview;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

class ConcreteCameraConnector implements CameraConnector {

    private static final String TAG = "CameraConnector";

    private RequestQueue mQueue;
    private int mRequestsPending;

    private String mUrl;

    private CameraInfo mCameraInfo;

    ConcreteCameraConnector(RequestQueue queue, String url) {
        mQueue = queue;
        mUrl = url;
        mRequestsPending = 0;
        Log.v(TAG, "Queue and URL set!");
    }

    @Override
    public void updateCameraInfo() {
        String url = mUrl + "/osc/info";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                (response) -> {
                    mRequestsPending--;
                    Log.v(TAG, "RESPONSE: Requests pending: " + mRequestsPending);
                    Log.v(TAG, response.toString());
                    mCameraInfo = new CameraInfo();
                    try {
                        mCameraInfo.setSerialNumber(response.getString("serialNumber"));
                        mCameraInfo.setFirmwareVersion(response.getString("firmwareVersion"));
                        mCameraInfo.setGpsPresent(response.getBoolean("gps"));
                        mCameraInfo.setGyroPresent(response.getBoolean("gyro"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                (response) -> {
                    mRequestsPending--;
                    Log.v(TAG, "RESPONSE: Requests pending: " + mRequestsPending);
                    Log.e(TAG, "That didn't work :-(");
                }
        );
        mRequestsPending++;
        mQueue.add(request);
        Log.v(TAG, "QUEUED: Requests pending: " + mRequestsPending);
    }

//    public CameraInfo getCameraInfo() {
//        new Thread()
//        while (mRequestsPending > 0) {
//            try {
//                Thread.sleep(500);
//                Log.v(TAG, "Sleeping... ");
//                Log.v(TAG, "Requests pending: " + mRequestsPending);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

//        return mCameraInfo;
//    }

}
