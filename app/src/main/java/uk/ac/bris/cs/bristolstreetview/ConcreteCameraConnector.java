package uk.ac.bris.cs.bristolstreetview;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class ConcreteCameraConnector implements CameraConnector {

    private static final String TAG = "CameraConnector";

    private List<CameraConnectorObserver> mObservers;

    private Gson mGson;
    private RequestQueue mQueue;
    private int mRequestsPending;

    private String mUrl;


    ConcreteCameraConnector(RequestQueue queue, String url) {
        mObservers = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        mGson = builder.create();
        mQueue = Objects.requireNonNull(queue);
        mRequestsPending = 0;
        mUrl = Objects.requireNonNull(url);
        Log.v(TAG, "Queue and URL set!");
    }

/*    @Override
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
    }*/

    @Override
    public void updateCameraInfo() {  //GSON VERSION
        String url = mUrl + "/osc/info";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                (response) -> {
                    mRequestsPending--;
                    Log.v(TAG, "RESPONSE: Requests pending: " + mRequestsPending);
                    CameraInfo cameraInfo = mGson.fromJson(response.toString(), CameraInfo.class);
                    onCameraInfoUpdatedAll(cameraInfo);
                },
                (error) -> {
                    mRequestsPending--;
                    Log.v(TAG, "RESPONSE: Requests pending: " + mRequestsPending);
                    Log.e(TAG, "That didn't work :-(");
                }
        );
        mRequestsPending++;
        mQueue.add(Objects.requireNonNull(request));
        Log.v(TAG, "QUEUED: Requests pending: " + mRequestsPending);
    }

    @Override
    public void updateCameraState() {
        String url = mUrl + "/osc/state";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                (response) -> {
                    Log.d(TAG, "updateCameraState: " + response.toString());
                    CameraState cameraState = mGson.fromJson(response.toString(), CameraState.class);
                    onCameraStateUpdatedAll(cameraState);
                },
                (error) -> {
                    Log.e(TAG, "updateCameraState: FUCKED UP");
                });
        mQueue.add(Objects.requireNonNull(request));
    }

    @Override
    public void takePhoto() {
        Command takePhotoCommand = new Command();
        takePhotoCommand.setName("camera.takePicture");
        String takePhotoJsonCommand = mGson.toJson(takePhotoCommand);


        String url = mUrl + "/osc/commands/execute";
        JsonObjectRequest request = null;
        try {
            request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(takePhotoJsonCommand),
                    (response) -> {
                        Log.d(TAG, "takePhoto: " + response.toString());
                    },
                    (error) -> {
                        Log.e(TAG, "takePhoto: FUCKED UP");
                    });
        } catch (JSONException e) {
            Log.e(TAG, "takePhoto: JSON fucked up", e);
        }

        mQueue.add(Objects.requireNonNull(request));
    }

    @Override
    public void registerObserver(CameraConnectorObserver observer) {
        if (mObservers.contains(observer))
            throw new IllegalArgumentException("The observer to be registered has already been registered");
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
