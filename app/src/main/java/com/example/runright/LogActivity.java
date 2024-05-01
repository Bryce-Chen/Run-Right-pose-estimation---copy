package com.example.runright;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {

    private List<String> imageUris;
    private ListView listView;
    private TextView placeholderText;
    private Button clearHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        listView = findViewById(R.id.listView);
        placeholderText = findViewById(R.id.placeholder_text);
        clearHistoryButton = findViewById(R.id.clear_history_button);
        imageUris = new ArrayList<>();

        loadImages();

        clearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory();
            }
        });

        Button buttonHome = findViewById(R.id.button_home);
        buttonHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // This will close the current activity and return to the Main Activity
            }
        });
    }

    private void loadImages() {
        SharedPreferences prefs = getSharedPreferences("ImageURIStore", MODE_PRIVATE);
        int count = prefs.getInt("count", 0);
        imageUris.clear();
        for (int i = 0; i < count; i++) {
            String uri = prefs.getString("uri_" + i, null);
            if (uri != null && !uri.isEmpty()) {
                imageUris.add(uri);
            }
        }

        updateUI();
    }

    private void clearHistory() {
        SharedPreferences prefs = getSharedPreferences("ImageURIStore", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        imageUris.clear();
        updateUI();
    }

    private void updateUI() {
        if (imageUris.isEmpty()) {
            placeholderText.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            placeholderText.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            ImageListAdapter adapter = new ImageListAdapter(this, imageUris);
            listView.setAdapter(adapter);
        }
    }
}