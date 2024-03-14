package com.example.simpleocr.Adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.simpleocr.Model.OcrItem;

public class DiffCallback extends DiffUtil.ItemCallback<OcrItem> {
    @Override
    public boolean areItemsTheSame(@NonNull OcrItem oldItem, @NonNull OcrItem newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(@NonNull OcrItem oldItem, @NonNull OcrItem newItem) {
        return oldItem.getText().equals(newItem.getText());
    }
}
