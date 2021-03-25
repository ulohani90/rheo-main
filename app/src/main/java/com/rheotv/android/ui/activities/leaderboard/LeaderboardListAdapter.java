package com.rheotv.android.ui.activities.leaderboard;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.Author;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.databinding.ItemLeaderboardBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;

import java.util.List;

public class LeaderboardListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<Author> leaderBoardList;

    private LeaderBoardItemClickListener mListener;

    boolean showLoading;

    private int VIEW_TYPE_ITEM = 1;
    private int VIEW_TYPE_LOADER = 2;

    public LeaderboardListAdapter(List<Author> mPostList) {
        this.leaderBoardList = mPostList;
    }

    public void setLeaderBoardListener(LeaderBoardItemClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getItemCount() {
        if (leaderBoardList != null && leaderBoardList.size() > 0) {
            Log.d("POSTLISTADAPTER", leaderBoardList.size() + " size ki list");

            return showLoading ? leaderBoardList.size() + 1 : leaderBoardList.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == leaderBoardList.size()) {
            return VIEW_TYPE_LOADER;
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOADER) {
            FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
        } else {
            ItemLeaderboardBinding itemLeaderboardBinding = ItemLeaderboardBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);
            return new LeaderBoardItemViewHolder(itemLeaderboardBinding, mListener);
        }
    }

    public void addItems(List<Author> leaderBoardItems) {
        int oldCount = this.leaderBoardList.size();
        this.leaderBoardList.addAll(leaderBoardItems);

        notifyItemRangeInserted(oldCount, leaderBoardList.size());
    }

    public void clearItems() {
        leaderBoardList.clear();
        notifyDataSetChanged();
    }

    public void setListener(LeaderBoardItemClickListener listener) {
        this.mListener = listener;
    }

    public void setShowLoadingView(boolean b) {
        showLoading = b;
        if (showLoading) {
            notifyItemInserted(leaderBoardList.size());
        } else {
            notifyItemRemoved(leaderBoardList.size());
        }
    }

    public interface LeaderBoardItemClickListener {
        void onItemClick(String id);

        void onFollowClick(boolean isFollow, String author, String profileId, FollowListenerCallback callback);
    }

    public class LeaderBoardItemViewHolder extends BaseViewHolder {
        private ItemLeaderboardBinding mBinding;
        private LeaderBoardItemClickListener leaderBoardItemClickListener;

        public LeaderBoardItemViewHolder(ItemLeaderboardBinding binding, LeaderBoardItemClickListener leaderBoardItemClickListener) {
            super(binding.getRoot());
            this.mBinding = binding;
            this.leaderBoardItemClickListener = leaderBoardItemClickListener;
        }

        @Override
        public void onBind(int position) {
            final Author author = leaderBoardList.get(position);
            LeaderBoardItemVM leaderBoardItemVM = new LeaderBoardItemVM();
            leaderBoardItemVM.setData(author, false);
            mBinding.setViewModel(leaderBoardItemVM);
            int color;
            float guidelinePercentage = 0f;
            if (position == 0 || position == 1 || position == 2) {
                mBinding.rankIndicatorView.setVisibility(View.VISIBLE);
                mBinding.guideline.setVisibility(View.VISIBLE);
                switch (position) {
                    case 0:
                        color = R.color.color_streamer_first_place;
                        guidelinePercentage = 0.84f;
                        break;
                    case 1:
                        color = R.color.color_streamer_second_place;
                        guidelinePercentage = 0.76f;
                        break;
                    case 2:
                        color = R.color.color_streamer_third_place;
                        guidelinePercentage = 0.68f;
                        break;

                    default:
                        color = R.color.app_background_color;
                        break;
                }
                mBinding.authorID2.setBorderColor(color);
                mBinding.rankIndicatorView.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                mBinding.rankIndicatorView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(mBinding.getRoot().getContext(), color)));
                mBinding.guideline.setGuidelinePercent(guidelinePercentage);
            } else {
                mBinding.authorID2.setBorderColor(R.color.app_background_color);
                mBinding.rankIndicatorView.setVisibility(View.GONE);
                mBinding.guideline.setVisibility(View.GONE);

            }
            BindingUtils.setProfileImageUrlFromCache(mBinding.authorID2, author.getUser().getProfilePic(), true);


            mBinding.getRoot().setOnClickListener(v -> leaderBoardItemClickListener.onItemClick(author.getUser().getUsername()));
            mBinding.followButton.setOnClickListener(v -> {
                boolean isFollowed = author.getUser().isFollowed();
                String id = author.getUser().getId() + "";
                leaderBoardItemVM.setCanFollow(false);

                leaderBoardItemClickListener.onFollowClick(isFollowed, author.getUser().getUsername(), id, new FollowListenerCallback() {
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

        public FooterLoadingViewHolder(FooterLoadingLayoutBinding binding) {
            super(binding.getRoot());
        }

        @Override
        public void onBind(int position) {

        }
    }
}