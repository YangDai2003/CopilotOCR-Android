package com.example.simpleocr;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class EditActivity extends AppCompatActivity {

    TextInputEditText textInputEditText;
    TextInputLayout textInputLayout;

    @Override
    public void finish() {
        String text = Objects.requireNonNull(textInputEditText.getText()).toString();
        Intent intent = new Intent();
        intent.putExtra("text", text);
        setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_edit);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                return;
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        textInputLayout = findViewById(R.id.textInputLayout);
        textInputEditText = findViewById(R.id.editText);

        if (null != getIntent().getStringExtra("text")) {
            textInputEditText.setText(getIntent().getStringExtra("text"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}