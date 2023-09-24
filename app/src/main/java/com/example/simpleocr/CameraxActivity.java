package com.example.simpleocr;

import static com.example.simpleocr.FileUtils.fileSaveToInside;
import static com.example.simpleocr.FileUtils.toTurn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.TorchState;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;


import com.example.simpleocr.Views.FocusView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author 30415
 */
@ExperimentalGetImage
public class CameraxActivity extends AppCompatActivity {
    private ImageView success;
    private ImageView imageView;
    private ImageAnalysis imageAnalysis;
    private BarcodeScanner scanner;
    private PreviewView viewFinder;
    private FocusView focusView;
    private Camera camera;
    private String savedUri;
    private float fingerSpacing = 0;

    private void setZoomRatio(float zoomRatio) {
        CameraControl cameraControl = camera.getCameraControl();
        cameraControl.setZoomRatio(zoomRatio);
    }

    private ZoomState getZoomState() {
        return camera.getCameraInfo().getZoomState().getValue();
    }

    private float getZoomRatio(float delta) {
        float zoomLevel = 1f;
        float newZoomLevel = zoomLevel + delta;
        if (newZoomLevel < 1f) {
            newZoomLevel = 1f;
        } else if (newZoomLevel > Objects.requireNonNull(getZoomState()).getMaxZoomRatio()) {
            newZoomLevel = getZoomState().getMaxZoomRatio();
        }
        return newZoomLevel;
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camerax);
        focusView = new FocusView(this);
        addContentView(focusView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        focusView.setVisibility(View.GONE);
        success = findViewById(R.id.success);
        imageView = findViewById(R.id.imageView);
        ImageView back = findViewById(R.id.imageView1);
        back.getBackground().setAlpha(50);
        imageView.setClickable(true);
        imageView.bringToFront();

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder().build();
        scanner = BarcodeScanning.getClient(options);

        startCamera();
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "ClickableViewAccessibility"})
    private void startCamera() {
        // 将Camera的生命周期和Activity绑定在一起（设定生命周期所有者），这样就不用手动控制相机的启动和关闭。
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // 将你的相机和当前生命周期的所有者绑定所需的对象
                ProcessCameraProvider processCameraProvider = cameraProviderFuture.get();

                // 创建一个Preview 实例，并设置该实例的 surface 提供者（provider）。
                viewFinder = findViewById(R.id.preview);
                viewFinder.setScaleType(PreviewView.ScaleType.FILL_CENTER);
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());


                // 选择后置摄像头作为默认摄像头
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 设置预览帧分析
                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new MyAnalyzer());

                // 重新绑定用例前先解绑
                processCameraProvider.unbindAll();

                // 绑定用例至相机
                camera = processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                imageView.setOnClickListener(v -> {
                    if (camera.getCameraInfo().hasFlashUnit()) {
                        // Toggle the torch state
                        if (null != camera.getCameraInfo().getTorchState().getValue()) {
                            if (camera.getCameraInfo().getTorchState().getValue() == TorchState.OFF) {
                                camera.getCameraControl().enableTorch(true);
                                imageView.setImageDrawable(getDrawable(R.drawable.baseline_flashlight_on_24));
                            } else {
                                camera.getCameraControl().enableTorch(false);
                                imageView.setImageDrawable(getDrawable(R.drawable.baseline_flashlight_off_24));
                            }
                        }
                    }
                });

                viewFinder.setOnTouchListener((view, event) -> {
                    try {
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN -> {
                                if (event.getPointerCount() == 1) {
                                    focusView.setCenter((int) event.getX(), (int) event.getY());
                                    focusView.setVisibility(View.VISIBLE);
                                    FocusMeteringAction action = new FocusMeteringAction.Builder(
                                            viewFinder.getMeteringPointFactory()
                                                    .createPoint(event.getX(), event.getY())).build();
                                    camera.getCameraControl().startFocusAndMetering(action);
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> focusView.setVisibility(View.GONE), 1000);
                                }
                            }
                            // 单指点击对焦
                            case MotionEvent.ACTION_POINTER_DOWN ->
                                // 双指缩放
                                    fingerSpacing = getFingerSpacing(event);
                            case MotionEvent.ACTION_MOVE -> {
                                if (event.getPointerCount() == 2) {
                                    float newFingerSpacing = getFingerSpacing(event);
                                    if (newFingerSpacing > fingerSpacing) {
                                        setZoomRatio(getZoomRatio(2f));
                                    } else if (newFingerSpacing < fingerSpacing) {
                                        setZoomRatio(getZoomRatio(-2f));
                                    }
                                    fingerSpacing = newFingerSpacing;
                                }
                            }
                            case MotionEvent.ACTION_POINTER_UP -> fingerSpacing = 0;
                            default -> {
                            }
                        }
                    } catch (Exception e) {
                        Log.e("Error setting focus and exposure", "");
                    }
                    return true;
                });

            } catch (Exception ignored) {
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private class MyAnalyzer implements ImageAnalysis.Analyzer {
        @SuppressLint("RestrictedApi")
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            final Bitmap bitmap = imageProxy.toBitmap();
            InputImage image =
                    InputImage.fromBitmap(bitmap, imageProxy.getImageInfo().getRotationDegrees());
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        StringBuilder codeInfo = new StringBuilder();
                        for (Barcode barcode : barcodes) {
                            int type = barcode.getValueType();
                            switch (type) {
                                case Barcode.TYPE_WIFI -> {
                                    String ssid = Objects.requireNonNull(barcode.getWifi()).getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    if (ssid != null && !ssid.isEmpty()) {
                                        codeInfo.append(getString(R.string.ssid)).append(" ").append(ssid).append("\n");
                                    }
                                    if (password != null && !password.isEmpty()) {
                                        codeInfo.append(getString(R.string.password)).append(" ").append(password).append("\n");
                                    }
                                }
                                case Barcode.TYPE_URL -> {
                                    String title = Objects.requireNonNull(barcode.getUrl()).getTitle();
                                    String uri = barcode.getUrl().getUrl();
                                    if (title != null && !title.isEmpty()) {
                                        codeInfo.append(getString(R.string.title)).append(" ").append(title).append("\n");
                                    }
                                    if (uri != null && !uri.isEmpty()) {
                                        codeInfo.append(getString(R.string.uri)).append(" ").append(uri).append("\n");
                                    }
                                }
                                case Barcode.TYPE_EMAIL -> {
                                    String address = Objects.requireNonNull(barcode.getEmail()).getAddress();
                                    String body = barcode.getEmail().getBody();
                                    if (address != null && !address.isEmpty()) {
                                        codeInfo.append(getString(R.string.address)).append(" ").append(address).append("\n");
                                    }
                                    if (body != null && !body.isEmpty()) {
                                        codeInfo.append(getString(R.string.body)).append(" ").append(body).append("\n");
                                    }
                                }
                                case Barcode.TYPE_PHONE -> {
                                    String number = Objects.requireNonNull(barcode.getPhone()).getNumber();
                                    if (number != null && !number.isEmpty()) {
                                        codeInfo.append(getString(R.string.phone)).append(" ").append(number).append("\n");
                                    }
                                }
                                default -> {
                                    String raw = barcode.getRawValue();
                                    codeInfo.append(raw).append("\n");
                                }
                            }

                        }
                        if (!codeInfo.toString().isEmpty()) {
                            success.bringToFront();
                            Bitmap res = toTurn(bitmap, imageProxy.getImageInfo().getRotationDegrees());
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
                            savedUri = fileSaveToInside(CameraxActivity.this, formatter.format(LocalDateTime.now()), res);
                            Intent intent = new Intent();
                            intent.putExtra("code", codeInfo.toString().trim());
                            intent.putExtra("uri", savedUri);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }
                    }).addOnCompleteListener(c -> imageProxy.close());
        }
    }
}
