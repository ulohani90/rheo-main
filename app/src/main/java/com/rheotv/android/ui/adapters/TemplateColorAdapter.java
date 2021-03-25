package com.rheotv.android.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.databinding.ListItemTemplateColorBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TemplateColorAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private ArrayList<String> mList = new ArrayList<>();
    private Map<String, String> selectedColor = new HashMap<>();
    private OnColorInteractionListener listener;
    private int selectedPositoin = 0;

    public void addColors(ArrayList<String> list) {
        this.mList.addAll(list);
        notifyDataSetChanged();
    }

    public void addListener(OnColorInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemTemplateColorBinding binding = ListItemTemplateColorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ColorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ColorViewHolder extends BaseViewHolder {
        ListItemTemplateColorBinding binding;

        public ColorViewHolder(ListItemTemplateColorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            binding.colorHolder.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(mList.get(position))));
            if (selectedPositoin == position)
                binding.setSelected(true);
            else
                binding.setSelected(false);

            binding.getRoot().setOnClickListener(v -> {
                binding.setSelected(true);
                if (selectedPositoin != position) {
                    notifyItemChanged(selectedPositoin);
                    selectedPositoin = getAdapterPosition();
                }

                if (listener != null) {
                    listener.onColorSelected(mList.get(position));
                }
            });

            binding.executePendingBindings();
        }
    }

    public interface OnColorInteractionListener {
        void onColorSelected(String color);
    }

}