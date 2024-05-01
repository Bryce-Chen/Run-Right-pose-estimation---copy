package com.example.runright;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_PICKER = 2;

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri videoUri = data.getData();
                Log.d("test-uri", videoUri.toString());
                String path = getRealPathFromURI(this, videoUri);
                Log.d("test-path", path);

                // Create intent to start VideoFramePickerActivity with videoUri
                Intent intent = new Intent(this, VideoFramePickerActivity.class);
                intent.putExtra("videoUri", path);
                startActivityForResult(intent, REQUEST_PICKER);
//                Intent returnIntent = new Intent();
//                returnIntent.putExtra("videoUri", path);
//                setResult(RESULT_OK, returnIntent);
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
//            finish(); // Finish CameraActivity
        }
        else if (requestCode == REQUEST_PICKER) {
            if (resultCode == RESULT_OK && data != null) {
                // Possibly do something with the data returned from VideoFramePickerActivity
                // E.g., return data to a previous activity
                setResult(RESULT_OK, data);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        String result = null;
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                result = cursor.getString(column_index);
            }
        } catch (Exception e) {
            // Handle exception here
            Log.e("TAG", "Failed to get real path", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }



}
