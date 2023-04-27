package com.example.simpleocr;

import static com.example.simpleocr.FileUtils.deleteSingleFile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.app.Dialog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.example.simpleocr.Adapters.OcrListAdapter;
import com.example.simpleocr.DataBase.Room;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    AppBarLayout appBarLayout;
    MaterialToolbar materialToolbar;
    RecyclerView recyclerView;
    FloatingActionButton button, openCamera, openAlbum;
    SwipeRefreshLayout refresh;
    Room room;
    OcrListAdapter ocrListAdapter;
    List<OcrItem> itemList = new ArrayList<>();
    int mPosition;
    ActivityResultLauncher<Intent> intentActivityResultLauncher1, intentActivityResultLauncher2;
    File parentFile;
    private final boolean[] options = new boolean[]{true, true, true};
    private final String[] languages = new String[]{"eng", "chi_all", "deu", "equ", "osd"};
    private final List<String> list = new ArrayList<>(Arrays.asList(languages));
    String lang = "chi_all+eng+deu+equ+osd";

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initUi(){
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
        refresh = findViewById(R.id.refresh);
        openCamera.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                Intent intent = new Intent(this, OcrActivity.class);
                intent.putExtra("launch", "camera");
                intent.putExtra("langs", lang);
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
                intentActivityResultLauncher1.launch(intent);
            }
        });
        button.setOnClickListener(view -> {
            ObjectAnimator objectAnimatorY1, objectAnimatorY2;
            if (openCamera.getVisibility() == View.GONE) {
                openCamera.setVisibility(View.VISIBLE);
                openAlbum.setVisibility(View.VISIBLE);
                objectAnimatorY1 = ObjectAnimator.ofFloat(openCamera, "translationY", -520f);
                objectAnimatorY1.setDuration(200);
                objectAnimatorY1.start();
                objectAnimatorY2 = ObjectAnimator.ofFloat(openAlbum, "translationY", -260f);
                objectAnimatorY2.setDuration(200);
                objectAnimatorY2.start();
                button.setImageDrawable(getDrawable(R.drawable.baseline_clear_24));
            } else {
                objectAnimatorY1 = ObjectAnimator.ofFloat(openCamera, "translationY", 0f);
                objectAnimatorY1.setDuration(200);
                objectAnimatorY1.start();
                objectAnimatorY2 = ObjectAnimator.ofFloat(openAlbum, "translationY", 0f);
                objectAnimatorY2.setDuration(200);
                objectAnimatorY2.start();
                new Handler().postDelayed(() -> {
                    openCamera.setVisibility(View.GONE);
                    openAlbum.setVisibility(View.GONE);
                }, 200); // 延时1秒
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

        String mDataPath = getFilesDir().getAbsolutePath();

        parentFile = new File(mDataPath, "tessdata");
        if (!parentFile.exists()) { // 确保路径存在
            parentFile.mkdir();
        }
        copyFiles(); // 复制字库到手机
    }

    private void copyFiles() {
        AssetManager am = getAssets();
        String[] dataFilePaths = new String[]{"chi_all.traineddata",
                "eng.traineddata", "deu.traineddata", "equ.traineddata", "osd.traineddata"}; // 拷字库过去
        for (String dataFilePath : dataFilePaths) {
            File engFile = new File(parentFile, dataFilePath);
            if (!engFile.exists()) {
                copyFile(am, dataFilePath, engFile);
            }
        }
    }

    private static void copyFile(@NonNull AssetManager am, @NonNull String assetName, @NonNull File outFile) {
        try (
                InputStream in = am.open(assetName);
                OutputStream out = new FileOutputStream(outFile)
        ) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onRefresh() {//刷新
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
            Dialog dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
            //填充对话框的布局
            View inflate = View.inflate(this, R.layout.about, null);
            inflate.setBackground(new MaterialAlertDialogBuilder(this).getBackground());
            //初始化控件
            MaterialButton source = inflate.findViewById(R.id.source);
            MaterialButton close = inflate.findViewById(R.id.close);
            MaterialButton share = inflate.findViewById(R.id.share);
            MaterialButton rate = inflate.findViewById(R.id.rate);
            rate.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.yangdai.simpleocr"));
                startActivity(intent);
            });
            share.setOnClickListener(v -> {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareContent));
                startActivity(Intent.createChooser(sendIntent, ""));
            });
            source.setOnClickListener(v -> {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.source));
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
            });
            close.setOnClickListener(v -> dialog.dismiss());
            //将布局设置给Dialog
            dialog.setContentView(inflate);
            dialog.setCancelable(false);
            //获取当前Activity所在的窗体
            Window dialogWindow = dialog.getWindow();
            //设置Dialog从窗体底部弹出
            dialogWindow.setGravity(Gravity.CENTER);
            dialogWindow.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();//显示对话框
            return true;
        } else if (item.getItemId() == R.id.settings) {
            final String[] items = {getString(R.string.en), getString(R.string.zh), getString(R.string.de)};
            MaterialAlertDialogBuilder alertBuilder = new MaterialAlertDialogBuilder(this);
            alertBuilder.setTitle(getString(R.string.settings));
            alertBuilder.setIcon(R.drawable.baseline_settings_24);
            alertBuilder.setCancelable(false);
            // 0英语 1中文 2德语
            alertBuilder.setMultiChoiceItems(items, options, (dialogInterface, i, isChecked) -> {
                if (isChecked) {
                    if (!list.contains(languages[i])) list.add(languages[i]);
                } else {
                    list.remove(languages[i]);
                }
                options[i] = isChecked;
            });
            alertBuilder.setPositiveButton(getString(R.string.confirm), (dialogInterface, i) -> {
                StringBuilder lang = new StringBuilder();
                for (int j = 0; j < list.size(); ++j) {
                    lang.append(list.get(j));
                    if (j != list.size() - 1) lang.append("+");
                }
                this.lang = lang.toString();
            });
            alertBuilder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}