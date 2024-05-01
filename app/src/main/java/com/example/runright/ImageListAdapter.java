package com.example.runright;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ImageListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> images;

    public ImageListAdapter(@NonNull Context context, List<String> images) {
        super(context, R.layout.list_item, images);
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.history_text);
        ImageView imageView = convertView.findViewById(R.id.image_view);

        String imageUriString = getItem(position);
        Uri imageUri = Uri.parse(imageUriString);

        textView.setText("History " + (position + 1));
        imageView.setImageURI(imageUri);

        // Setting up the click listener for the whole row
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PoseEstimationActivity.class);
                intent.putExtra("imageUri", imageUriString);
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}

/*
public class ImageListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> images;

    public ImageListAdapter(@NonNull Context context, List<String> images) {
        super(context, R.layout.list_item, images);
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.history_text);
        ImageView imageView = convertView.findViewById(R.id.image_view);

        textView.setText("History " + (position + 1));
        Uri imageUri = Uri.parse(getItem(position));
        imageView.setImageURI(imageUri);

        return convertView;
    }
}


 */