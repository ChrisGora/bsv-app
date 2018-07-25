package uk.ac.bris.cs.bristolstreetview;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ConcreteCameraConnector extends Service implements CameraConnector {

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
    public void sendTakePhotoRequest(PhotoRequest photoRequest) {
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
                             onTakePhotoInProgressAll(photoRequest, cameraOutput);
                             setStatusListener(photoRequest, cameraOutput.getId());
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

        mQueue.add(Objects.requireNonNull(request, "Request was null"));
    }

    private void checkStatus(PhotoRequest photoRequest, String id) {

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
                                onTakePhotoDoneAll(photoRequest, cameraOutput);
                                updateJobStatusMap(id, state);
                                break;
                            }
                            case "inProgress":      {
                                Log.d(TAG, "checkStatus: PROGRESS");
                                onTakePhotoInProgressAll(photoRequest, cameraOutput);
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

    private void setStatusListener(PhotoRequest photoRequest, String id) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(() -> {
            if (mJobStatusMap.get(id).equals("inProgress")) checkStatus(photoRequest, id);
            else executor.shutdownNow();
        },
                100,
                1000,
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
    public void requestDownloadPhotoAsBytes(PhotoRequest photoRequest) {
        String url = photoRequest.getCameraUrl();
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                (response) -> {
                    Log.d(TAG, "requestDownloadPhotoAsBytes: bytes are " + Arrays.toString(response));
                    onPhotoAsBytesDownloadedAll(photoRequest, response);
                },
                (error) -> Log.e(TAG, "requestDownloadPhotoAsBytes: test"),
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

    private void onTakePhotoInProgressAll(PhotoRequest photoRequest, CameraOutput output) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onTakePhotoInProgress(photoRequest, output);
        }
    }

    private void onTakePhotoErrorAll(PhotoRequest photoRequest, CameraOutput output) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onTakePhotoError(photoRequest, output);
        }
    }

    private void onTakePhotoDoneAll(PhotoRequest photoRequest, CameraOutput output) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onTakePhotoDone(photoRequest, output);
        }
    }

    private void onPhotoAsBytesDownloadedAll(PhotoRequest photoRequest, byte[] photo) {
        for (CameraConnectorObserver observer : mObservers) {
            observer.onPhotoAsBytesDownloaded(photoRequest, photo);
        }
    }
}
