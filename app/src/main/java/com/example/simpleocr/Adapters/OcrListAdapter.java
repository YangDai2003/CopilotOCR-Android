package com.example.simpleocr.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.simpleocr.ItemClick;
import com.example.simpleocr.OcrItem;
import com.example.simpleocr.R;

import java.util.List;

public class OcrListAdapter extends RecyclerView.Adapter<OcrListAdapter.ItemViewHolder> {
    List<OcrItem> list;
    ItemClick itemClick;
    boolean scrollUp = false;

    public OcrListAdapter(List<OcrItem> list, ItemClick itemClick) {
        this.list = list;
        this.itemClick = itemClick;
    }

    public void setScrollUp(boolean scroll) {
        scrollUp = scroll;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ItemViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (scrollUp) {
            addAnimation(holder);
        }
        scrollUp = false;
    }

    private void addAnimation(ItemViewHolder holder) {
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_anim));
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        view.setOnClickListener(v -> {
            int position1 = itemViewHolder.getAdapterPosition();
            ImageView imageView = itemViewHolder.imageView;
            imageView.setTransitionName("testImg");
            itemClick.onClick(list.get(position1), position1, imageView);
        });

        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        holder.textView_text.setText(list.get(position).getText());
        holder.textView_date.setText(list.get(position).getDate());
        String images = list.get(holder.getAdapterPosition()).getImage();
        if (!images.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(images).sizeMultiplier(0.8f).diskCacheStrategy(DiskCacheStrategy.NONE).into(holder.imageView);
        } else {
            Glide.with(holder.itemView.getContext()).clear(holder.imageView);
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textView_text, textView_date;
        ImageView imageView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_container);
            textView_text = itemView.findViewById(R.id.textview_text);
            textView_date = itemView.findViewById(R.id.textview_date);
            imageView = itemView.findViewById(R.id.imageShow);
        }
    }
}