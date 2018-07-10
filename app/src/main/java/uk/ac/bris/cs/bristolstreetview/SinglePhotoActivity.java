package uk.ac.bris.cs.bristolstreetview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
//import javax.inject.Inject;

public class SinglePhotoActivity extends AppCompatActivity implements PhotoTakerObserver {

    private static final String TAG = "SinglePhotoActivity";

    private Button mTakePhotoButton;
    private Button mUpdateInfoButton;
    private Button mUpdateStateButton;
    private Button mMuteButton;
    private Button mFullVolumeButton;
    private ImageView mResponseImageView;

    private PhotoTaker mPhotoTaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo);

        mPhotoTaker = new ConcretePhotoTaker(this);
        mPhotoTaker.registerObserver(this);

        findAllViews();
        setAllOnClickListeners();
    }

    private void findAllViews() {
        mTakePhotoButton = findViewById(R.id.take_photo_button);
        mUpdateInfoButton = findViewById(R.id.update_info_button);
        mUpdateStateButton = findViewById(R.id.update_state_button);
        mMuteButton = findViewById(R.id.mute_button);
        mFullVolumeButton = findViewById(R.id.full_volume_button);

        mResponseImageView = findViewById(R.id.response_image_view);
    }

    private void setAllOnClickListeners() {
        mTakePhotoButton.setOnClickListener((view) -> {
            Log.v(TAG, "Take photo pressed");
            mPhotoTaker.sendTakePhotoRequest();
        });

        mUpdateInfoButton.setOnClickListener((view) -> {
            Log.v(TAG, "Update info pressed");
            mPhotoTaker.updateCameraInfo();
        });

        mUpdateStateButton.setOnClickListener((view) -> {
            Log.v(TAG, "Update state pressed");
            mPhotoTaker.updateCameraState();
        });

        mMuteButton.setOnClickListener((view) -> {
            Log.v(TAG, "Mute pressed");
            mPhotoTaker.setShutterVolume(0);
        });

        mFullVolumeButton.setOnClickListener((view) -> {
            Log.v(TAG, "Full volume pressed");
            mPhotoTaker.setShutterVolume(100);
        });
    }

    @Override
    public void onPhotoTaken(String url) {
        Log.d(TAG, "onPhotoTaken: Got a url...");
        displayImage(url);
    }


    private void displayImage(String url) {
        Picasso
                .get()
                .load(url)
                .resize(500, 500)
                .into(mResponseImageView);
    }

    @Override
    public void onPhotoSavedAndProcessed(String fullPath) {
        Log.i(TAG, "onPhotoSavedAndProcessed: DONE!!!");
    }
}
