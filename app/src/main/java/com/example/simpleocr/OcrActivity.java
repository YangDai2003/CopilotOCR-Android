package com.example.simpleocr;

import static com.example.simpleocr.FileUtils.FileSaveToInside;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class OcrActivity extends AppCompatActivity {
    ImageButton copy, edit, share;
    ShapeableImageView imageButton;
    MaterialTextView textView;
    Bitmap bitmap;
    ActivityResultLauncher<Intent> galleryActivityResultLauncher, cameraActivityResultLauncher, editActivityResultLauncher;
    Uri sourceUri, destinationUri, resultUri;
    TessBaseAPI mTess;
    OcrItem ocrItem;
    boolean oldItem;
    String imageUri, dateStr;
    boolean changed = false;

    @Override
    public void finish() {
        String text = Objects.requireNonNull(textView.getText()).toString();
        if (!text.isEmpty()) {
            ocrItem.setText(text);
            ocrItem.setImage(imageUri);
            Intent intent = new Intent();
            intent.putExtra("ocr_item", ocrItem);
            setResult(Activity.RESULT_OK, intent);
        }
        if (!oldItem) {
            mTess.recycle();
        }
        super.finish();
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void initUI() {
        share = findViewById(R.id.buttonShare);
        edit = findViewById(R.id.buttonEdit);
        imageButton = findViewById(R.id.imageButton);
        copy = findViewById(R.id.buttonCopy);
        textView = findViewById(R.id.textView);

        copy.setOnClickListener(view -> {
            if (!Objects.requireNonNull(textView.getText()).toString().isEmpty()) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("", textView.getText());
                cm.setPrimaryClip(clipData);
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
            }
        });
        edit.setOnClickListener(v -> {
            if (!textView.getText().toString().isEmpty()) {
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("text", textView.getText().toString());
                editActivityResultLauncher.launch(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, textView, "text"));
            }
        });
        share.setOnClickListener(v -> {
            if (!textView.getText().toString().isEmpty()) {
                try {
                    CharSequence res = textView.getText();
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, res.toString());
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share)));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "???", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (changed) finish();
            else finishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("QueryPermissionsNeeded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        setContentView(R.layout.activity_ocr);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        initUI();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (changed) finish();
                else finishAfterTransition();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        initGalleryActivityResultLauncher();
        initCameraActivityResultLauncher();
        initEditActivityResultLauncher();

        ocrItem = new OcrItem();
        oldItem = true;
        if (null != getIntent().getSerializableExtra("old_ocr")) {
            ocrItem = (OcrItem) getIntent().getSerializableExtra("old_ocr");
            textView.setText(ocrItem.getText());
            imageUri = ocrItem.getImage();
            dateStr = ocrItem.getDate();
            bitmap = BitmapFactory.decodeFile(imageUri);
            imageButton.setVisibility(View.VISIBLE);
            imageButton.setImageBitmap(bitmap);
            imageButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, PhotoActivity.class);
                intent.putExtra("uri", imageUri);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this, imageButton, "testImg").toBundle());
            });
        } else {
            String lang = getIntent().getStringExtra("langs");
            mTess = new TessBaseAPI();
            try {
                mTess.init(getFilesDir().getAbsolutePath(), lang, TessBaseAPI.OEM_LSTM_ONLY);
            } catch (IllegalArgumentException ignored) {
            }
            oldItem = false;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = new Date();
            dateStr = format.format(date);
            ocrItem.setDate(dateStr);
        }
        Objects.requireNonNull(getSupportActionBar()).setTitle(dateStr);
        if (getIntent().getStringExtra("launch") != null && getIntent().getStringExtra("launch").equals("camera")) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, getString(R.string.photo));
                values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.camera));
                sourceUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, sourceUri);
                cameraActivityResultLauncher.launch(intent);
            }
        } else if (getIntent().getStringExtra("launch") != null && getIntent().getStringExtra("launch").equals("album")) {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                intent.setType("image/*");
                galleryActivityResultLauncher.launch(intent);
            } else {
                intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                galleryActivityResultLauncher.launch(intent);
            }
        }
    }

    private void initGalleryActivityResultLauncher() {
        galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                sourceUri = result.getData().getData();
                destinationUri = Uri.fromFile(new File(getCacheDir(), "CropImage.jpeg"));
                startUcrop(OcrActivity.this, sourceUri, destinationUri);
            }
        });
    }

    private void initCameraActivityResultLauncher() {
        cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "CropImage.jpeg"));
                startUcrop(OcrActivity.this, sourceUri, destinationUri);
            }
        });
    }

    private void initEditActivityResultLauncher() {
        editActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
                textView.setText(result.getData().getStringExtra("text"));
                changed = true;
            }
        });
    }

    public void startUcrop(Activity activity, Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();

        options.setToolbarWidgetColor(getColor(android.R.color.tab_indicator_text));
        options.setStatusBarColor(SurfaceColors.SURFACE_2.getColor(this));
        options.setToolbarColor(SurfaceColors.SURFACE_2.getColor(this));
        UCrop uCrop = UCrop.of(sourceUri, destinationUri);
        uCrop.withOptions(options);
        uCrop.start(activity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    imageButton.setVisibility(View.VISIBLE);
                    bitmap = BitmapFactory.decodeFile(resultUri.getPath());
                    imageButton.setImageBitmap(bitmap);
                    imageButton.setOnClickListener(v -> {
                        Intent intent = new Intent(this, PhotoActivity.class);
                        intent.putExtra("uri", resultUri.toString());
                        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageButton, "testImg").toBundle());
                    });
                }
                getText(bitmap);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Date date = new Date();
                imageUri = FileSaveToInside(this, format.format(date), bitmap);
            }
        }
    }

    private void getText(Bitmap bitmap) {
        textView.setText("");
        textView.setHint(R.string.processing___);

        // Start process in another thread
        new Thread(() -> {
            mTess.setImage(bitmap);
            String text = mTess.getUTF8Text();
            runOnUiThread(() -> textView.setText(text));
        }).start();
    }
}