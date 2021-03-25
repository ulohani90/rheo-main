package com.rheotv.android.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.databinding.ClipItemLayoutBinding;
import com.rheotv.android.databinding.ClipsRvFooterLayoutBinding;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.db.ClipItem;
import com.rheotv.android.ui.activities.player.activity.FollowStatusCompleteListener;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ClipsListAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    List<ClipItem> clips;

    public final int FOOTER_LOADER = 1;
    boolean checkFirstClip = true;
    public static final int CLIP_VIEW = 2;

    public final int FOOTER_END_OF_CLIPS = 3;

    boolean shouldShowLoading;

    boolean showEndOfListFooter;

    OnClipCardItemsClick mListener;

    public ClipsListAdapter() {
        this.clips = new ArrayList<>();
    }


    public void setListener(OnClipCardItemsClick mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CLIP_VIEW) {
            ClipItemLayoutBinding binding = ClipItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);
            return new ClipViewHolder(binding);
        } else if (viewType == FOOTER_LOADER) {
            FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
        } else {
            ClipsRvFooterLayoutBinding clipsRvFooterLayoutBinding = ClipsRvFooterLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FooterEndOfDateViewHolder(clipsRvFooterLayoutBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {
        baseViewHolder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return shouldShowLoading || showEndOfListFooter ? clips.size() + 1 : clips.size();
    }


    public void setShowEndOfListFooter(boolean showEndOfListFooter) {
        this.showEndOfListFooter = showEndOfListFooter;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == clips.size()) {
            if (shouldShowLoading)
                return FOOTER_LOADER;
            else
                return FOOTER_END_OF_CLIPS;
        }
        return CLIP_VIEW;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        resetFollowCard(holder);
    }

    private void resetFollowCard(BaseViewHolder holder) {
        if (holder instanceof ClipViewHolder) {
            ClipViewHolder clipViewHolder = (ClipViewHolder) holder;
            if (clipViewHolder.getmBinding() == null || clipViewHolder.getmBinding().clipFollowCard == null)
                return;
            clipViewHolder.getmBinding().clipFollowCard.getRoot().setVisibility(View.GONE);

        }
    }

    public void setClips(List<ClipItem> clips) {
        int positionStart = this.clips.size();
        this.clips.addAll(clips);
        notifyItemRangeInserted(positionStart, clips.size());
    }

    public void setShowLoadingView(boolean shouldShowLoading) {
        this.shouldShowLoading = shouldShowLoading;
        if (shouldShowLoading) {
            notifyItemInserted(clips.size());
        } else {
            notifyItemRemoved(clips.size());
        }
    }

    public void changeClapState(int position, boolean isClapped) {
        try {
            clips.get(position).setClap(isClapped);
            clips.get(position).setClapCount(clips.get(position).getClapCount() + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ClipItem getItem(int playPosition) {
        if (clips.size() >= playPosition) {
            return clips.get(playPosition);
        }
        return null;
    }

    public void clearClips() {
        showEndOfListFooter = false;
        if (clips != null) {
            clips.clear();
        }
    }


    public class ClipViewHolder extends BaseViewHolder {

        ClipItemLayoutBinding mBinding;

        private boolean isInitialLikeSent = false;

        public ClipViewHolder(ClipItemLayoutBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            //initializePlayer(videoUrl, mBinding);
            mBinding.getRoot().setTag(this);
            ClipItem result = clips.get(position);
            mBinding.setResult(result);

            mBinding.setIsContentModerator(CommonUtils.isUserContentModerator());
            BindingUtils.setProfileImageUrlFromCache(mBinding.userPic, Objects.requireNonNull(result.getAuthor()).getProfilePic(), true);
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("clip_id", result.getId());
            properties.put("game", result.getGame());
            properties.put("username", result.getAuthor().getUser().getUsername());
            properties.put("title", result.getTitle());
            int totalClipViews = 0;
            if (result.getViewCount() != null)
                totalClipViews = result.getViewCount();

            if (mListener != null) {
                mListener.getFollowStatus(result, this);
            }
            mBinding.clipFollowCard.getRoot().setVisibility(View.GONE);


            if (result.getLiveStatus() != null && result.getLiveStatus().isLive())
                mBinding.clipFollowCard.liveStatusGroup.setVisibility(View.VISIBLE);
            else
                mBinding.clipFollowCard.liveStatusGroup.setVisibility(View.GONE);


            animateLiveTag(mBinding.clipFollowCard.liveIconCircle1, mBinding.clipFollowCard.liveIconCircle2);

            // mBinding.authorFollowers.setText(CommonUtils.getPlural("Follower", totalFollowers, ((totalFollowers / 1000 >= 1) ? (totalFollowers / 1000) + "." + ((totalFollowers % 1000) / 100) + "K" : totalFollowers + "")));
            mBinding.authorName.setText(result.getAuthor().getUser().getUsername());
            mBinding.postTitle.setText(result.getTitle());
            mBinding.clipNoViews.setText(CommonUtils.getPlural("view", totalClipViews, ((totalClipViews / 1000 >= 1) ? (totalClipViews / 1000) + "." + ((totalClipViews % 1000) / 100) + "K" : totalClipViews + "")));


            BindingUtils.loadBitmap(mBinding.defaultBg, result.getThumbnailUrl());

            mBinding.gameNameTv.setOnClickListener(view -> {
                SegmentTracker.getInstance(mBinding.followBtn.getContext()).trackEvent(SegmentConstants.EVENT_GAME_CLICK_CLIP, properties);
                mListener.stopAutoScroll();
                mListener.onGameClicked(result.getGameId(), result.getGame());
            });

            mBinding.userPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SegmentTracker.getInstance(mBinding.followBtn.getContext()).trackEvent(SegmentConstants.EVENT_AUTHOR_CLICK_CLIP, properties);
                    mListener.stopAutoScroll();
                    mListener.onProfileClicked(result.getAuthor().getUser().getUsername());

                }
            });

            mBinding.thumbsDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onThumbsDown(result);
                }

            });

            mBinding.gameNameTv.setText(result.getGame());

            updateFollowState(result);

            mBinding.followBtn.setOnClickListener(view -> {
                mListener.stopAutoScroll();
                if (!CommonUtils.isUserLoggedin()) {
                    mListener.showLoginFlow();
                } else {
                    if (mBinding.followBtn.isSelected()) {
                        mBinding.clipFollowCard.clipFollowParent.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#043990")));
                        mBinding.clipFollowCard.clipFollowBtn.setText("Follow");
                        mBinding.clipFollowCard.clipFollowLive.setText("Get notified when streamer is live");
                        mBinding.clipFollowCard.clipFollowText.setText("Follow " + result.getAuthor().getUser().getUsername());

                        // mBinding.authorFollowers.setText(result.getAuthor().getFollowersCount()-1);
                    } else {
                        mBinding.clipFollowCard.clipFollowText.setText(result.getAuthor().getUser().getUsername());
                        if (result.getLiveStatus() != null && result.getLiveStatus().isLive()) {
                            mBinding.clipFollowCard.clipFollowParent.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#161619")));
                            mBinding.clipFollowCard.clipFollowBtn.setText("Watch");
                        } else {
                            mBinding.clipFollowCard.liveStatusGroup.setVisibility(View.GONE);
                            if (mBinding.clipFollowCard.getRoot().getVisibility() == View.VISIBLE)
                                animateClipCardDownTitle();
                            mBinding.clipFollowCard.getRoot().setVisibility(View.GONE);
                        }

                    }
                    SegmentTracker.getInstance(mBinding.followBtn.getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_AUTHOR_CLIP, properties);
                    mBinding.followBtn.setSelected(!mBinding.followBtn.isSelected());
                    result.getAuthor().setFollowed(mBinding.followBtn.isSelected());
                    animateFollowBtn(mBinding.followBtn);
                    if (result.getAuthor().getBio() != null)
                        mBinding.clipFollowCard.clipFollowLive.setText(result.getAuthor().getBio().toString());

                    mListener.onFollowClicked(result, mBinding.followBtn.isSelected(), new FollowStatusCompleteListener() {
                        @Override
                        public void success() {
                            result.getAuthor().setFollowed(mBinding.followBtn.isSelected());
//                            notifyItemChanged(position);
                        }

                        @Override
                        public void error() {

                        }
                    });
                }
            });
            if (result.getClap()) {
                AppUtils.changeTopDrawable(mBinding.clapBtn, R.drawable.ic_like_heart_filled_48);
            } else {
                AppUtils.changeTopDrawable(mBinding.clapBtn, R.drawable.ic_heart_filled_white_48);
            }

            setClapCountText(mBinding.clapBtn, result.getClapCount());
            mBinding.clapBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.stopAutoScroll();
                    if (!CommonUtils.isUserLoggedin()) {
                        mListener.showLoginFlow();
                    } else {
                        SegmentTracker.getInstance(mBinding.followBtn.getContext()).trackEvent(SegmentConstants.EVENT_LIKE_CLIP, properties);
                        if (!result.getClap()) {
                            result.setClap(true);
                            result.setClapCount(result.getClapCount() + 1);
                            setClapCountText(mBinding.clapBtn, result.getClapCount());
                            AppUtils.changeTopDrawable(mBinding.clapBtn, R.drawable.ic_like_heart_filled_48);
                        } else {
                            result.setClap(false);
                            result.setClapCount(result.getClapCount() - 1);
                            setClapCountText(mBinding.clapBtn, result.getClapCount());
                            AppUtils.changeTopDrawable(mBinding.clapBtn, R.drawable.ic_heart_filled_white_48);
                        }
                        mListener.onClipCardClapClicked(result);
                    }
                }
            });
            mBinding.flagBtn.setOnClickListener(view -> {
                mListener.onClipCardReportClicked(result);
            });

            mBinding.clipMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!CommonUtils.isUserLoggedin()) {
                        mListener.showLoginFlow();
                    } else {
                        // SegmentTracker.getInstance(mBinding.followBtn.getContext()).trackEvent(SegmentConstants.EVENT_SHARE_CLIP, properties);
                        mListener.stopAutoScroll();
                        mListener.onClipMoreClicked(result);
                    }
                }
            });

            mBinding.shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!CommonUtils.isUserLoggedin()) {
                        mListener.showLoginFlow();
                    } else {
                        SegmentTracker.getInstance(mBinding.followBtn.getContext()).trackEvent(SegmentConstants.EVENT_SHARE_CLIP, properties);
                        mListener.stopAutoScroll();
                        mListener.onClipCardShareClicked(result);
                    }
                }
            });
            mBinding.clipFollowCard.clipFollowBtn.setOnClickListener(view -> {
                mListener.stopAutoScroll();
                if (!CommonUtils.isUserLoggedin()) {
                    mListener.showLoginFlow();
                } else {

                    if (result.getAuthor().isFollowed() && result.getLiveStatus() != null && result.getLiveStatus().isLive()) {
                        mBinding.clipFollowCard.liveStatusGroup.setVisibility(View.VISIBLE);
                        mBinding.clipFollowCard.clipFollowBtn.setText("Watch");
                        if (result.getLiveStatus().getLivePostId() != null)
                            mListener.onWatchNowClicked(result.getLiveStatus().getLivePostId());//Redirect to StreamPlayerActivity

                    } else {
                        SegmentTracker.getInstance(mBinding.clipFollowCard.clipFollowBtn.getContext()).trackEvent(SegmentConstants.EVENT_FOLLOW_AUTHOR_CLIP, properties);
                        mBinding.followBtn.setSelected(!result.getAuthor().isFollowed());
                        result.getAuthor().setFollowed(mBinding.followBtn.isSelected());
                        animateFollowBtn(mBinding.followBtn);


                        if (result.getLiveStatus() != null && result.getLiveStatus().isLive()) {
                            mBinding.clipFollowCard.clipFollowParent.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#161619")));
                            mBinding.clipFollowCard.clipFollowBtn.setText("Watch");
                            mBinding.clipFollowCard.liveStatusGroup.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.clipFollowCard.liveStatusGroup.setVisibility(View.GONE);
                            animateClipCardDownTitle();
                            mBinding.clipFollowCard.getRoot().setVisibility(View.GONE);
                        }

                        if (result.getAuthor().getBio() != null)
                            mBinding.clipFollowCard.clipFollowLive.setText(result.getAuthor().getBio().toString());
                        //mBinding.authorFollowers.setText(result.getAuthor().getFollowersCount()+1);
                        mBinding.clipFollowCard.clipFollowText.setText(result.getAuthor().getUser().getUsername());


                        mListener.onFollowClicked(result, result.getAuthor().isFollowed(), new FollowStatusCompleteListener() {
                            @Override
                            public void success() {
                                result.getAuthor().setFollowed(result.getAuthor().isFollowed());
//                                notifyItemChanged(position);
                            }

                            @Override
                            public void error() {

                            }
                        });
                        //mBinding.clipFollowCard.executePendingBindings();


                    }


                }
            });

//            mBinding.reportBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    SegmentTracker.getInstance(mBinding.followBtn.getContext()).trackEvent(SegmentConstants.EVENT_REPORT_CLIP, properties);
//                    mListener.stopAutoScroll();
//                    mListener.onClipCardReportClicked(result);
//                }
//            });

        }

        public void updateFollowState(ClipItem clipItem) {
            if (clipItem == null) return;
            if (clipItem.getAuthor() != null && clipItem.getAuthor().isFollowed() != null && clipItem.getAuthor().isFollowed()) {
                mBinding.followBtn.setSelected(true);
                mBinding.followBtn.setBackgroundResource(R.drawable.avd_correct_grey);
                mBinding.clipFollowCard.clipFollowParent.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#161619")));
                mBinding.clipFollowCard.clipFollowBtn.setText("View");
                if (clipItem.getLiveStatus() != null) {
                    if (clipItem.getLiveStatus().isLive())
                        mBinding.clipFollowCard.clipFollowBtn.setText("Watch");
                }
            } else {
                mBinding.followBtn.setSelected(false);
                mBinding.followBtn.setBackgroundResource(R.drawable.avd_add_background);

            }
            mBinding.executePendingBindings();
        }

        private void setClapCountText(TextView clapBtn, int clapCount) {
            if (clapCount > 0) {
                clapBtn.setText((clapCount / 1000 >= 1) ? (clapCount / 1000) + "." + ((clapCount % 1000) / 100) + "K" : clapCount + "");
            } else {
                clapBtn.setText("Clap");
            }
        }

        public ClipItemLayoutBinding getmBinding() {
            return mBinding;
        }

        public void animateFollowBtn(TextView followBtn) {
            Animation animation1 = AnimationUtils.loadAnimation(followBtn.getContext(), R.anim.rotate_scale_in_animation);
            Animation animation2 = AnimationUtils.loadAnimation(followBtn.getContext(), R.anim.rotate_scale_out_animation);
            animation1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mBinding.followBtn.startAnimation(animation2);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation2.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // mBinding.followBtn.setText(mBinding.followBtn.isSelected() ? "\u2713" : "\u002B");
                    mBinding.followBtn.setBackgroundResource(mBinding.followBtn.isSelected() ? R.drawable.avd_correct_grey : R.drawable.avd_add_background);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mBinding.followBtn.startAnimation(animation1);
        }

        private void animateLiveTag(View liveIconCircle1, View liveIconCircle2) {
            animateLiveTagScaleUp(liveIconCircle1, 0);
            animateLiveTagScaleUp(liveIconCircle2, 500);
        }

        private void animateLiveTagScaleUp(View liveIcon, long delay) {
            ObjectAnimator animX = ObjectAnimator.ofFloat(liveIcon, View.SCALE_X, 1f, 3f);
            ObjectAnimator animY = ObjectAnimator.ofFloat(liveIcon, View.SCALE_Y, 1f, 3f);
            ObjectAnimator animFadeOut = ObjectAnimator.ofFloat(liveIcon, View.ALPHA, 1f, 0f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(1000);
            animatorSet.setStartDelay(delay);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    animateLiveTagScaleUp(liveIcon, delay);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.playTogether(animX, animY, animFadeOut);
            animatorSet.start();
        }

        public void animateClipCardDownTitle() {
            if (mBinding == null) return;
            // Animation animation1 = AnimationUtils.loadAnimation(mBinding.clipFollowCard.getRoot().getContext(), R.anim.slide_in_down);
            Animation animation2 = AnimationUtils.loadAnimation(mBinding.postTitle.getContext(), R.anim.scale_down);

            Animation animation3 = AnimationUtils.loadAnimation(mBinding.gameNameTv.getContext(), R.anim.scale_down);
            //mBinding.clipFollowCard.getRoot().setVisibility(View.GONE);
            animation2.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //mBinding.clipFollowCard.getRoot().setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mBinding.postTitle.startAnimation(animation2);
            mBinding.gameNameTv.startAnimation(animation3);

        }


        public void animateClipCard() {
            try {
                if (mBinding == null ||
                        (mBinding.getResult() != null && mBinding.getResult().getAuthor() != null
                                && mBinding.getResult().getAuthor().isFollowed()
                                && mBinding.getResult().getLiveStatus() != null
                                && !mBinding.getResult().getLiveStatus().isLive()
                        ) ||
                        mBinding.clipFollowCard.getRoot().getVisibility() == View.VISIBLE) return;
                Animation animation1 = AnimationUtils.loadAnimation(mBinding.clipFollowCard.getRoot().getContext(), R.anim.slide_in_up);
                Animation animation2 = AnimationUtils.loadAnimation(mBinding.postTitle.getContext(), R.anim.scale_up);

                Animation animation3 = AnimationUtils.loadAnimation(mBinding.gameNameTv.getContext(), R.anim.scale_up);
                mBinding.clipFollowCard.getRoot().setVisibility(View.VISIBLE);
                animation1.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        //mBinding.clipFollowCard.getRoot().setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mBinding.postTitle.startAnimation(animation2);
                mBinding.gameNameTv.startAnimation(animation3);
                mBinding.clipFollowCard.getRoot().startAnimation(animation1);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public class FooterEndOfDateViewHolder extends BaseViewHolder {
        public FooterEndOfDateViewHolder(ClipsRvFooterLayoutBinding binding) {
            super(binding.getRoot());
            Context context = binding.getRoot().getContext();
            if (!NetworkUtils.isNetworkConnected(context)) {
                binding.textView.setText(context.getString(R.string.last_offline_clip_text));
                binding.executePendingBindings();
            }
        }

        @Override
        public void onBind(int position) {

        }
    }


    public interface OnClipCardItemsClick {
        void onClipCardShareClicked(ClipItem result);

        void onClipCardClapClicked(ClipItem result);

        void onClipCardReportClicked(ClipItem result);

        void onClipMoreClicked(ClipItem result);

        void onFollowClicked(ClipItem result, boolean isFollowed, FollowStatusCompleteListener listener);

        void onProfileClicked(String authorUsername);

        void onWatchNowClicked(String liveId);

        void onGameClicked(String gameId, String game);

        void showLoginFlow();

        void stopAutoScroll();

        void onThumbsDown(ClipItem result);

        void getFollowStatus(ClipItem result, ClipsListAdapter.ClipViewHolder clipViewHolder);

    }

}