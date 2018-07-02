package uk.ac.bris.cs.bristolstreetview;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

class CameraConnector {

    private static final String TAG = "CameraConnector";

    private RequestQueue mQueue;
    private String mUrl;

    CameraConnector(RequestQueue queue, String url) {
        mQueue = queue;
        mUrl = url;
        Log.v(TAG, "Queue and URL set!");
    }

    CameraInfo getCameraInfo() {
        String url = mUrl + "/osc/info";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                (response) -> Log.v(TAG, response.toString()),
                (response) -> Log.e(TAG, "That didn't work :-(")
        );
        mQueue.add(request);
        return null; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
    }

}
