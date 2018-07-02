package uk.ac.bris.cs.bristolstreetview;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class ConcreteCameraConnector implements CameraConnector {

    private static final String TAG = "CameraConnector";

    private List<CameraConnectorObserver> mObservers;

    private RequestQueue mQueue;
    private int mRequestsPending;

    private String mUrl;


    ConcreteCameraConnector(RequestQueue queue, String url) {
        mQueue = Objects.requireNonNull(queue);
        mUrl = Objects.requireNonNull(url);
        mRequestsPending = 0;
        mObservers = new ArrayList<>();
        Log.v(TAG, "Queue and URL set!");
    }

    @Override
    public void updateCameraInfo() {
        String url = mUrl + "/osc/info";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                (response) -> {
                    mRequestsPending--;
                    Log.v(TAG, "RESPONSE: Requests pending: " + mRequestsPending);
//                    Log.v(TAG, response.toString());
                    CameraInfo cameraInfo = new CameraInfo();
                    try {
                        cameraInfo.setSerialNumber(response.getString("serialNumber"));
                        cameraInfo.setFirmwareVersion(response.getString("firmwareVersion"));
                        cameraInfo.setGpsPresent(response.getBoolean("gps"));
                        cameraInfo.setGyroPresent(response.getBoolean("gyro"));
                        onCameraInfoUpdatedAll(cameraInfo);
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

    @Override
    public void updateCameraState() {

    }

    @Override
    public void registerObserver(CameraConnectorObserver observer) {
        if (mObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been refistered");
        else mObservers.add(Objects.requireNonNull(observer, "Observer to register was null"));
    }

    @Override
    public void removeObserver(CameraConnectorObserver observer) {
        if (mObservers.contains(Objects.requireNonNull(observer, "Observer to deregister was null")))
            mObservers.remove(observer);
        else throw new IllegalArgumentException("The observer to be deregistered isn't already registered");
    }

    private void onCameraInfoUpdatedAll (CameraInfo newCameraInfo) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onCameraInfoUpdated(newCameraInfo);
        }
    }

    private void onCameraStateUpdatedAll (CameraState newCameraState) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onCameraStateUpdated(newCameraState);
        }
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
