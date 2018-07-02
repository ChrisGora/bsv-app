package uk.ac.bris.cs.bristolstreetview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class SinglePhotoActivity extends AppCompatActivity {

    private static final String TAG = "SinglePhotoActivity";

    private Button mTakePhotoButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_photo);
        findAllViews();
        setAllOnClickListeners();
    }

    private void findAllViews() {
        mTakePhotoButton = findViewById(R.id.take_photo_button);
    }

    private void setAllOnClickListeners() {
        mTakePhotoButton.setOnClickListener((view) -> {
            Log.v(TAG, "Button pressed");
        });
    }



}
