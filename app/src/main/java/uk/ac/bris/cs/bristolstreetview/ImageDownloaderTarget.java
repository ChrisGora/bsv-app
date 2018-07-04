package uk.ac.bris.cs.bristolstreetview;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageDownloaderTarget implements Target {

    private static final String TAG = "ImageDownloaderTarget";
    private final String mUrl;

    public ImageDownloaderTarget(String url) {
        mUrl = url;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        //                new Thread( () -> {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + mUrl);
        try {
            Log.d(TAG, "onBitmapLoaded: HERE!!!!!!");
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//                }).run();
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        Log.e(TAG, "onBitmapFailed: HERE 1");
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        Log.e(TAG, "onPrepareLoad: HERE 2");
    }
}
