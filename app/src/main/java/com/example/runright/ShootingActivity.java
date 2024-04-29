package com.example.runright;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ShootingActivity extends AppCompatActivity {

    private Button buttonConfirm, buttonRetake;
    private ImageView imageViewPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting);

        // Initialize UI components
        buttonConfirm = findViewById(R.id.buttonConfirm);
        buttonRetake = findViewById(R.id.buttonRetake);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

        // Assuming the captured photo's URI is passed from MainActivity
        Uri photoUri = getIntent().getData();
        if (photoUri != null) {
            // Display the photo. In a real app, consider scaling the image to fit the ImageView.
            imageViewPhoto.setImageURI(photoUri);
        }

        // Handle "Confirm" button click
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to AnalysisActivity, passing the photo URI
                // Intent intent = new Intent(ShootingActivity.this, AnalysisActivity.class);

                // going into pose estimation activity instead
                Intent intent = new Intent(ShootingActivity.this, PoseEstimationActivity.class);

                intent.setData(photoUri); // Pass the photo's URI to AnalysisActivity
                startActivity(intent);
            }
        });

        // Handle "Retake" button click
        buttonRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simply finish this activity, returning to MainActivity for a retake
                finish();
            }
        });
    }
}
