package uk.ac.bris.cs.bristolstreetview;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mManualButton;
    private Button mAutoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAllViews();
        setAllOnClickListeners();

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.INTERNET);
        permissions.add((Manifest.permission.READ_EXTERNAL_STORAGE));
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        PermissionChecker permissionChecker = new PermissionChecker(this, this, permissions);
        permissionChecker.checkPermissions();
    }

    private void findAllViews() {
        mManualButton = findViewById(R.id.manual_button);
        mAutoButton = findViewById(R.id.auto_button);
    }

    private void setAllOnClickListeners() {
        mManualButton.setOnClickListener((view) -> {
            startActivity(new Intent(MainActivity.this, SinglePhotoActivity.class));
        });
        mAutoButton.setOnClickListener((view) -> {
            startActivity(new Intent(MainActivity.this, AutomaticPhotoActivity.class));
        });
    }
}
