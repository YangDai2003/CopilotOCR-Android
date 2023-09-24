package com.example.simpleocr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.color.DynamicColors;
import com.yangdai.imageviewpro.ImageViewPro;

/**
 * @author 30415
 */
public class PhotoActivity extends AppCompatActivity {
    ImageViewPro photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_photo);

        photoView = findViewById(R.id.photoView);
        Glide.with(this).load(getIntent().getStringExtra("uri")).into(photoView);
        photoView.setOnClickListener(v -> this.finishAfterTransition());
    }
}