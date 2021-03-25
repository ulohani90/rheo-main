package com.rheotv.android.ui.customViews.bottomSheetMenu;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.databinding.BottomSheetDialogFragmentGridItemBinding;
import com.rheotv.android.databinding.BottomSheetDialogFragmentHeaderBinding;
import com.rheotv.android.databinding.BottomSheetDialogFragmentItemBinding;

import java.util.ArrayList;

public class BottomSheetMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_LINEAR_ITEM = 1;
    private final int VIEW_TYPE_GRID_ITEM = 2;

    private ArrayList<Option> options;
    private String header;
    private MenuItemInteractionListener listener;
    private boolean isCheckable = false;
    private int checkedId = -1;
    private boolean enableGrid = false;
    private boolean applyTint = false;
    private int spanner = -1;

    public BottomSheetMenuAdapter(ArrayList<Option> options, String header) {
        this.options = options;
        this.header = header;
    }

    public BottomSheetMenuAdapter(@Nullable String header, MenuItemInteractionListener listener) {
        this.options = new ArrayList<>();
        this.header = header;
        this.listener = listener;
    }

    public BottomSheetMenuAdapter(
            String header,
            MenuItemInteractionListener listener,
            boolean isCheckable,
            int checkId,
            boolean enableGrid,
            boolean applyTint,
            int spanner
    ) {
        this.options = new ArrayList<>();
        this.header = header;
        this.listener = listener;
        this.isCheckable = isCheckable;
        this.checkedId = checkId;
        this.enableGrid = enableGrid;
        this.applyTint = applyTint;
        this.spanner = spanner;
    }

    public void set(ArrayList<Option> options) {
        this.options.clear();
        this.options.addAll(options);
        notifyDataSetChanged();
    }

    public String getHeader() {
        return header;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                BottomSheetDialogFragmentHeaderBinding binding = BottomSheetDialogFragmentHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new HeaderViewHolder(binding);

            case VIEW_TYPE_LINEAR_ITEM:
                BottomSheetDialogFragmentItemBinding itemBinding = BottomSheetDialogFragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new ItemViewHolder(itemBinding);

            case VIEW_TYPE_GRID_ITEM:
                BottomSheetDialogFragmentGridItemBinding gridItemBinding = BottomSheetDialogFragmentGridItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new GridItemViewHolder(gridItemBinding);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int correctedPosition = header == null ? position : position - 1;
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(header);
        } else if (holder instanceof ItemViewHolder) {
            ((ItemViewHolder) holder).bind(options.get(correctedPosition));
        } else if (holder instanceof GridItemViewHolder) {
            ((GridItemViewHolder) holder).bind(options.get(correctedPosition));
        }
    }

    @Override
    public int getItemCount() {
        return header == null ? options.size() : options.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (header != null) {
            if (position == 0) {
                return VIEW_TYPE_HEADER;
            }
        }

        if (!enableGrid)
            return VIEW_TYPE_LINEAR_ITEM;
        else
            return VIEW_TYPE_GRID_ITEM;
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        BottomSheetDialogFragmentHeaderBinding binding;

        HeaderViewHolder(@NonNull BottomSheetDialogFragmentHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String header) {
            binding.setTitle(header);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        BottomSheetDialogFragmentItemBinding binding;

        ItemViewHolder(@NonNull BottomSheetDialogFragmentItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Option option) {
            binding.setOption(option);
            Log.i(getClass().getSimpleName(), "check_id: " + checkedId + " and " + option.getId());
            if (option.getId() == checkedId) {
                binding.getRoot().setSelected(true);
            } else {
                binding.getRoot().setSelected(false);
            }

            String title = option.getTitle().toString();
            SpannableString spannableString = new SpannableString(title);
            if (spanner == BottomSheetMenuDialog.Builder.SPANNER_BRACKET_ROUND) {
                try {
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#aeaeb2")), title.indexOf("("), title.indexOf(")") + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IndexOutOfBoundsException e) {

                }
            }

            binding.setTitleSpan(spannableString);

            if (option.showTint()) {
                binding.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(binding.getRoot().getContext(), R.color.navigation_item_tint)));
            } else {
                binding.icon.setImageTintList(null);
            }

            binding.getRoot().setOnClickListener(view -> {
                if (isCheckable && checkedId != option.getId()) {
                    checkedId = option.getId();
                    view.setSelected(!view.isSelected());
                    notifyDataSetChanged();
                }

                listener.onMenuClick(option);
            });
        }
    }

    class GridItemViewHolder extends RecyclerView.ViewHolder {
        BottomSheetDialogFragmentGridItemBinding binding;

        GridItemViewHolder(@NonNull BottomSheetDialogFragmentGridItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Option option) {
            binding.setOption(option);
            Log.i(getClass().getSimpleName(), "check_id: " + checkedId + " and " + option.getId());
            if (option.getId() == checkedId) {
                binding.getRoot().setSelected(true);
            } else {
                binding.getRoot().setSelected(false);
            }

            binding.getRoot().setOnClickListener(view -> {
                if (isCheckable && checkedId != option.getId()) {
                    checkedId = option.getId();
                    view.setSelected(!view.isSelected());
                    notifyDataSetChanged();
                }

                listener.onMenuClick(option);
            });
        }
    }

    interface MenuItemInteractionListener {

        void onMenuClick(Option option);

    }
}
