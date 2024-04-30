package com.example.runright;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PoseEstimationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView imageView;
    private Interpreter tflite;
    private Button buttonHome;
    private static final int[][] KEYPOINT_CONNECTIONS = {
            {1, 2}, // left_eye to right_eye
            {2, 4}, // right_eye to right_ear
            {1, 3}, // left_eye to left_ear
            {5, 6}, // left_shoulder to right_shoulder
            {6, 8}, // right_shoulder to right_elbow
            {8, 10}, // right_elbow to right_wrist
            {5, 7}, // left_shoulder to left_elbow
            {7, 9}, // left_elbow to left_wrist
            {11, 12}, // left_hip to right_hip
            {12, 14}, // right_hip to right_knee
            {14, 16}, // right_knee to right_ankle
            {11, 13}, // left_hip to left_knee
            {13, 15}, // left_knee to left_ankle
            {5, 11}, // left_shoulder to left_hip
            {6, 12}  // right_shoulder to right_hip
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pose_estimation);

        imageView = findViewById(R.id.imageView);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
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
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            // start here when combining, this is where you get the frame
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri);

            // load the model
            try {
                MappedByteBuffer tfliteModel
                        = FileUtil.loadMappedFile(this, "4.tflite");
                tflite = new Interpreter(tfliteModel);
            } catch (IOException e) {
                Log.e("tfliteSupport", "Error reading model", e);
            }

            // convert the Uri to a bitmap then preprocess to fit the ts model rq
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            saveImageToGallery(bitmap);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 192, 192, true);

            // then convert the bitmap to bytebuffer
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * 192 * 192 * 3);
            inputBuffer.order(ByteOrder.nativeOrder());
            inputBuffer.rewind();
            for (int y = 0; y < 192; y++) {
                for (int x = 0; x < 192; x++) {
                    int pixelValue = scaledBitmap.getPixel(x, y);
                    inputBuffer.put((byte) ((pixelValue >> 16) & 0xFF)); // Red channel
                    inputBuffer.put((byte) ((pixelValue >> 8) & 0xFF));  // Green channel
                    inputBuffer.put((byte) (pixelValue & 0xFF));         // Blue channel
                }
            }

            // run the model
            float[][][][] outputBuffer = new float[1][1][17][3]; // MoveNet outputs 17 keypoints
            if (tflite != null) {
                tflite.run(inputBuffer, outputBuffer);
            } else {
                Log.e("TFLite Error", "TensorFlow Lite Interpreter is null");
            }

            tflite.run(inputBuffer, outputBuffer);

            // print the output
            for (int i = 0; i < 17; i++) {
                float x = outputBuffer[0][0][i][1];
                float y = outputBuffer[0][0][i][0];
                float confidence = outputBuffer[0][0][i][2];

                Log.i("MoveNet", "Keypoint " + i + ": (" + x + ", " + y + ") Confidence: " + confidence);
            }
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(10);
            Paint linePaint = new Paint();
            linePaint.setColor(Color.GREEN);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(8);

            float[][] points = new float[17][2]; // Store keypoint coordinates for line drawing
            for (int i = 0; i < 17; i++) {
                float x = outputBuffer[0][0][i][1] * width;
                float y = outputBuffer[0][0][i][0] * height;
                float confidence = outputBuffer[0][0][i][2];
                points[i][0] = x;
                points[i][1] = y;

                if (confidence > 0) {
                    canvas.drawCircle(x, y, 10, paint);
                }
            }

            // Draw lines between keypoints
            for (int[] pair : KEYPOINT_CONNECTIONS) {
                float confidence1 = outputBuffer[0][0][pair[0]][2];
                float confidence2 = outputBuffer[0][0][pair[1]][2];
                if (confidence1 > 0 && confidence2 > 0) {
                    canvas.drawLine(points[pair[0]][0], points[pair[0]][1],
                            points[pair[1]][0], points[pair[1]][1], linePaint);
                }
            }

            imageView.setImageBitmap(mutableBitmap);
        }
    }

//    private void saveImageToGallery(Bitmap bitmap) {
//        // Create a file name with the current timestamp for uniqueness
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + ".jpg";
//        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YourAppFolder");
//
//        boolean success = true;
//        if (!storageDir.exists()) {
//            success = storageDir.mkdirs();
//        }
//        Log.d("VideoFramePicker", "[3]");
//
//        File imageFile = new File(storageDir, imageFileName);
//        String savedImagePath = imageFile.getAbsolutePath();
//        Log.d("VideoFramePicker", "[4]");
//
//        // Check and request for the WRITE_EXTERNAL_STORAGE permission if necessary
////        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
////                != PackageManager.PERMISSION_GRANTED) {
////            ActivityCompat.requestPermissions(this,
////                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
////                    REQUEST_READ_STORAGE_PERMISSION);
////        } else {
//        if (success) {
//            // Save the bitmap to the specified file
//            try (FileOutputStream out = new FileOutputStream(imageFile)) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                out.flush();
//                Toast.makeText(this, "Frame Saved Successfully", Toast.LENGTH_SHORT).show();
//
//                // Notify the media scanner of the new image so it appears in the gallery
//                MediaScannerConnection.scanFile(getApplicationContext(),
//                        new String[]{savedImagePath}, null,
//                        (path, uri) -> {
//                            Log.i("ExternalStorage", "Scanned " + path + ":");
//                            Log.i("ExternalStorage", "-> uri=" + uri);
//                        });
//
//            } catch (IOException e) {
//                Toast.makeText(this, "Failed to Save Frame", Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
//            }
//        } else {
//            Toast.makeText(this, "Failed to create directory", Toast.LENGTH_SHORT).show();
//        }
////        }
//    }
}