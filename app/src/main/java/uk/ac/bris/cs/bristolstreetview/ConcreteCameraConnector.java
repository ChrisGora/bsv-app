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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ConcreteCameraConnector implements CameraConnector {

    private static final String TAG = "CameraConnector";

    private List<CameraConnectorObserver> mObservers;

    private Gson mGson;
    private RequestQueue mQueue;
    private int mRequestsPending;

    private String mUrl;

    private Map<String, String> mJobStatusMap;

    ConcreteCameraConnector(RequestQueue queue, String url) {
        mObservers = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        mGson = builder.create();
        mQueue = Objects.requireNonNull(queue);
        mRequestsPending = 0;
        mUrl = Objects.requireNonNull(url);

        mJobStatusMap = new HashMap<>();

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
                    CameraInfo cameraInfo = mGson.fromJson(response.toString(), CameraInfo.class);
                    onCameraInfoUpdatedAll(cameraInfo);
                },
                (error) -> {
                    Log.e(TAG, "That didn't work :-(");
                }
        );
        mQueue.add(Objects.requireNonNull(request));
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
    public void sendTakePhotoRequest() {
        CameraCommand takePhotoCommand = new CameraCommand();
        takePhotoCommand.setName("camera.takePicture");
        String takePhotoJsonCommand = mGson.toJson(takePhotoCommand);

        Log.d(TAG, "sendTakePhotoRequest: takePhotoJsonCommand: " + takePhotoJsonCommand);

        String url = mUrl + "/osc/commands/execute";
        JsonObjectRequest request = null;
        try {
            request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(takePhotoJsonCommand),
                    (response) -> {
                        Log.d(TAG, "sendTakePhotoRequest: " + response.toString());
                        CameraOutput cameraOutput = mGson.fromJson(response.toString(), CameraOutput.class);
                         updateJobStatusMap(cameraOutput);
                         if (cameraOutput.getState().equals("inProgress")) {
                             onTakePhotoInProgressAll(cameraOutput);
                             setStatusListener(cameraOutput.getId());
                         } else {
                             throw new AssertionError("Photo taking not in progress");
                         }
                    },
                    (error) -> {
                        Log.e(TAG, "sendTakePhotoRequest: FUCKED UP", error);
                    });
        } catch (JSONException e) {
            Log.e(TAG, "sendTakePhotoRequest: JSON fucked up", e);
        }

        mQueue.add(Objects.requireNonNull(request));
    }

    private void checkStatus(String id) {

        Log.d(TAG, "checkStatus: >>>>>>>>>>>>>>>>>>>>>>>>> HERE");

        CameraCommand checkStatusCommand = new CameraCommand();
        checkStatusCommand.setId(id);
        String checkStatusJsonCommand = mGson.toJson(checkStatusCommand);

        Log.d(TAG, "checkStatus: checkStatusJsonCommand: " + checkStatusJsonCommand);

        String url = mUrl + "/osc/commands/status";
        JsonObjectRequest request = null;
        try {
            request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(checkStatusJsonCommand),
                    (response) -> {
                        Log.d(TAG, "checkStatus: " + response.toString());
                        CameraOutput cameraOutput = mGson.fromJson(response.toString(), CameraOutput.class);
                        String state = cameraOutput.getState();
                        Log.d(TAG, "checkStatus: GET STATE SAYS: " + state);
                        switch (state) {
                            case "done":            {
                                Log.d(TAG, "checkStatus: DONE");
                                onTakePhotoDoneAll(cameraOutput);
                                updateJobStatusMap(id, state);
                                break;
                            }
                            case "inProgress":      {
                                Log.d(TAG, "checkStatus: PROGRESS");
                                onTakePhotoInProgressAll(cameraOutput);
                                updateJobStatusMap(cameraOutput);
                                break;
                            }
                            case "error":           {
                                Log.d(TAG, "checkStatus: ERROR");
                                updateJobStatusMap(id, state);
                                break;
                            }
                        }

                    },
                    (error) -> {
                        Log.e(TAG, "checkStatus: FUCKED UP");
                    });
        } catch (JSONException e) {
            Log.e(TAG, "checkStatus: JSON fucked up", e);
        }

        mQueue.add(Objects.requireNonNull(request));
    }

    private void setStatusListener(String id) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            if (mJobStatusMap.get(id).equals("inProgress")) checkStatus(id);
            else executor.shutdownNow();
        },
                0,
                400,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void setShutterVolume(int volume) {
        CameraCommand setVolumeCommand = new CameraCommand();
        setVolumeCommand.setParameters(new Parameters());
        setVolumeCommand.getParameters().setOptions(new Options());
        setVolumeCommand.getParameters().getOptions().set_shutterVolume(volume);

        setVolumeCommand.setName("camera.setOptions");
        String setVolumeJsonCommand = mGson.toJson(setVolumeCommand);

        Log.d(TAG, "setShutterVolume: setVolumeJsonCommand: " + setVolumeJsonCommand);

        String url = mUrl + "/osc/commands/execute";
        JsonObjectRequest request = null;
        try {
            request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(setVolumeJsonCommand),
                    (response) -> {
                        Log.d(TAG, "setShutterVolume: " + response.toString());
//                        updateJobStatusMap(mGson.fromJson(response.toString(), CameraOutput.class));
                    },
                    (error) -> {
                        Log.e(TAG, "setShutterVolume: FUCKED UP");
                    });
        } catch (JSONException e) {
            Log.e(TAG, "setShutterVolume: JSON fucked up", e);
        }

        mQueue.add(Objects.requireNonNull(request));
    }

    @Override
    public void getPhotoAsBytes(String url) {
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                (response) -> {
                    Log.d(TAG, "getPhotoAsBytes: bytes are " + response);
                    onPhotoAsBytesDownloadedAll(response);
                },
                (error) -> Log.e(TAG, "getPhotoAsBytes: test"),
                null);
        mQueue.add(Objects.requireNonNull(request));
    }

    private void updateJobStatusMap(CameraOutput output) {
        updateJobStatusMap(
                Objects.requireNonNull(output.getId(), "UpdateStatus: ID was null"),
                Objects.requireNonNull(output.getState(), "UpdateStatus: State was null")
        );
    }

    private void updateJobStatusMap(String id, String status) {
        mJobStatusMap.put(id, status);
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

    private void onTakePhotoInProgressAll(CameraOutput output) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onTakePhotoInProgress(output);
        }
    }

    private void onTakePhotoErrorAll(CameraOutput output) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onTakePhotoError(output);
        }
    }

    private void onTakePhotoDoneAll(CameraOutput output) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onTakePhotoDone(output);
        }
    }

    private void onPhotoAsBytesDownloadedAll(byte[] photo) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onPhotoAsBytesDownloaded(photo);
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
