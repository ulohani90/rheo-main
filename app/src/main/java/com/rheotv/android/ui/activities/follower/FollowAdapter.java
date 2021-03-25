package com.rheotv.android.ui.activities.follower;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.postlisting.responses.Author;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.databinding.ItemLeaderboardBinding;
import com.rheotv.android.ui.activities.leaderboard.FollowListenerCallback;
import com.rheotv.android.ui.activities.leaderboard.LeaderBoardItemVM;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;

import java.util.ArrayList;
import java.util.List;

public class FollowAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    FollowAdapterItemListener mListener;
    private ArrayList<Author> mList = new ArrayList<>();
    private boolean showLoadingView = false;
    private boolean shouldShowFollowerButton = false;

    public void setListener(FollowAdapterItemListener mListener) {
        this.mListener = mListener;
    }

    public void submitList(ArrayList<Author> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setShouldShowFollowerButton(boolean shouldShowFollowerButton) {
        this.shouldShowFollowerButton = shouldShowFollowerButton;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AppConstants.VIEW_TYPE_LOADING_FOOTER) {
            FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
        }

        ItemLeaderboardBinding itemLeaderboardBinding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FollowViewHolder(itemLeaderboardBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mList.size())
            return AppConstants.VIEW_TYPE_LOADING_FOOTER;
        return AppConstants.VIEW_TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : (showLoadingView ? mList.size() + 1 : mList.size());
    }

    public void addItems(List<Author> list) {
        int oldCount = this.mList.size();
        this.mList.addAll(list);
        notifyItemRangeInserted(oldCount, list.size());
    }

    public void setShowLoadingView(boolean showLoadingView) {
        this.showLoadingView = showLoadingView;
        if (showLoadingView) {
            notifyItemInserted(mList.size());
        } else {
            notifyItemRemoved(mList.size());
        }
    }

    class FollowViewHolder extends BaseViewHolder {
        private ItemLeaderboardBinding binding;

        FollowViewHolder(ItemLeaderboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            Author author = mList.get(position);
            LeaderBoardItemVM leaderBoardItemVM = new LeaderBoardItemVM();
            leaderBoardItemVM.setData(author, true);

            binding.setViewModel(leaderBoardItemVM);
            binding.setShouldShowFollowButton(shouldShowFollowerButton);
            BindingUtils.setProfileImageUrlFromCache(binding.authorID2, author.getUser().getProfilePic(), true);
            binding.getRoot().setOnClickListener(v -> mListener.onItemClick(author.getUser().getUsername()));
            Log.i(getClass().getSimpleName(), "FollowViewHolder: " + author.getUser().getUsername() + " and " + author.getUser().isFollowed());
            binding.followButton.setOnClickListener(v -> {
                boolean isFollowed = author.getUser().isFollowed();
                String id = author.getUser().getId() + "";
                leaderBoardItemVM.setCanFollow(false);
                mListener.onFollowClick(isFollowed, author.getUser().getUsername(), id, new FollowListenerCallback() {
                    @Override
                    public void onToggleFollow(boolean flag) {
                        leaderBoardItemVM.setIsFollowed(flag);
                        leaderBoardItemVM.setCanFollow(true);
                    }

                    @Override
                    public void onFail() {
                        leaderBoardItemVM.setCanFollow(true);
                    }
                });
            });
        }
    }

    public class FooterLoadingViewHolder extends BaseViewHolder {

        FooterLoadingViewHolder(FooterLoadingLayoutBinding binding) {
            super(binding.getRoot());
        }

        @Override
        public void onBind(int position) {
            Log.i(getClass().getName(), "loading footer");
        }
    }

    public interface FollowAdapterItemListener {
        void onItemClick(String id);

        void onFollowClick(boolean isFollow, String author, String profileId, FollowListenerCallback callback);
    }
}
