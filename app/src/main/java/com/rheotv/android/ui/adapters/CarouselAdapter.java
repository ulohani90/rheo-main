package com.rheotv.android.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.VideoUrlObj;
import com.rheotv.android.databinding.CarouselItemBinding;
import com.rheotv.android.databinding.CarouselSeeAllLayoutBinding;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.PlayerHeadServiceHelper;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.List;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;
import static com.rheotv.android.utils.NetworkUtils.NETWORK_QUALITY_HIGH;
import static com.rheotv.android.utils.NetworkUtils.NETWORK_QUALITY_MEDIUM;
import static com.rheotv.android.utils.NetworkUtils.NETWORK_TYPE_MOBILE;

public class CarouselAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final String TAG = CarouselAdapter.class.getSimpleName();
    private List<PostObject> results;

    private CarouselItemClickListener listener;

    boolean isLiveFeedCarousel = false;

    public static String PLAYER_TAG = "Player Video";

    public PlayerView mPlayerView;

    public SimpleExoPlayer mPlayer;

    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    boolean isVideoAdded;

    public static final int VIEW_TYPE_SEE_ALL = 1;

    public static final int VIEW_TYPE_CAROUSEL_ITEM = 2;

    public String gameId;

    public String gameTitle;

    public boolean showSeeAll;

    String carouselTitle;

    public CarouselAdapter(List<PostObject> results, String gameId, String gameTitle, boolean showSeeAll, String carouselTitle) {
        this.results = results;
        this.gameId = gameId;
        this.gameTitle = gameTitle;
        this.showSeeAll = showSeeAll;
        this.carouselTitle = carouselTitle;
    }

    public CarouselAdapter(List<PostObject> results) {
        this.results = results;
    }

    public void setmPlayerView(PlayerView player) {
        this.mPlayerView = player;
    }

    public void setmPlayer(SimpleExoPlayer mPlayer) {
        this.mPlayer = mPlayer;
    }

    public void setLiveFeedCarousel(boolean liveFeedCarousel) {
        isLiveFeedCarousel = liveFeedCarousel;
    }

    public void setListener(CarouselItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PostObject> results) {
        this.results = results;
    }

    /**
     * Recursively unbind any resources from the provided view. This method will clear the resources of all the
     * children of the view before invalidating the provided view itself.
     *
     * @param view The view for which to unbind resource.
     */
    protected void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }


    private void showMoreOptionsLayout(View view) {
        view.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1);
        animator.setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void hideMoreOptionsLayout(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 1, 0);
        animator.setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
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

    private void animateLiveTagScaleIn(ImageView liveIcon) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(liveIcon, View.SCALE_X, 1.3f, 1.0f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(liveIcon, View.SCALE_Y, 1.3f, 1.0f);
        ObjectAnimator animFadeIn = ObjectAnimator.ofFloat(liveIcon, View.ALPHA, 0.2f, 1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // animateLiveTagScaleUp(liveIcon);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.playTogether(animX, animY, animFadeIn);
        animatorSet.start();
    }


   /* private SimpleExoPlayer getPlayer(Result currentPlayingPost) {

        final SimpleExoPlayer[] mPlayer = {ExoPlayerClass.getInstance()};
        // mPlayer[0].removeListener(playerListener);
        // mPlayer[0].addListener(playerListener);
        return mPlayer[0];
    }*/

    private void initializePlayer(PostObject currentPlayingPost, CarouselItemBinding carouselItemBinding) {
        //removePreviouslyAddedView();
        Log.i(TAG, "Initializing ExoPlayer");

        mPlayerView.setPlayer(mPlayer);

        Player.DefaultEventListener playerListener = new Player.DefaultEventListener() {

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                super.onTimelineChanged(timeline, manifest, reason);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.i("Tag", "State");
                switch (playbackState) {
                    case Player.STATE_BUFFERING:

                        if (carouselItemBinding.mediaLoadProgress != null) {
                            carouselItemBinding.mediaLoadProgress.setVisibility(View.GONE);
                        }

                        break;
                    case Player.STATE_READY:
                        if (carouselItemBinding.mediaLoadProgress != null) {
                            carouselItemBinding.mediaLoadProgress.setVisibility(View.GONE);
                        }
                        if (!isVideoAdded) {
                            addVideoPlayerView(carouselItemBinding);
                            isVideoAdded = true;
                        }
                        break;
                    case Player.STATE_ENDED:
                        break;

                }

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                super.onPlayerError(error);
                if (carouselItemBinding != null) {
                /*mPlayer[0].stop();
                mPlayer[0].release();
                mPlayer[0] = null;
                initializePlayer(currentPlayingPost, carouselItemBinding);*/
                }
            }
        };

        mPlayer.removeListener(playerListener);
        mPlayer.addListener(playerListener);

        ((SimpleExoPlayer) mPlayerView.getPlayer()).setVolume(0.0F);
        /*if (currentPlayingPost.getVolume() == null) {

        } else {
            ((SimpleExoPlayer) mPlayerView.getPlayer()).setVolume(Float.parseFloat(currentPlayingPost.getVolume()));
        }*/
        String uriString = "";
        if (currentPlayingPost.getVideoUrls() != null && !currentPlayingPost.getVideoUrls().isEmpty()) {
            String networkQuality = NetworkUtils.getNetworkGeneration(mPlayerView.getContext());
            String networkType = NetworkUtils.getNetworkType(mPlayerView.getContext());
            if (networkType.equals(NETWORK_TYPE_MOBILE) && networkQuality.equals(NETWORK_QUALITY_HIGH)) {
                networkQuality = NETWORK_QUALITY_MEDIUM;
            }
            uriString = setVideoUrl(networkQuality, NETWORK_QUALITY_HIGH, currentPlayingPost.getVideoUrls());
            if (uriString == null || uriString.isEmpty())
                setVideoUrl(networkQuality, NETWORK_QUALITY_MEDIUM, currentPlayingPost.getVideoUrls());
            if (uriString == null || uriString.isEmpty())
                setVideoUrl(networkQuality, NetworkUtils.NETWORK_QUALITY_LOW, currentPlayingPost.getVideoUrls());
        }
        if ((uriString == null || uriString.isEmpty()) && currentPlayingPost.getVideoUrl() != null) {
            uriString = currentPlayingPost.getVideoUrl();
        }
        if (uriString != null && !uriString.isEmpty()) {
            Uri uri = Uri.parse(uriString);
            MediaSource mediaSource = buildMediaSource(uri);
            ((SimpleExoPlayer) mPlayerView.getPlayer()).prepare(mediaSource, false, true);
        }
        if (!PlayerHeadServiceHelper.getInstance().isServiceRunning())
            mPlayer.setPlayWhenReady(true);


        //carouselItemBinding.executePendingBindings();
    }

    private String setVideoUrl(String networkQuality, String requiredNetworkQuality, List<VideoUrlObj> videoUrlObjList) {
        if (networkQuality.equalsIgnoreCase(requiredNetworkQuality)) {
            for (VideoUrlObj url : videoUrlObjList) {
                if (url.getUrl() != null && url.getUrl().toLowerCase().contains(requiredNetworkQuality.toLowerCase())) {
                    return url.getUrl();
                }
            }
        }
        return "";
    }

    public void addVideoPlayerView(CarouselItemBinding carouselItemBinding) {
        removePreviouslyAddedView();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        if (mPlayerView != null) {
            carouselItemBinding.mediaContainer.addView(mPlayerView, lp);
            mPlayerView.invalidate();
        }
    }


    private void removePreviouslyAddedView() {
        if (mPlayerView == null) return;
        ViewGroup parent = (ViewGroup) mPlayerView.getParent();
        if (parent == null) {
            return;
        }
        int childIndex = parent.indexOfChild(mPlayerView);
        if (childIndex >= 0) {
            parent.removeViewAt(childIndex);
        }

    }

    private MediaSource buildMediaSource(Uri uri) {
        String url = uri.toString();
        if (url.substring(url.lastIndexOf(".")).equals(".m3u8")) {
            DataSource.Factory factory = new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT);
            return new HlsMediaSource.Factory(factory).
                    createMediaSource(uri);
        }
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT)).
                createMediaSource(uri);

    }


    public void playVideo() {

    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CAROUSEL_ITEM) {
            CarouselItemBinding carouselItemBinding = CarouselItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CarouselItemViewHolder(carouselItemBinding);
        }
        CarouselSeeAllLayoutBinding binding = CarouselSeeAllLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SeeAllLayoutViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == results.size()) {
            return VIEW_TYPE_SEE_ALL;
        }

        return VIEW_TYPE_CAROUSEL_ITEM;
    }

    @Override
    public int getItemCount() {
        return results != null ? ((results.size() > 1 && showSeeAll) ? results.size() + 1 : results.size()) : 0;
    }


    public class CarouselItemViewHolder extends BaseViewHolder {
        CarouselItemBinding carouselItemBinding;

        public CarouselItemViewHolder(CarouselItemBinding binding) {
            super(binding.getRoot());
            carouselItemBinding = binding;
        }

        @Override
        public void onBind(int position) {
            Log.e(TAG, "psoition --> " + position);
            PostObject result = results.get(position);
            RelativeLayout carouselItemLinearLayout = carouselItemBinding.carouselItemRelativeLayout;
            View moreOptionsLayout = carouselItemBinding.moreOptionsLayout;

            TextView textView = carouselItemBinding.carouselTextView;
            textView.setText(results.get(position).getTitle());
            carouselItemLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCarouselItemClicked(result.getId(), results, result, carouselTitle);
                    //recordVideoClick(result, carouselItemBinding.volumeBtn.getVisibility() == View.VISIBLE);

                }
            });
            carouselItemLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showMoreOptionsLayout(moreOptionsLayout);
                    return true;
                }
            });
            carouselItemBinding.moreOptionsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });
            carouselItemBinding.moreOptionsLayout.findViewById(R.id.follow_btn).setVisibility(View.GONE);
            carouselItemBinding.moreOptionsLayout.findViewById(R.id.delete_btn).setVisibility(View.GONE);
            carouselItemBinding.moreOptionsLayout.findViewById(R.id.download_btn).setVisibility(View.GONE);

            carouselItemBinding.moreOptionsLayout.findViewById(R.id.share_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onShareButtonClicked(result);
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });

            carouselItemBinding.moreOptionsLayout.findViewById(R.id.report_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onMoreOptionBtnClicked(result.getId());
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });

            if (position == 0 && isLiveFeedCarousel) {
                carouselItemBinding.mediaContainer.setVisibility(View.VISIBLE);
                carouselItemBinding.getRoot().setTag(PLAYER_TAG);
                if (mPlayerView != null)
                    initializePlayer(result, carouselItemBinding);
                if (result.isLive()) {
                    carouselItemBinding.liveTagOld.setVisibility(View.GONE);
                    SpannableString liveNowText = new SpannableString("LIVE " + ((result.getAuthor() != null && result.getAuthor().getUser() != null) ? result.getAuthor().getUser().getUsername() + " is " : "") + ((result.getGame() != null && result.getGame().getName().length() > 0) ? "streaming " + result.getGame().getName() : ""));
                    liveNowText.setSpan(new ForegroundColorSpan(Color.parseColor("#E2574C")), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    carouselItemBinding.liveNowText.setText("LIVE");
                    //carouselItemBinding.liveNowText.setText("ChennaiCityGame is Live now streaming Call of Duty- Mordern warfare ");
                    carouselItemBinding.liveTag.setVisibility(View.VISIBLE);
                    animateLiveTag(carouselItemBinding.liveIconCircle1, carouselItemBinding.liveIconCircle2);
                    //animateLiveTagScaleUp(carouselItemBinding.liveIcon);
                } else {
                    carouselItemBinding.liveTagOld.setVisibility(View.GONE);
                /*carouselItemBinding.liveIconCircle1.clearAnimation();
                carouselItemBinding.liveIconCircle2.clearAnimation();*/
                    carouselItemBinding.liveTag.setVisibility(View.GONE);

                }
                carouselItemBinding.volumeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mPlayerView != null && mPlayerView.getPlayer() != null) {
                            if (carouselItemBinding.volumeBtn.isSelected()) {
                                ((SimpleExoPlayer) mPlayerView.getPlayer()).setVolume(0);
                            } else {
                                ((SimpleExoPlayer) mPlayerView.getPlayer()).setVolume(10.0f);
                            }
                            carouselItemBinding.volumeBtn.setSelected(!carouselItemBinding.volumeBtn.isSelected());
                            recordVideoVolumeClick(result);
                        }
                    }
                });
                ImageView imageView = carouselItemBinding.imageView;
                try {
                    BindingUtils.setImageUrlUsingCache(imageView, result.getThumbnail(), true);
                } catch (Exception e) {
                    Log.d("hello", e.getMessage());
                }
            } else {
                carouselItemBinding.mediaContainer.setVisibility(View.GONE);
                if (result.isLive()) {
                    carouselItemBinding.liveTagOld.setVisibility(View.VISIBLE);
                } else {
                    carouselItemBinding.liveTagOld.setVisibility(View.GONE);
                }
                carouselItemBinding.liveTag.setVisibility(View.GONE);
                ImageView imageView = carouselItemBinding.imageView;
                try {
                    BindingUtils.setImageUrlUsingCache(imageView, result.getThumbnail(), true);
                } catch (Exception e) {
                    Log.d("hello", e.getMessage());
                }
            }
            if (result.getAuthor() != null)
                BindingUtils.setProfileImageUrlFromCache(carouselItemBinding.authorProfilePic, result.getAuthor().getProfilePic(), true);
            carouselItemBinding.authorName.setText(result.getAuthor().getUser().getUsername());
            carouselItemBinding.titleTextView.setText(result.getTitle());
            SpannableString gameText = new SpannableString((result.isLive() ? "Streaming " : "Streamed ") + result.getGame().getName());
            gameText.setSpan(new ForegroundColorSpan(Color.parseColor("#aeaeb2")), 0, result.isLive() ? 9 : 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //gameText.setSpan(new StyleSpan(Typeface.BOLD), 10, gameText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            carouselItemBinding.authorGame.setText(result.getGame().getName());
            if (result.getLanguage() != null) {
                carouselItemBinding.languageTv.setVisibility(View.VISIBLE);
                carouselItemBinding.languageTv.setText(result.getLanguage());
            } else {
                carouselItemBinding.languageTv.setVisibility(View.GONE);
            }

            carouselItemBinding.authorGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onGameClicked(result.getGame().getId(), result.getGame().getId());
                }
            });

            if (result.getWatchingCount() > 0) {
                int totalWatch = result.getWatchingCount();
                carouselItemBinding.listingTvNoofviews.setText(((totalWatch / 1000 >= 1) ? (totalWatch / 1000) + "." + ((totalWatch % 1000) / 100) + "K" : totalWatch) + " Watching");
                carouselItemBinding.listingIvViews.setVisibility(View.GONE);
            } else {
                carouselItemBinding.listingIvViews.setVisibility(View.VISIBLE);
                if (result.getTotalViews() != 0) {
                    int totalViews = result.getTotalViews();
                    carouselItemBinding.listingTvNoofviews.setText((totalViews / 1000 >= 1) ? (totalViews / 1000) + "." + ((totalViews % 1000) / 100) + "K" : totalViews + "");
                } else {
                    carouselItemBinding.listingTvNoofviews.setText("1");
                }
            }

            carouselItemBinding.authorProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onAuthorClicked(result.getAuthor().getUser().getUsername());
                }
            });

            carouselItemBinding.moreOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // listener.onMoreOptionBtnClicked(result.getId());
                    if (moreOptionsLayout.isShown()) {
                        hideMoreOptionsLayout(moreOptionsLayout);
                    } else {
                        showMoreOptionsLayout(moreOptionsLayout);
                    }
                }
            });
        }

        private void recordVideoClick(PostObject result, boolean isFeatured) {
            HashMap<String, Object> properties = new HashMap<>();

            properties.put("postId", result.getId());
            properties.put("is_live", result.isLive());
            properties.put("type", result.isLive() ? "live" : "fullRecorded");
            properties.put("game_id", result.getGame().getId());
            properties.put("title", result.getTitle());
            properties.put("is_feature", isFeatured);
            properties.put("game_name", result.getGame().getName());
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_VIDEO_CLICKED, properties);
        }

        private void recordVideoVolumeClick(PostObject result) {
            HashMap<String, Object> properties = new HashMap<>();

            properties.put("postId", result.getId());
            properties.put("game_id", result.getGame().getId());
            properties.put("title", result.getTitle());
            properties.put("game_name", result.getGame().getName());
            SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_VIDEO_VOLUME_CLICKED,
                    properties);
        }
    }

    public class SeeAllLayoutViewHolder extends BaseViewHolder {

        CarouselSeeAllLayoutBinding mBinding;

        public SeeAllLayoutViewHolder(CarouselSeeAllLayoutBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            mBinding.seeAllBtn.setVisibility(View.VISIBLE);
            mBinding.seeAllBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onSeeAllClicked(gameId, gameTitle);
                }
            });
        }
    }

    public interface CarouselItemClickListener {

        void onCarouselItemClicked(String id, List<PostObject> results, PostObject post, String carouselTitle);


        void onSingleItemInCarousel();

        void onMoreOptionBtnClicked(String id);

        void onFollowBtnClicked(String author, int id, boolean isFollowed, OnFollowActionCompleteListener listener);

        void onShareButtonClicked(PostObject post);

        void onGameClicked(String game, String gameId);

        void onAuthorClicked(String username);

        void onSeeAllClicked(String gameId, String gameTitle);

    }


}