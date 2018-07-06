package uk.ac.bris.cs.bristolstreetview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mManualButton;
    private Button mAutoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAllViews();
        setAllOnClickListeners();

    }

    private void findAllViews() {
        mManualButton = findViewById(R.id.manual_button);
        mAutoButton = findViewById(R.id.auto_button);
    }

    private void setAllOnClickListeners() {
        mManualButton.setOnClickListener((view) -> {
            startActivity(new Intent(MainActivity.this, SinglePhotoActivity.class));
        });
    }

}
