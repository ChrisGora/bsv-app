package uk.ac.bris.cs.bristolstreetview;

// Adapted from https://stackoverflow.com/questions/39051966/how-to-download-video-file-using-volley-in-android

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import java.util.HashMap;
import java.util.Map;

public class InputStreamVolleyRequest extends Request<byte[]> {

    private static final String TAG = "InputStreamVolleyReq";

    private final Response.Listener<byte[]> mListener;
//    private final Response.Listener<String> mContentDispositionListener;
    private Map<String, String> mParams;
    //create a static map for directly accessing headers
    public Map<String, String> responseHeaders;

    public InputStreamVolleyRequest(int post, String mUrl, Response.Listener<byte[]> listener,
//                                    Response.Listener<String> contentDispositionListener,
                                    Response.ErrorListener errorListener, HashMap<String, String> params) {
        // TODO Auto-generated constructor stub

        super(post, mUrl, errorListener);
        // this request would never use cache.
        setShouldCache(false);
        mListener = listener;
//        mContentDispositionListener = contentDispositionListener;
        mParams = params;
    }

    @Override
    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return mParams;
    }


    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
//        mContentDispositionListener.onResponse(responseHeaders.get("Content-Type"));
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {

        //Initialise local responseHeaders map with response headers received
        responseHeaders = response.headers;

        Log.d(TAG, "parseNetworkResponse: " + responseHeaders);

        //Pass the response data here
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}