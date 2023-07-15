package com.example.simpleocr.DataBase;


import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.simpleocr.Model.OcrItem;

import java.util.List;

/**
 * @author 30415
 */
@Dao
public interface DAO {

    @Insert(onConflict = REPLACE)
    long insert(OcrItem ocrItem);

    @Query("SELECT * FROM items ORDER BY ID DESC")
    List<OcrItem> getAll();

    @Update
    void update(OcrItem ocrItem);

    @Delete
    void delete(OcrItem ocrItem);
}