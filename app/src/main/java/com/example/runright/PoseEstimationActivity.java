package com.example.runright;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
    private Uri finalImageUri = null;
    private Button buttonAnalysis;
    private Uri imageUri;
    private String analysis;
    float[][] points;
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

//        // Initialize your button
//        Button buttonHome = findViewById(R.id.button_home);
//        Drawable background = buttonHome.getBackground();
//        int paddingTop = buttonHome.getPaddingTop();
//
//        // Log the details
//        Log.d("ButtonStyleCheck", "Background: " + background + ", Padding Top: " + paddingTop);
//
//        // Do the same for other buttons if needed
//        Button buttonAnalysis = findViewById(R.id.button_analysis);
//        background = buttonAnalysis.getBackground();
//        paddingTop = buttonAnalysis.getPaddingTop();
//        Log.d("ButtonStyleCheck", "Background: " + background + ", Padding Top: " + paddingTop);
//
//        float scale = getResources().getDisplayMetrics().density;
//        int expectedPaddingDp = 16;  // As defined in your style
//        int expectedPaddingPx = (int) (expectedPaddingDp * scale + 0.5f);
//        Log.d("ButtonStyleCheck", "Expected Padding in Pixels: " + expectedPaddingPx);
//
//        int paddingLeft = buttonHome.getPaddingLeft();
//        int paddingRight = buttonHome.getPaddingRight();
//        int paddingBottom = buttonHome.getPaddingBottom();
//        Log.d("ButtonStyleCheck", "Padding - Top: " + paddingTop + ", Left: " + paddingLeft + ", Right: " + paddingRight + ", Bottom: " + paddingBottom);
//
//        // After setting content view and initializing button
//        Log.d("InitialPadding", "Initial - Top: " + buttonHome.getPaddingTop() + ", Bottom: " + buttonHome.getPaddingBottom());
//
//// Any other code that might influence padding
//// For example, checking conditionally applied styles or layout changes
//
//// Log again after potential changes
//        Log.d("FinalPadding", "Final - Top: " + buttonHome.getPaddingTop() + ", Bottom: " + buttonHome.getPaddingBottom());
//

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
                if (finalImageUri != null) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("finalImageUri", finalImageUri.toString());
                    Log.d("test", "send data");
                    Log.d("test", finalImageUri.toString());

                    setResult(RESULT_OK, returnIntent);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish(); // This will close the current activity and return to the Main Activity
            }
        });

        buttonAnalysis = findViewById(R.id.button_analysis);
        // on analysis click, check if there is an image present.
        // if there is no image, prompt to select an image.
        // if there is image present, analyze the image
        buttonAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri == null) {
                    Toast.makeText(v.getContext(), "Please choose an image by clicking the screen.", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent(PoseEstimationActivity.this, AnalysisActivity.class);
                    intent.putExtra("analysis", analysis);
                    startActivity(intent);
                }
            }
        });

        Intent intent = getIntent();

        // Check if the intent has the extra "imageUri"
        if (intent.hasExtra("imageUri")) {
            String imageUriString = intent.getStringExtra("imageUri");
            imageUri = Uri.parse(imageUriString);
            analyze();
        }

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
            imageUri = data.getData();
//            finalImageUri = imageUri;
            analyze();

        }

    }

    private void analyze () {
        TextView ins =findViewById(R.id.textViewInstructions);
        ins.setVisibility(View.GONE);

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
        saveImageToGallery(bitmap);
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

        points = new float[17][2]; // Store keypoint coordinates for line drawing
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

        // start analysis of the form
        analysis = "";

        float bodyLean = Math.abs(90 - calculateTiltAngle(
                midPoint(points[5], points[6]), // midpoint between leftShoulder and rightShoulder
                midPoint(points[11], points[12]) // midpoint between leftHip and rightHip
        ));

        // Calculate left elbow angle
        float leftElbowAngle = Math.abs(calculateAngle(points[5], points[7], points[9]));  // leftShoulder, leftElbow, leftWrist

        // Calculate right elbow angle
        float rightElbowAngle = Math.abs(calculateAngle(points[6], points[8], points[10])); // rightShoulder, rightElbow, rightWrist

        // Calculate left knee angle
        float leftKneeAngle = calculateAngle(points[11], points[13], points[15]); // leftHip, leftKnee, leftAnkle

        // Calculate right knee angle
        float rightKneeAngle = calculateAngle(points[12], points[14], points[16]); // rightHip, rightKnee, rightAnkle

        float strideAngle = calculateAngle(points[13], midPoint(points[11], points[12]), points[14]);

        analysis += "Body Lean: " + bodyLean + "\n";
        analysis = analysis + "The recommended angle of torso lean is between 6 - 10 degrees.\n";
        if (bodyLean > 10) {
            analysis += "Your body is leaning too far forward/backward, so consider straighten up a bit.\n";
        } else if (bodyLean < 6) {
            analysis += "Your body is too straight, consider leaning forward a bit.\n";
        } else {
            analysis += "Your body is leaning at a good angle.\n\n";
        }

        analysis += "Left elbow angle: " + leftElbowAngle + "\n";
        analysis += "Recommended elbow angle is between 70 - 110 degrees.\n";
        if (leftElbowAngle < 70) {
            analysis += "Your left elbow is too straight, consider bending it a bit more.\n";
        } else if (leftElbowAngle > 110) {
            analysis += "Your left elbow is bent too much, consider straightening it a bit.\n";
        } else {
            analysis += "Your left elbow angle is perfect.\n";
        }
        analysis += "Right elbow angle: " + rightElbowAngle + "\n";
        if (rightElbowAngle < 70) {
            analysis += "Your right elbow is too straight, consider bending it a bit more.\n";
        } else if (rightElbowAngle > 110) {
            analysis += "Your right elbow is bent too much, consider straightening it a bit.\n";
        } else {
            analysis += "Your right elbow angle is perfect.\n";
        }

        analysis += "\nLeft knee angle: " + leftKneeAngle + "\n";
        analysis += "Recommended knee angle is between 90 - 160 degrees.\n";
        if (leftKneeAngle < 90) {
            analysis += "Your left knee is too straight, consider bending it more.\n";
        } else if (leftKneeAngle > 160) {
            analysis += "Your left knee is bent too much, consider straightening it.\n";
        } else {
            analysis += "Your left knee angle is optimal.\n";
        }

        analysis += "Right knee angle: " + rightKneeAngle + "\n";
        if (rightKneeAngle < 90) {
            analysis += "Your right knee is too straight, consider bending it more.\n";
        } else if (rightKneeAngle > 160) {
            analysis += "Your right knee is bent too much, consider straightening it.\n";
        } else {
            analysis += "Your right knee angle is optimal.\n";
        }

        analysis += "\nStride angle: " + strideAngle + "\n";
        analysis += "Recommended stride angle is between 60 - 65 degrees.\n";
        if (strideAngle < 60) {
            analysis += "Your stride angle is too narrow, consider widening your stride slightly.\n";
        } else if (strideAngle > 65) {
            analysis += "Your stride angle is too wide, consider narrowing your stride slightly.\n";
        } else {
            analysis += "Your stride angle is ideal.\n";
        }

        System.out.println(analysis);
    }

    public static float calculateAngle(float[] a, float[] b, float[] c) {
        // Calculate the vectors from point b to point a and c
        float[] ba = {a[0] - b[0], a[1] - b[1]};
        float[] bc = {c[0] - b[0], c[1] - b[1]};

        // Calculate the dot product of vectors ba and bc
        float dotProduct = ba[0] * bc[0] + ba[1] * bc[1];

        // Calculate the magnitude of vector ba and bc
        float magnitudeBA = (float) Math.sqrt(ba[0] * ba[0] + ba[1] * ba[1]);
        float magnitudeBC = (float) Math.sqrt(bc[0] * bc[0] + bc[1] * bc[1]);

        // Calculate the cosine of the angle using the dot product and magnitude of vectors
        float cosineAngle = dotProduct / (magnitudeBA * magnitudeBC);

        // Calculate the angle in radians and then convert it to degrees
        float angle = (float) Math.acos(cosineAngle);
        float angleInDegrees = (float) Math.toDegrees(angle);

        // Ensure the angle is always less than 180 degrees
        if (angleInDegrees > 180) {
            angleInDegrees = 360 - angleInDegrees;
        }

        return angleInDegrees;
    }



    // Method to calculate the tilt angle with respect to vertical axis
    private static float calculateTiltAngle(float[] point1, float[] point2) {
        float angle = (float) Math.atan2(point2[1] - point1[1], point2[0] - point1[0]);
        float degreeAngle = (float) Math.toDegrees(angle);
        return Math.abs(degreeAngle);  // Return the absolute angle in degrees
    }

    // Helper method to find the midpoint between two points
    private static float[] midPoint(float[] point1, float[] point2) {
        return new float[] {
                (point1[0] + point2[0]) / 2,
                (point1[1] + point2[1]) / 2
        };
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
                            finalImageUri = uri;
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
}
