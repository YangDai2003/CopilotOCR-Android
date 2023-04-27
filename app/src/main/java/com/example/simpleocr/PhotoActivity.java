package com.example.simpleocr;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.color.DynamicColors;

public class PhotoActivity extends AppCompatActivity {
    PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_photo);

        photoView = findViewById(R.id.photoView);
        photoView.setOnClickListener(v -> this.finishAfterTransition());
        photoView.setImageURI(Uri.parse(getIntent().getStringExtra("uri")));
    }
}