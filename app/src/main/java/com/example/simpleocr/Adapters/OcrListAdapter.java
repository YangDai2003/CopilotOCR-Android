package com.example.simpleocr.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.simpleocr.Model.ItemClick;
import com.example.simpleocr.Model.OcrItem;
import com.example.simpleocr.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

/**
 * @author 30415
 */
public class OcrListAdapter extends RecyclerView.Adapter<OcrListAdapter.ItemViewHolder> {
    private final List<OcrItem> list;
    final ItemClick itemClick;

    public OcrListAdapter(List<OcrItem> list, ItemClick itemClick) {
        this.list = list;
        this.itemClick = itemClick;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ItemViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        addAnimation(holder);
    }

    private void addAnimation(ItemViewHolder holder) {
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_anim));
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        OcrItem ocrItem = list.get(holder.getAdapterPosition());
        holder.textViewText.setText(ocrItem.getText());
        holder.textViewDate.setText(ocrItem.getDate());
        String image = ocrItem.getImage();
        if (!image.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(image).sizeMultiplier(0.8f).into(holder.imageView);
        }
        holder.itemView.setOnClickListener(v ->
                itemClick.onClick(ocrItem, holder.getAdapterPosition(), holder.imageView));
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewText;
        final TextView textViewDate;
        final ShapeableImageView imageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewText = itemView.findViewById(R.id.textview_text);
            textViewDate = itemView.findViewById(R.id.textview_date);
            imageView = itemView.findViewById(R.id.imageShow);
        }
    }
}
