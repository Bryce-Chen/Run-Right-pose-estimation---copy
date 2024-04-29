package com.example.runright;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class LogActivity extends AppCompatActivity {

    private Button closeButton;
    private ListView listViewLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        // Initialize UI components
        closeButton = findViewById(R.id.closeButton);
        listViewLogs = findViewById(R.id.listViewLogs);

        // Sample data for demonstration purposes
        String[] sampleLogs = new String[]{
                "Analysis 1",
                "Analysis 2",
                "Analysis 3",
                // Add more sample data as needed
        };

        // Setting up the ListView with an ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, sampleLogs);
        listViewLogs.setAdapter(adapter);

        // Handling the click event of the close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                Intent intent = new Intent(LogActivity.this, MainActivity.class);
                // Consider using FLAG_ACTIVITY_CLEAR_TOP to clear the activity stack
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // Handle list item clicks (if navigation to AnalysisActivity is required)
        listViewLogs.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(LogActivity.this, AnalysisActivity.class);
            // Optionally, put extra data into the intent, such as the selected log's details
            intent.putExtra("logDetails", sampleLogs[position]);
            startActivity(intent);
        });
    }
}
