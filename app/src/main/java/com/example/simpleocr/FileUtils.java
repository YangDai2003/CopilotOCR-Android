package com.example.simpleocr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class FileUtils {
    public static void deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    public static String FileSaveToInside(Context context, String fileName, Bitmap bitmap) {
        FileOutputStream fos = null;
        String path = null;
        try {
            File folder = context.getFilesDir();
            //判断目录是否存在
            //目录不存在时自动创建
            if (folder.exists() || folder.mkdir()) {
                File file = new File(folder, fileName);
                fos = new FileOutputStream(file);
                //写入文件
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                path = file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    //关闭流
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //返回路径
        return path;
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap toTurn(Bitmap img, int deg) {
        Matrix matrix = new Matrix();
        matrix.postRotate(deg);
        int width = img.getWidth();
        int height = img.getHeight();
        return Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.exists() && fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    public static void deleteLangFile(String filePath$Name, File parentFile) {
        File file = new File(parentFile, filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    public static void copyFile(@NonNull AssetManager am, @NonNull String assetName, @NonNull File outFile) {
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
}
