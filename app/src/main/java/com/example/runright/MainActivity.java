package com.example.runright;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_Camera = 1;
    private static final int REQUEST_CODE_VideoFramePicker = 2;
    private static final int REQUEST_CODE_PoseEstimation = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    private Button buttonCamera;
    private Button buttonVideoFramePicker;
    private Button buttonPoseEstimation;
    private Button buttonLog;

    private void clearSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("ImageURIStore", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // This will clear all the data in SharedPreferences
        editor.apply(); // Commit the changes
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        clearSharedPreferences();

        buttonCamera = findViewById(R.id.button_camera);
        buttonVideoFramePicker = findViewById(R.id.button_video_frame_picker);
        buttonPoseEstimation = findViewById(R.id.button_pose_estimation);
        buttonLog = findViewById(R.id.button_log);

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Request camera permission if not granted
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    // Permission has already been granted, start CameraActivity
                    startCameraActivity();
                }
//                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_Camera);
            }
        });

        buttonVideoFramePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VideoFramePickerActivity.class);
                startActivityForResult(intent, REQUEST_CODE_VideoFramePicker);
            }
        });

        buttonPoseEstimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PoseEstimationActivity.class);
                startActivityForResult(intent, REQUEST_CODE_PoseEstimation);
            }
        });

        buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogActivity.class);
                startActivity(intent);
            }
        });
    }

    private void startCameraActivity() {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        startActivityForResult(intent, REQUEST_CODE_Camera);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Camera permission granted");
                startCameraActivity();
            } else {
                Log.d("MainActivity", "Camera permission denied");
                // Consider providing feedback to the user
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_CODE_PoseEstimation && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("finalImageUri")) {
                String receivedUri = data.getStringExtra("finalImageUri");
                Log.d("test-received", receivedUri);
                Uri finalImageUri = Uri.parse(receivedUri);

                // Storing the URI in SharedPreferences
                SharedPreferences prefs = getSharedPreferences("ImageURIStore", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                int uriCount = prefs.getInt("count", 0);
                editor.putString("uri_" + uriCount, receivedUri);
                editor.putInt("count", uriCount + 1);
                editor.apply();
            }
        }
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_Camera && resultCode == RESULT_OK) {
//            if (data != null && data.getData() != null) {
//                Uri videoUri = data.getData();
//                // Save the videoUri.toString() to SharedPreferences or a database
//
//            }
//        }
//        else if (requestCode == REQUEST_CODE_PoseEstimation && resultCode == RESULT_OK) {
//            Uri finalImageUri = data.getData();
//        }
//    }


//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.d("test", "receive data1");
//        if (requestCode == REQUEST_CODE_PoseEstimation && resultCode == RESULT_OK) {
//            Log.d("test", "receive data2");
//            if (data != null && data.hasExtra("finalImageUri")) {
//                String receivedUri = data.getStringExtra("finalImageUri");
//                Uri finalImageUri = Uri.parse(receivedUri);
////            if (data != null && data.getData() != null) {
//                Log.d("test", "receive data3");
////                Uri finalImageUri = data.getData();
//                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
//                SharedPreferences.Editor editor = prefs.edit();
//                int historyCount = prefs.getInt("historyCount", 0) + 1;
//                editor.putString("history" + historyCount, finalImageUri.toString());
//                editor.putInt("historyCount", historyCount);
//                editor.apply();
//            }
//        }

        //        if (requestCode == REQUEST_CODE_Camera && resultCode == RESULT_OK) {
//            if (data != null && data.getData() != null) {
//                Uri videoUri = data.getData();
//                // Save the videoUri.toString() to SharedPreferences or a database
//            }
//        }
//    }

}