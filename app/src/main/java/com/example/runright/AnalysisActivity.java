package com.example.runright;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AnalysisActivity extends AppCompatActivity {

    private Button closeAnalysisButton;
    private TextView textViewAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        // Initialize UI components
        closeAnalysisButton = findViewById(R.id.closeAnalysisButton);
        textViewAnalysis = findViewById(R.id.textViewAnalysis);

        // Mock analysis result. In a real application, you would analyze the photo here.
        String analysisResult = performPhotoAnalysis();

        // Display the analysis result
        textViewAnalysis.setText(analysisResult);

        // Handle the close button click
        closeAnalysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                Intent intent = new Intent(AnalysisActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    // Placeholder for a photo analysis function
    private String performPhotoAnalysis() {
        // In a real app, you'd analyze the photo here and return the results.
        // This is just a placeholder to simulate analysis.
        return "Analysis Result: TBD";
    }
}
