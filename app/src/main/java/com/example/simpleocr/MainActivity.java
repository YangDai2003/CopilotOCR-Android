package com.example.simpleocr;

import static com.example.simpleocr.FileUtils.copyFile;
import static com.example.simpleocr.FileUtils.deleteLangFile;
import static com.example.simpleocr.FileUtils.deleteRecursive;
import static com.example.simpleocr.FileUtils.deleteSingleFile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.simpleocr.Adapters.OcrListAdapter;
import com.example.simpleocr.DataBase.Room;
import com.example.simpleocr.Model.ItemClick;
import com.example.simpleocr.Model.OcrItem;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ExperimentalGetImage
public class MainActivity extends AppCompatActivity {
    AppBarLayout appBarLayout;
    MaterialToolbar materialToolbar;
    RecyclerView recyclerView;
    FloatingActionButton button, openCamera, openAlbum, openScan;
    SwipeRefreshLayout refresh;
    Room room;
    OcrListAdapter ocrListAdapter;
    private List<OcrItem> itemList = new ArrayList<>();
    private int mPosition;
    ActivityResultLauncher<Intent> intentActivityResultLauncher1, intentActivityResultLauncher2;
    File parentFile;
    private static final String lang = "jpn+kor+equ";
    private int engineNum = 0;

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initUi() {
        appBarLayout = findViewById(R.id.appBar);
        appBarLayout.setStatusBarForeground(MaterialShapeDrawable.createWithElevationOverlay(this));
        materialToolbar = findViewById(R.id.materialToolbar);
        setSupportActionBar(materialToolbar);
        materialToolbar.setTitleCentered(true);
        materialToolbar.setNavigationIcon(R.drawable.baseline_menu_24);

        recyclerView = findViewById(R.id.recycler_view);
        button = findViewById(R.id.fab_add_btn);
        openCamera = findViewById(R.id.camera_btn);
        openAlbum = findViewById(R.id.album_btn);
        openScan = findViewById(R.id.scan_btn);
        refresh = findViewById(R.id.refresh);

        openCamera.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                Intent intent = new Intent(this, OcrActivity.class);
                intent.putExtra("launch", "camera");
                intent.putExtra("langs", lang);
                intent.putExtra("engine", engineNum);
                intentActivityResultLauncher1.launch(intent);
            }
        });
        openAlbum.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 不需要解释为何需要该权限，直接请求授权
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                Intent intent = new Intent(this, OcrActivity.class);
                intent.putExtra("launch", "album");
                intent.putExtra("langs", lang);
                intent.putExtra("engine", engineNum);
                intentActivityResultLauncher1.launch(intent);
            }
        });
        openScan.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                Intent intent = new Intent(this, OcrActivity.class);
                intent.putExtra("launch", "scancode");
                intentActivityResultLauncher1.launch(intent);
            }
        });
        button.setOnClickListener(view -> {
            ObjectAnimator objectAnimatorY1, objectAnimatorY2, objectAnimatorY3;
            if (openCamera.getVisibility() == View.GONE) {
                openCamera.setVisibility(View.VISIBLE);
                openAlbum.setVisibility(View.VISIBLE);
                openScan.setVisibility(View.VISIBLE);
                objectAnimatorY1 = ObjectAnimator.ofFloat(openCamera, "translationY", -520f);
                objectAnimatorY1.setDuration(200);
                objectAnimatorY1.start();
                objectAnimatorY2 = ObjectAnimator.ofFloat(openAlbum, "translationY", -260f);
                objectAnimatorY2.setDuration(200);
                objectAnimatorY2.start();
                objectAnimatorY3 = ObjectAnimator.ofFloat(openScan, "translationX", -260f);
                objectAnimatorY3.setDuration(200);
                objectAnimatorY3.start();
                button.setImageDrawable(getDrawable(R.drawable.baseline_clear_24));
            } else {
                objectAnimatorY1 = ObjectAnimator.ofFloat(openCamera, "translationY", 0f);
                objectAnimatorY1.setDuration(200);
                objectAnimatorY1.start();
                objectAnimatorY2 = ObjectAnimator.ofFloat(openAlbum, "translationY", 0f);
                objectAnimatorY2.setDuration(200);
                objectAnimatorY2.start();
                objectAnimatorY3 = ObjectAnimator.ofFloat(openScan, "translationX", 0f);
                objectAnimatorY3.setDuration(200);
                objectAnimatorY3.start();
                new Handler().postDelayed(() -> {
                    openCamera.setVisibility(View.GONE);
                    openAlbum.setVisibility(View.GONE);
                    openScan.setVisibility(View.GONE);
                }, 200);
                button.setImageDrawable(getDrawable(R.drawable.baseline_add_24));
            }
        });
        refresh.setOnRefreshListener(this::onRefresh);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ocrListAdapter.setScrollUp(dy > 20);
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 没有获得授权，申请授权
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, getString(R.string.ask), Toast.LENGTH_LONG).show();
                } else {
                    // 不需要解释为何需要该权限，直接请求授权
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
            }
        }

        initUi();

        itemTouchHelper.attachToRecyclerView(recyclerView);
        room = Room.getInstance(this);
        itemList = room.dao().getAll();
        updateRecycler(itemList);

        intentActivityResultLauncher1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                OcrItem new_item = null;
                if (res.getData() != null) {
                    new_item = (OcrItem) res.getData().getSerializableExtra("ocr_item");
                }
                long id = room.dao().insert(new_item);
                if (new_item != null) {
                    new_item.setID(id);
                }
                itemList.add(0, new_item);
                ocrListAdapter.notifyItemInserted(0);
            }
        });

        intentActivityResultLauncher2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
            if (res.getResultCode() == Activity.RESULT_OK) {
                OcrItem new_item = null;
                if (res.getData() != null) {
                    new_item = (OcrItem) res.getData().getSerializableExtra("ocr_item");
                }
                if (new_item != null) {
                    room.dao().update(new_item);
                }
                itemList.clear();
                itemList.addAll(room.dao().getAll());
                ocrListAdapter.notifyItemChanged(mPosition);
            }
        });

        getSupportFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, bundle) -> {
            if (requestKey.equals("requestKey") && bundle != null) {
                if (bundle.getBoolean("clear", false)) {
                    SpinKitView process = findViewById(R.id.spin_kit);
                    process.setVisibility(View.VISIBLE);
                    room.clearAllTables();
                    File dir = getFilesDir();
                    deleteRecursive(dir);
                    new Handler().postDelayed(() -> {
                        itemList = room.dao().getAll();
                        updateRecycler(itemList);
                        process.setVisibility(View.GONE);
                    }, 2000);
                }
            }
        });

        String mDataPath = getFilesDir().getAbsolutePath();

        parentFile = new File(mDataPath, "tessdata");
        if (!parentFile.exists()) { // 确保路径存在
            parentFile.mkdir();
        }
        copyFiles(); // 复制字库到手机

        String[] deleteFilePaths = new String[]{"chi_all.traineddata", "eng.traineddata", "deu.traineddata", "osd.traineddata"};
        try {
            for (String path : deleteFilePaths) {
                deleteLangFile(path, parentFile);
            }
        } catch (Exception ignored) {
        }
    }

    private void copyFiles() {
        AssetManager am = getAssets();
        String[] dataFilePaths = new String[]{"jpn.traineddata", "kor.traineddata", "equ.traineddata"}; // 拷字库过去
        for (String dataFilePath : dataFilePaths) {
            File engFile = new File(parentFile, dataFilePath);
            if (!engFile.exists()) {
                copyFile(am, dataFilePath, engFile);
            }
        }
    }

    public void onRefresh() {//刷新
        new Handler().postDelayed(() -> {
            itemList = room.dao().getAll();
            updateRecycler(itemList);
            refresh.setRefreshing(false);//刷新旋转动画停止
        }, 1000);
    }

    private void updateRecycler(List<OcrItem> ocrItemList) {
        recyclerView.setHasFixedSize(true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
        } else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
        }
        ocrListAdapter = new OcrListAdapter(ocrItemList, itemClick);
        recyclerView.setAdapter(ocrListAdapter);
    }

    private final ItemClick itemClick = new ItemClick() {
        @Override
        public void onClick(OcrItem ocrItem, int position, View imageview) {
            mPosition = position;
            Intent intent = new Intent(MainActivity.this, OcrActivity.class);
            intent.putExtra("old_ocr", ocrItem);
            intentActivityResultLauncher2.launch(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, imageview, "testImg"));
        }
    };

    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int swiped = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            //第一个参数拖动，第二个删除侧滑
            return makeMovementFlags(0, swiped);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle(getString(R.string.delete))//标题
                    .setMessage(getString(R.string.sure))//内容
                    .setIcon(R.mipmap.ic_launcher)//图标
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> ocrListAdapter.notifyItemChanged(position))
                    .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                        room.dao().delete(itemList.get(position));
                        String path = itemList.get(position).getImage();
                        deleteSingleFile(path);
                        itemList.remove(position);
                        ocrListAdapter.notifyItemRemoved(position);
                    }).show();
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 创建Material3 Bottom Sheet
            MyBottomSheetDialog bottomSheetDialog = new MyBottomSheetDialog();
            // 显示Bottom Sheet
            bottomSheetDialog.show(getSupportFragmentManager(), "bottom_sheet_tag");
            return true;
        } else if (item.getItemId() == R.id.choose) {
            final String[] items = {getString(R.string.engineOptions1), getString(R.string.engineOptions2)};
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(this);
            alertBuilder.setTitle(getString(R.string.engine));
            alertBuilder.setIcon(R.drawable.baseline_settings_24);
            alertBuilder.setCancelable(false);
            // 0tess 1google
            alertBuilder.setSingleChoiceItems(items, engineNum, (dialog1, which) -> {
                if (which == 0) {
                    engineNum = 0;
                } else {
                    engineNum = 1;
                }
            });
            alertBuilder.setPositiveButton(getString(R.string.confirm), (dialogInterface, i) -> {

            });
            alertBuilder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}