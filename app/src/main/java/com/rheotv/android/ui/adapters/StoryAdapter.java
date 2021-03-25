package com.rheotv.android.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.databinding.HorizontalLoadingLayoutBinding;
import com.rheotv.android.databinding.ListItemStoryBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private ArrayList<ProfileResult> mList = new ArrayList<>();
    private OnStoryInteractionListener mListener;
    private boolean isLoading = false;

    public StoryAdapter() {

    }

    public void submitItem(ProfileResult item) {
        mList.add(item);
        notifyDataSetChanged();
    }

    public void submitList(ArrayList<ProfileResult> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public void setListener(OnStoryInteractionListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AppConstants.VIEW_TYPE_LOADING_FOOTER) {
            HorizontalLoadingLayoutBinding footerLoadingLayoutBinding = HorizontalLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
        }

        ListItemStoryBinding binding = ListItemStoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mList.size())
            return AppConstants.VIEW_TYPE_LOADING_FOOTER;
        return AppConstants.VIEW_TYPE_STORY;
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return isLoading ? mList.size() + 1 : mList.size();
    }

    public void addItems(List<ProfileResult> rewards) {
        if (rewards == null || rewards.isEmpty()) return;

        int oldCount = mList.size();
        this.mList.addAll(rewards);
        notifyItemRangeInserted(oldCount, mList.size());
    }

    public void showLoading(boolean flag) {
        this.isLoading = flag;
//        if (flag) {
//            notifyItemInserted(mList.size());
//        } else {
//            notifyItemRemoved(mList.size());
//        }
        notifyDataSetChanged();
    }

    public boolean isLoading() {
        return isLoading;
    }

    class StoryViewHolder extends BaseViewHolder {
        ListItemStoryBinding binding;

        StoryViewHolder(ListItemStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            Context context = binding.getRoot().getContext();
            ProfileResult author = mList.get(position);

            binding.setAuthor(author);
            if (CommonUtils.isUserLoggedin()) {
                if (author.getId().equalsIgnoreCase(CommonUtils.getAuthorId())) {
                    binding.indicatorImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_add_background));
                    binding.indicatorImageView.setVisibility(View.VISIBLE);
                    binding.receiptImageView.setVisibility(View.INVISIBLE);
                    int dimension = convertDpToPx(context, 58);
                    binding.userAvatar.getLayoutParams().width = dimension;
                    binding.userAvatar.getLayoutParams().height = dimension;
                } else {
                    binding.indicatorImageView.setVisibility(View.INVISIBLE);
                    binding.receiptImageView.setVisibility(View.VISIBLE);
                    if (author.getStoryViewed())
                        binding.receiptImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.grey_circle_border_bg));
                    else
                        binding.receiptImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.accent_circle_border_bg));
                }
            } else {
                if ("me".equalsIgnoreCase(author.getId())) {
                    binding.indicatorImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avd_add_background));
                    binding.indicatorImageView.setVisibility(View.VISIBLE);
                    binding.receiptImageView.setVisibility(View.INVISIBLE);
                    int dimension = convertDpToPx(context, 54);
                    binding.userAvatar.getLayoutParams().width = dimension;
                    binding.userAvatar.getLayoutParams().height = dimension;
                } else {
                    binding.indicatorImageView.setVisibility(View.INVISIBLE);
                    binding.receiptImageView.setVisibility(View.VISIBLE);
                    if (author.getStoryViewed())
                        binding.receiptImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.grey_circle_border_bg));
                    else
                        binding.receiptImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.accent_circle_border_bg));
                }
            }

            binding.getRoot().setOnClickListener(v -> {
                if (CommonUtils.isUserLoggedin()) {
                    if (author.getId().equalsIgnoreCase(CommonUtils.getAuthorId()))
                        mListener.onAddNewStoryClicked();
                    else
                        mListener.onStoryClicked(author, position);
                } else {
                    if ("me".equalsIgnoreCase(author.getId()))
                        mListener.onAddNewStoryClicked();
                    else
                        mListener.onStoryClicked(author, position);
                }
            });

            binding.executePendingBindings();
        }
    }

    public class FooterLoadingViewHolder extends BaseViewHolder {

        FooterLoadingViewHolder(HorizontalLoadingLayoutBinding binding) {
            super(binding.getRoot());
        }

        @Override
        public void onBind(int position) {
            Log.i(getClass().getName(), "loading rewards");
        }
    }

    public int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public interface OnStoryInteractionListener {
        void onStoryClicked(ProfileResult author, int position);

        void onAddNewStoryClicked();

        void loadNextStory();
    }
}
