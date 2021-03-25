package com.rheotv.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.ui.fragments.CommonBottomSheetDialog;

import java.util.List;

public class BottomSheetOptionsAdapter extends RecyclerView.Adapter<BottomSheetOptionsAdapter.BottomSheetItemViewHolder> {

    List<String> mOptions;

    ItemClickListener mListener;

    public BottomSheetOptionsAdapter(List<String> options, ItemClickListener listener) {
        this.mOptions = options;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public BottomSheetItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_item_layout, parent, false);
        return new BottomSheetItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BottomSheetItemViewHolder holder, int position) {
        holder.itemText.setText(mOptions.get(position));
        holder.itemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mOptions.size();
    }

    public class BottomSheetItemViewHolder extends RecyclerView.ViewHolder {

        TextView itemText;

        public BottomSheetItemViewHolder(View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.item_text);
        }

    }

    public interface ItemClickListener {
        void onItemClicked(int position);
    }
}
