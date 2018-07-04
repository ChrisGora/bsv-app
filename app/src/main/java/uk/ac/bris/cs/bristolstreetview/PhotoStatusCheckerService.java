/*
package uk.ac.bris.cs.bristolstreetview;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class PhotoStatusCheckerService extends IntentService {

    private static final String TAG = "PhotoStatusCheckerService";
    private static final String EXTRA_ID = "uk.ac.bris.cs.bristolstreetview.id";

    public PhotoStatusCheckerService() {
        super("PhotoStatusCheckerService");
    }

    public static Intent newCheckerIntent(Context packageContext, String id) {
        Intent intent = new Intent(packageContext, PhotoStatusCheckerService.class);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String id = Objects.requireNonNull(intent).getStringExtra(EXTRA_ID);

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
                        onTakePhotoInProgressAll(cameraOutput);
                    },
                    (error) -> {
                        Log.e(TAG, "sendTakePhotoRequest: FUCKED UP");
                    });
        } catch (JSONException e) {
            Log.e(TAG, "sendTakePhotoRequest: JSON fucked up", e);
        }

        mQueue.add(Objects.requireNonNull(request));
    }

}
*/
