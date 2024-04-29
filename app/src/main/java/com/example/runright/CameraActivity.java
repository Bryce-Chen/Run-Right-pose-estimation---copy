package com.example.runright;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openCameraForVideo();
    }

    private void openCameraForVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        } else {
            // If there's no camera activity to handle the intent, finish with an error.
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_VIDEO_CAPTURE) {
//            if (resultCode == RESULT_OK && data != null) {
//                // Optionally handle the captured video data
//                setResult(RESULT_OK, data);
//            } else {
//                setResult(RESULT_CANCELED);
//            }
//            finish(); // Ensure this call is made to close the CameraActivity
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri videoUri = data.getData();

                // Create intent to start VideoFramePickerActivity with videoUri
//                Intent intent = new Intent(this, VideoFramePickerActivity.class);
//                intent.putExtra("videoUri", videoUri.toString());
//                startActivity(intent);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("videoUri", videoUri.toString());
                setResult(RESULT_OK, returnIntent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish(); // Finish CameraActivity
        }
    }


}
