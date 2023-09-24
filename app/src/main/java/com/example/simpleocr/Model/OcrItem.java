package com.example.simpleocr.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * @author 30415
 */
@Entity(tableName = "items")
public class OcrItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    long id = 0;

    @ColumnInfo(name = "text")
    String text = "";

    @ColumnInfo(name = "date")
    String date = "";

    @ColumnInfo(name = "image")
    String image = "";

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
