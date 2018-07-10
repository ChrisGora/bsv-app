package uk.ac.bris.cs.bristolstreetview;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PermissionChecker {

    private static final String TAG = "PermissionChecker";

    private Context mContext;
    private Activity mActivity;
    private List<String> mPermissions;


    PermissionChecker(Context context, Activity activity, List<String> permissions) {
        mContext = context;
        mActivity = activity;
        mPermissions = permissions;
    }

    public void checkPermissions() {
        List<String> notGranted = new ArrayList<>();
        for (String permission : mPermissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, permission + " NOT granted");
                notGranted.add(permission);
            } else {
                Log.v(TAG, permission + " granted");
            }
        }
        if (!notGranted.isEmpty()) ActivityCompat.requestPermissions(mActivity, notGranted.toArray(new String[] {}), 0);
    }

}
