package com.example.simpleocr.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.simpleocr.Model.OcrItem;

/**
 * @author 30415
 */
@Database(entities = OcrItem.class, version = 1, exportSchema = false)
public abstract class Room extends RoomDatabase {
    private static Room database;
    private static final String DATABASE_NAME = "OcrApp";

    public synchronized static Room getInstance(Context context) {
        if (database == null) {
            database = androidx.room.Room.databaseBuilder(context.getApplicationContext(),
                            Room.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }

    public abstract DAO dao();
}
