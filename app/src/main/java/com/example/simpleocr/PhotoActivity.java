package com.example.simpleocr;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.yangdai.imageviewpro.ImageViewPro;

/**
 * @author 30415
 */
public class PhotoActivity extends AppCompatActivity {
    ImageViewPro photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        photoView = findViewById(R.id.photoView);
        Glide.with(this).load(getIntent().getStringExtra("uri")).into(photoView);
        photoView.setOnClickListener(v -> this.finishAfterTransition());
    }
}