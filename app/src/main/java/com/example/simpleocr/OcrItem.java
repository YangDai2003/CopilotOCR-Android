package com.example.simpleocr;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "items")
public class OcrItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    long ID = 0;

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

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
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
