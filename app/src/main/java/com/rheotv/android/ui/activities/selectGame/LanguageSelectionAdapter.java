package com.rheotv.android.ui.activities.selectGame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.onboarding.LanguageObject;
import com.rheotv.android.databinding.ListItemLanguageSelectionBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageSelectionAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<LanguageObject> list;
    private Map<String, String> selectedLanguage;
    private LanguageInteractionListener mListener;
    private Map<String, Integer> localLanguage = new HashMap<>();

    public LanguageSelectionAdapter(List<LanguageObject> list) {
        this.list = list;
        this.selectedLanguage = new HashMap<>();
        localLanguage.put("hindi", R.drawable.avd_hindi);
        localLanguage.put("english", R.drawable.avd_english);
        localLanguage.put("tamil", R.drawable.avd_tamil);
        localLanguage.put("telugu", R.drawable.avd_telugu);
        localLanguage.put("marathi", R.drawable.avd_marathi);
        localLanguage.put("bengali", R.drawable.avd_bengali);
        localLanguage.put("malayalam", R.drawable.avd_malayali);
    }

    public void setListener(LanguageInteractionListener mListener) {
        this.mListener = mListener;
    }

    void submitList(List<LanguageObject> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemLanguageSelectionBinding binding = ListItemLanguageSelectionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LanguageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class LanguageViewHolder extends BaseViewHolder {
        ListItemLanguageSelectionBinding binding;

        LanguageViewHolder(ListItemLanguageSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            Context context = binding.getRoot().getContext();
            LanguageObject language = list.get(position);
            if (language.getName() != null && localLanguage.containsKey(language.getName().toLowerCase())) {
                binding.languageThumbnailImageView.setImageDrawable(
                        ContextCompat.getDrawable(context, localLanguage.get(language.getName().toLowerCase())));
            } else {
                BindingUtils.setImageUrlUsingCache(binding.languageThumbnailImageView, language.getThumbnail(), false);
            }

            binding.setLanguage(language);

            if (selectedLanguage.containsKey(language.getId())) {
                binding.getRoot().setSelected(true);
            } else {
                binding.getRoot().setSelected(false);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (selectedLanguage.containsKey(language.getId())) {
                    selectedLanguage.remove(language.getId());
                } else {
                    selectedLanguage.put(language.getId(), language.getName());
                }

                binding.getRoot().setSelected(!binding.getRoot().isSelected());
                if (mListener != null)
                    mListener.onLanguageItemClicked(selectedLanguage);
            });
        }
    }

    interface LanguageInteractionListener {
        void onLanguageItemClicked(Map<String, String> selectedLanguage);
    }
}
