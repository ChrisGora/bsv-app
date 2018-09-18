package uk.ac.bris.cs.bristolstreetview;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.Volley;
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

import static android.support.v4.app.NotificationCompat.PRIORITY_LOW;
import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class ConcreteCameraConnectorService extends Service implements CameraConnector {

    private static final String TAG = "CameraConnector";

    private List<CameraConnectorObserver> mObservers;

    private Gson mGson;
    private RequestQueue mQueue;
    private int mRequestsPending;

    private String mUrl;

    private Map<String, String> mJobStatusMap;

    private NotificationManager mNM;
    private int NOTIFICATION = R.string.local_CCC_service_started;

    private final IBinder mBinder = new LocalBinder();

//    private static CameraConnector instance;


    public class LocalBinder extends Binder {
        ConcreteCameraConnectorService getService() {
            return ConcreteCameraConnectorService.this;
        }
    }

//    ConcreteCameraConnectorService(String url) {
//
//    }


//    static CameraConnector getInstance() {
//        return instance;
//    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: >>>>>>>>>>>>>>>>>>> ON CREATE");
        Log.i(TAG, "onCreate: " + Thread.currentThread());

        Context context = getApplicationContext();

        // TODO: 25/07/18 Clicking the notification takes you back to the wrong activity

        Objects.requireNonNull(context, "getApplicationContext() returned null");

        mNM = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        mObservers = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        mGson = builder.create();
        mQueue = Objects.requireNonNull(Volley.newRequestQueue(context), "Queue was null");
        mRequestsPending = 0;
//        mUrl = Objects.requireNonNull(url);
        mJobStatusMap = new HashMap<>();

        Log.i(TAG, "onCreate: " + ConcreteCameraConnectorService.this);
//        instance = ConcreteCameraConnectorService.this;

        Log.v(TAG, "onCreate: DONE");
        Log.v(TAG, "Queue set!");

        showNotification(context);

    }



    private void showNotification(Context context) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_CCC_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(this, context.getClass()), 0);

        // FIXME: 30/07/18 Context get seems to always return automatic photo actvity

        Notification notification = getNotification(context);
        mNM.notify(NOTIFICATION, notification);
        startForeground(NOTIFICATION, notification);
    }

    public Notification getNotification(Context context) {
        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel(context);
        else {
            channel = "";
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AutomaticPhotoActivity.class), 0);

        CharSequence text = getText(R.string.local_CCC_service_started);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channel).setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentTitle("snap map fake location");
        Notification notification = mBuilder
                .setPriority(PRIORITY_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setSmallIcon(R.drawable.googleg_standard_color_18)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_CCC_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();


        return notification;
    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "snap map fake location ";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel("snap map channel", name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return "snap map channel";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: >>>>>>>>>>> received start " + intent + " flags: " + flags + " id: " + startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        mNM.cancel(NOTIFICATION);
        stopForeground(true);
        Toast.makeText(this, R.string.local_CCC_service_stopped, Toast.LENGTH_LONG).show();
//        instance = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setUrl(String url) {
        mUrl = Objects.requireNonNull(url, "URL supplied was null");
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

    @Override
    public void deleteAll() {
        CameraCommand deleteCommand = new CameraCommand();
        deleteCommand.setName("camera.delete");
        deleteCommand.setParameters(new Parameters());
        deleteCommand.getParameters().setFileUrls(new String[]{"all"});

        String deleteJsonCommand = mGson.toJson(deleteCommand);

        Log.d(TAG, "delete All: " + deleteJsonCommand);

        String url = mUrl + "/osc/commands/execute";
        JsonObjectRequest request = null;
        try {
            request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(deleteJsonCommand),
                    (response) -> {
                        Log.d(TAG, "Delete All: " + response.toString());
                    },
                    (error) -> {
                        Log.e(TAG, "Delete All: FUCKED UP", error);
                    });
        } catch (JSONException e) {
            Log.e(TAG, "delete All: JSON fucked up", e);
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

        JsonObjectRequest request = getShutterVolumeRequest(setVolumeJsonCommand);

        mQueue.add(Objects.requireNonNull(request));
    }

    @Nullable
    private JsonObjectRequest getShutterVolumeRequest(String setVolumeJsonCommand) {
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
        return request;
    }

    @Override
    public void requestDownloadPhotoAsBytes(PhotoRequest photoRequest) {
        InputStreamVolleyRequest request = getDownloadRequest(photoRequest);
        mQueue.add(Objects.requireNonNull(request));
    }

    @NonNull
    private InputStreamVolleyRequest getDownloadRequest(PhotoRequest photoRequest) {
        String url = photoRequest.getCameraUrl();
        return new InputStreamVolleyRequest(Request.Method.GET, url,
                (response) -> {
//                    Log.d(TAG, "requestDownloadPhotoAsBytes: bytes are " + Arrays.toString(response));
                    onPhotoAsBytesDownloadedAll(photoRequest, response);
                },
                (error) -> Log.e(TAG, "requestDownloadPhotoAsBytes: test"),
                null);
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
