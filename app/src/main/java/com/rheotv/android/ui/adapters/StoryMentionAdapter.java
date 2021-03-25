package com.rheotv.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.objects.SearchSuggestionObject;
import com.rheotv.android.databinding.ListItemMentionUserBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;

import java.util.ArrayList;
import java.util.List;

public class StoryMentionAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<SearchSuggestionObject> list = new ArrayList<>();
    private OnMentionInteractionListener listener;

    public StoryMentionAdapter() {
    }

    public void setListener(OnMentionInteractionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SearchSuggestionObject> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemMentionUserBinding binding = ListItemMentionUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MentionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MentionViewHolder extends BaseViewHolder {
        ListItemMentionUserBinding binding;

        public MentionViewHolder(ListItemMentionUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            SearchSuggestionObject suggestionObject = list.get(position);
            binding.setUsername(suggestionObject.getTitle());
            BindingUtils.setRoundImageUri(binding.userAvatar, suggestionObject.getImageUrl(), suggestionObject.getTitle());
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMentionClicked(suggestionObject);
                }
            });
        }
    }

    public interface OnMentionInteractionListener {
        void onMentionClicked(SearchSuggestionObject result);
    }
}
