package com.example.runright;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoFramePickerActivity extends Activity {

    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_POSE = 2;
    private VideoView videoView;
    private ImageView imageView;
    private Uri videoUri;
    private Button buttonHome;
    private Bitmap current_frame;
    private Uri final_image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_frame_picker);

        videoView = findViewById(R.id.videoView);
        imageView = findViewById(R.id.imageView);
        Button pickFrameButton = findViewById(R.id.pickFrameButton);
        Button selectVideoButton = findViewById(R.id.selectVideoButton);
        Button analyzeButton = findViewById(R.id.analyzeButton);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        selectVideoButton.setOnClickListener(v -> {
            Intent takeVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
//            }
        });

        pickFrameButton.setOnClickListener(v -> {
            // Check if the VideoView has a video loaded
            if (videoView.getDuration() <= 0) {
                Toast.makeText(getApplicationContext(), "No video loaded", Toast.LENGTH_SHORT).show();
            } else {
                // If there is a video, check if it is playing and then pause it
                if (videoView.isPlaying()) {
                    videoView.pause();
                    Log.d("VideoFramePicker", "Video paused");
                }
                current_frame = getCurrentFrame(); // Ensure this method fetches the current frame correctly
                imageView.setImageBitmap(current_frame);
                // saveFrameToFile(current_frame); // Uncomment or modify as necessary
                saveImageToGallery(current_frame); // Assuming this method handles saving the frame
            }
        });

        analyzeButton.setOnClickListener(v -> {
            if (final_image_uri == null) {
                Toast.makeText(this, "Please pick a frame for analysis", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(VideoFramePickerActivity.this, PoseEstimationActivity.class);
                intent.putExtra("imageUri", final_image_uri.toString());
                startActivityForResult(intent, REQUEST_POSE);
            }
        });




        buttonHome = findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to return data
//                Intent returnIntent = new Intent();
//                returnIntent.putExtra("resultKey", "Some data");
//                setResult(Activity.RESULT_OK, returnIntent);
                finish(); // This will close the current activity and return to the Main Activity
            }
        });

        Intent intent = getIntent();
        // Check if the intent has the extra "imageUri"
        if (intent.hasExtra("videoUri")) {
            String videoUriString = intent.getStringExtra("videoUri");
            videoUri = Uri.parse(videoUriString);
            videoView.setVideoURI(videoUri);
            videoView.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            videoUri = data.getData();
            videoView.setVideoURI(videoUri);
            videoView.start();
            Log.d("VideoFramePicker", "Video playback started");
        }
        else if (requestCode == REQUEST_POSE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            Log.d("VideoFramePicker", data.getStringExtra("finalImageUri"));
            finish();
        }
    }

//    private Bitmap getCurrentFrame() {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        Log.d("VideoFramePicker", "[2]");
//
//        retriever.setDataSource(this, videoUri);
//        Log.d("VideoFramePicker", "[3]");
//
//        return retriever.getFrameAtTime(0);
////        videoView.seekTo(videoView.getCurrentPosition());
////        return videoView.getDrawingCache();
//    }

    private Bitmap getCurrentFrame() {
        // Make sure you have a valid video URI
        if (videoUri == null) {
            Log.e("VideoFramePicker", "Video URI is not set");
            return null;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, videoUri);
            // Get the current position of the video in microseconds
            int currentPosition = videoView.getCurrentPosition();
            // Convert milliseconds to microseconds for getFrameAtTime()
            long frameTime = currentPosition * 1000L;
            Bitmap frame = retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

            if (frame == null) {
                Log.e("VideoFramePicker", "Failed to retrieve frame");
            } else {
                Log.d("VideoFramePicker", "Frame retrieved at position: " + currentPosition);
            }

            return frame;
        } catch (Exception e) {
            Log.e("VideoFramePicker", "Failed to retrieve frame", e);
            return null;
        } finally {
            try {
                retriever.release(); // Ensure the retriever is released to avoid memory leaks
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void saveImageToGallery(Bitmap bitmap) {
        // Create a file name with the current timestamp for uniqueness
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YourAppFolder");

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        Log.d("VideoFramePicker", "[3]");

        File imageFile = new File(storageDir, imageFileName);
        String savedImagePath = imageFile.getAbsolutePath();
        Log.d("VideoFramePicker", "[4]");

        // Check and request for the WRITE_EXTERNAL_STORAGE permission if necessary
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_READ_STORAGE_PERMISSION);
//        } else {
        if (success) {
            // Save the bitmap to the specified file
            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
//                Toast.makeText(this, "Frame Saved Successfully", Toast.LENGTH_SHORT).show();

                // Notify the media scanner of the new image so it appears in the gallery
                MediaScannerConnection.scanFile(getApplicationContext(),
                        new String[]{savedImagePath}, null,
                        (path, uri) -> {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                            final_image_uri = uri;
                        });

            } catch (IOException e) {
                Toast.makeText(this, "Failed to Save Frame", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
        }
//        }
    }

//    private void saveFrameToFile(Bitmap bitmap) {
//        try {
//            FileOutputStream out = new FileOutputStream("/storage/emulated/0/DCIM/Camera/frame.jpg");
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            out.flush();
//            out.close();
//            Log.d("VideoFramePicker", "Frame saved");
//        } catch (IOException e) {
//            Log.d("VideoFramePicker", "Error saving frame", e);
//        }
//    }
}