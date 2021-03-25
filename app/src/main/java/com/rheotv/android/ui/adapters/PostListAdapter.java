/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 4/1/19 12:06 AM
 *
 */

package com.rheotv.android.ui.adapters;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.FeedObject;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.objects.StreamerObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.databinding.ItemCarouselViewBinding;
import com.rheotv.android.databinding.ItemPostTopGamesLayoutBinding;
import com.rheotv.android.databinding.ItemPostViewBinding;
import com.rheotv.android.databinding.ItemSuperStreamersCarouselBinding;
import com.rheotv.android.databinding.ItemTopStreamersCardBinding;
import com.rheotv.android.databinding.LayoutStoryContainerBinding;
import com.rheotv.android.databinding.ShimmerPostLayoutBinding;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostEmptyItemViewModel;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostItemViewModel;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.ui.decorators.TopGamesRvItemDecorator;
import com.rheotv.android.ui.decorators.TopStreamerItemDecorator;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.recyclerdecorators.HorizontalSpacesItemDecoration;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.recyclerdecorators.ViewPagerItemDecoration;
import com.segment.analytics.Properties;

import java.util.ArrayList;
import java.util.List;

import static com.rheotv.android.utils.AppConstants.SHIMMER_LOADER;


public class PostListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private static final String TAG = PostListAdapter.class.getSimpleName();
    private List<FeedObject> mPostList;

    private BlogAdapterListener mListener;

    private StoryAdapter.OnStoryInteractionListener storyInteractionListener;

    private boolean isLoading = true;
    private Handler handler = new Handler();
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    boolean showLoadingView = false;

    boolean firstLiveAlreadyShown = false;

    public static final String LIVE_VIDEO_TAG = "Live Video";

    public PlayerView mPlayerView;

    public SimpleExoPlayer mPlayer;

    RecyclerView.RecycledViewPool recycledViewPool;
    private FeedObject storyFeed;

    boolean isUpdatingPlayer;

    StoryAdapter storyAdapter;

    public PostListAdapter(List<FeedObject> mPostList) {
        this.mPostList = mPostList;
    }

    public PostListAdapter(Context context, List<FeedObject> mPostList) {
        this.mPostList = mPostList;
        calculateHeightForCarouselItems(context);
        recycledViewPool = new RecyclerView.RecycledViewPool();
    }

    public void addStories(ArrayList<ProfileResult> list) {
        if (storyFeed == null) {
            storyFeed = new FeedObject(AppConstants.VIEW_TYPE_STORY, list);
            mPostList.add(0, storyFeed);
        } else {
            storyFeed.setStoryAuthors(list);
        }
        if (mPostList.size() == 1)
            setShowLoadingView(true);
        notifyDataSetChanged();
    }

    public void addPageStory(ArrayList<ProfileResult> list) {
        if (storyFeed == null)
            storyFeed = new FeedObject(AppConstants.VIEW_TYPE_STORY, new ArrayList<>());
        storyFeed.getStoryAuthors().addAll(list);
        if (storyAdapter != null)
            storyAdapter.showLoading(false);
    }

    public void showStoryLoading(boolean flag) {
        if (storyAdapter != null)
            storyAdapter.showLoading(flag);
    }

    public void removeStories(int index) {
        try {
            if (mPostList.get(index).getType() == AppConstants.VIEW_TYPE_STORY) {
                mPostList.remove(index);
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStoryListener(StoryAdapter.OnStoryInteractionListener storyInteractionListener) {
        this.storyInteractionListener = storyInteractionListener;
    }

    @Override
    public int getItemCount() {
        if (mPostList != null && mPostList.size() > 0) {
            Log.d("POSTLISTADAPTER", mPostList.size() + " size ki list");
            if (showLoadingView) {
                return mPostList.size() + 1;
            } else {
                return mPostList.size();
            }
        } else {
            return 2;
        }
    }

    public void setShowLoadingView(boolean showLoadingView) {
        this.showLoadingView = showLoadingView;
        if (showLoadingView) {
            notifyItemInserted(mPostList.size());
        } else {

            notifyItemRemoved(mPostList.size());
        }
    }

    public void showShimmerLoading(boolean loading) {
//        try {
//            if (loading)
//                mPostList.add(new FeedObject(SHIMMER_LOADER, new ArrayList<>()));
//            else {
//                if (!mPostList.isEmpty()) mPostList.remove(1);
//            }
//
//            notifyDataSetChanged();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public int getItemViewType(int position) {
        if (mPostList != null && !mPostList.isEmpty()) {
            if (position == mPostList.size()) {
                return AppConstants.VIEW_TYPE_LOADING_FOOTER;
            } else if (mPostList.get(position).getType() == 0)
                return AppConstants.VIEW_TYPE_NORMAL;
            else if (mPostList.get(position).getType() == 2) {
                return AppConstants.VIEW_TYPE_CAROUSEL;
            } else if (mPostList.get(position).getType() == 7) {
                return AppConstants.VIEW_TOP_STREAMERS;
            } else if (mPostList.get(position).getType() == 10) {
                return AppConstants.VIEW_TYPE_SUPER_PRIME_STREAMER;
            } else if (mPostList.get(position).getType() == 11) {
                return AppConstants.VIEW_TYPE_TOP_GAMES;
            } else if (mPostList.get(position).getType() == AppConstants.VIEW_TYPE_STORY) {
                return AppConstants.VIEW_TYPE_STORY;
            } else if (mPostList.get(position).getType() == SHIMMER_LOADER) {
                return AppConstants.VIEW_TYPE_EMPTY;
            } else {
                return AppConstants.VIEW_TYPE_NORMAL;
            }
        } else {
            return AppConstants.VIEW_TYPE_EMPTY;
        }
    }


    public void updatePlayerView() {
        isUpdatingPlayer = true;
        notifyItemChanged(0);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case AppConstants.VIEW_TYPE_NORMAL:
                ItemPostViewBinding blogViewBinding = ItemPostViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                ViewGroup.LayoutParams params = blogViewBinding.parent.getLayoutParams();
                params.height = heightSingleItemCarousel;
                blogViewBinding.parent.setLayoutParams(params);
                return new BlogViewHolder(blogViewBinding);
            case AppConstants.VIEW_TYPE_CAROUSEL:
                ItemCarouselViewBinding carouselViewBinding = ItemCarouselViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                ViewGroup.LayoutParams lp = carouselViewBinding.carouselRv.getLayoutParams();
                lp.height = heightSingleItemCarousel;
                carouselViewBinding.carouselRv.setLayoutParams(lp);
                return new CarouselViewHolder(parent.getContext(), carouselViewBinding);
            case AppConstants.VIEW_TYPE_EMPTY:
                ShimmerPostLayoutBinding emptyViewBinding = ShimmerPostLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new EmptyViewHolder(emptyViewBinding);
            case AppConstants.VIEW_TOP_STREAMERS:
                ItemTopStreamersCardBinding itemTopStreamersCardBinding = ItemTopStreamersCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new TopStreamersViewHolder(itemTopStreamersCardBinding);
            case AppConstants.VIEW_TYPE_LOADING_FOOTER:
                FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
            case AppConstants.VIEW_TYPE_SUPER_PRIME_STREAMER:
                ItemSuperStreamersCarouselBinding primeStreamersCarouselViewBinding = ItemSuperStreamersCarouselBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new SuperPrimeStreamersViewHolder(parent.getContext(), primeStreamersCarouselViewBinding);
            case AppConstants.VIEW_TYPE_TOP_GAMES:
                ItemPostTopGamesLayoutBinding itemPostTopGamesLayoutBinding = ItemPostTopGamesLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new TopGamesViewHolder(itemPostTopGamesLayoutBinding);

            case AppConstants.VIEW_TYPE_STORY:
                LayoutStoryContainerBinding binding = LayoutStoryContainerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new StoryViewHolder(binding);
            default:
                ShimmerPostLayoutBinding emptyBinding = ShimmerPostLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new EmptyViewHolder(emptyBinding);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    public void addItems(List<FeedObject> mPostList, int oldSize) {
        int startPos = this.mPostList.size();
        this.isLoading = mPostList != null && mPostList.size() > 0;
        if (mPostList == null || mPostList.size() < 1) {
            return;
        }
        this.mPostList.addAll(mPostList);
        if (startPos == 0) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(startPos, mPostList.size());
        }

        /*if (startPos < 1) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(startPos, mPostList.size());
        }*/
    }

    public void clearItems() {
        storyFeed = null;
        mPostList.clear();
        notifyDataSetChanged();
    }

    public int getPostListSize() {
        return mPostList.size();
    }

    public void setListener(BlogAdapterListener listener) {
        this.mListener = listener;
    }

    public interface BlogAdapterListener {
        void onItemClick(String id, PostObject post);

        void onRetryClick();

        void onLikeButtonClicked(String body, Result post);

        void onShareButtonClicked(PostObject post);

        void onAuthorClicked(String userName);

        void onCarouselItemClicked(String id, List<PostObject> results, PostObject postObject, String carouselTitle);

        void onMultiViewItemClicked(String id, List<PostObject> results);

        void onSeeMoreClicked(List<PostObject> result);

        void onLeaderboardClicked(String id);

        void onSeeAllClicked(String game, String id);

        void onAlertCardClicked();

        void onFollowBtnClicked(String author, int authorId, boolean isFollowed, OnFollowActionCompleteListener listener);

        void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener);

        void onMoreOptionsBtnClick(String id);

        void onSuperPrimeReminderListener(PostObject result);

        void onSuperStreamerCardClick(String id);

        void onGameClicked(String game, String gameId);

        void onDeleteVideoClicked(String id, int position);

        void onDownloadVideoClicked(String id, int position);
    }


    int heightSingleItemCarousel;

    int heightMultiItemCarousel;

    int superPrimeStreamerCardWidth;

    int superPrimeStreamerCardHeight;

    public void calculateHeightForCarouselItems(Context context) {
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        int width = outMetrics.widthPixels - (2 * (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, outMetrics));
        heightSingleItemCarousel = ((width * 9) / 16) + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, outMetrics);
        heightMultiItemCarousel = ((((width * 9) / 10) * 9) / 16) + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, outMetrics);
        superPrimeStreamerCardWidth = outMetrics.widthPixels - (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, outMetrics));
        superPrimeStreamerCardHeight = (superPrimeStreamerCardWidth * 360) / 540;
    }

    public class CarouselViewHolder extends BaseViewHolder implements CarouselAdapter.CarouselItemClickListener, ViewPager.OnPageChangeListener {
        private ItemCarouselViewBinding mBinding;
        private CarouselAdapter adapter;
        private Context context;
        boolean isItemDecorated = false;

        public CarouselViewHolder(Context context, ItemCarouselViewBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            this.context = context;
        }

        @Override
        public void onBind(int position) {
            final FeedObject result = mPostList.get(position);
            boolean isLiveFeedCarousel = false;

            if (result.getCount() > result.getPosts().size()) {
                mBinding.seeAllIcon.setVisibility(View.VISIBLE);
                mBinding.categoryTitleSeeAll.setVisibility(View.VISIBLE);
                mBinding.seeAllIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onSeeAllClicked(result.getTitle(), result.getGameId());
                    }
                });
                mBinding.categoryTitleSeeAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mListener.onSeeAllClicked(result.getTitle(), result.getGameId());
                    }
                });
            } else {
                mBinding.seeAllIcon.setVisibility(View.GONE);
                mBinding.categoryTitleSeeAll.setVisibility(View.GONE);
            }

            if (result.getTitle() == null || result.getTitle().length() == 0) {
                isLiveFeedCarousel = true;
            }
            if (isLiveFeedCarousel) {
                mBinding.separator.setVisibility(View.GONE);
                mBinding.headerTitle.setVisibility(View.GONE);
                mBinding.title.setVisibility(View.GONE);
                firstLiveAlreadyShown = true;
            } else {
                mBinding.separator.setVisibility(View.GONE);
                mBinding.headerTitle.setVisibility(View.VISIBLE);
                mBinding.title.setVisibility(View.VISIBLE);
                mBinding.title.setText(result.getTitle());
                firstLiveAlreadyShown = false;
            }

            //ViewPager2 viewPager = mBinding.viewpager;

            RecyclerView viewPager = mBinding.carouselRv;
            viewPager.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            SnapHelper snapHelper = new PagerSnapHelper();
            viewPager.setOnFlingListener(null);
            snapHelper.attachToRecyclerView(viewPager);
            /*int height;
            if (result.getPosts().size() > 1) {
                height = heightMultiItemCarousel;
            } else {
                height = heightSingleItemCarousel;
            }
            ViewGroup.LayoutParams lp = mBinding.viewpager.getLayoutParams();
            lp.height = height;
            mBinding.viewpager.setLayoutParams(lp);*/

            adapter = new CarouselAdapter(result.getPosts(), result.getGameId(), result.getTitle(), result.getCount() > result.getPosts().size(), result.getTitle());
            boolean showLiveFeed = isLiveFeedCarousel ? isLiveFeedCarousel : (position == 0 && !firstLiveAlreadyShown && result.getPosts().get(0).isLive());
            if (showLiveFeed) {
                mBinding.getRoot().setTag(LIVE_VIDEO_TAG);
                adapter.setmPlayerView(getPlayerView(mBinding.getRoot().getContext()));
                adapter.setmPlayer(getPlayer(mBinding.getRoot().getContext()));
            } else {
                mBinding.getRoot().setTag(null);
            }
            /*viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (position == adapter.getItemCount() - 1 && result.getPosts().size() > 1) {
                        mBinding.seeAllBtn.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.seeAllBtn.setVisibility(View.GONE);
                    }
                }
            });*/
            adapter.setLiveFeedCarousel(showLiveFeed);

            adapter.setListener(this);
            viewPager.setAdapter(adapter);
            //viewPager.setPageMargin((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, context.getResources().getDisplayMetrics()));
//            autoScrollCarousel();

            if (!isItemDecorated) {
                viewPager.addItemDecoration(new ViewPagerItemDecoration());
                /*View recyclerViewInstance = viewPager.getChildAt(0);
                if (recyclerViewInstance instanceof RecyclerView) {
                    ((RecyclerView) recyclerViewInstance).setClipChildren(false);
                    ((RecyclerView) recyclerViewInstance).setClipToPadding(false);
                    ((RecyclerView) recyclerViewInstance).addItemDecoration(new ViewPagerItemDecoration());
                }*/

                //viewPager.setOffscreenPageLimit(1);
                isItemDecorated = true;
            }

            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();
        }

        @Override
        public void onAuthorClicked(String authorUserName) {
            mListener.onAuthorClicked(authorUserName);
        }

        @Override
        public void onSeeAllClicked(String gameId, String gameTitle) {
            mListener.onSeeAllClicked(gameTitle, gameId);
        }

        @Override
        public void onCarouselItemClicked(String id, List<PostObject> results, PostObject post, String carouselTitle) {
            sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_CARD_TYPE, "carousel_post");
            mListener.onCarouselItemClicked(id, results, post, carouselTitle);
        }

        @Override
        public void onSingleItemInCarousel() {

        }

        @Override
        public void onMoreOptionBtnClicked(String id) {
            mListener.onMoreOptionsBtnClick(id);
        }

        @Override
        public void onFollowBtnClicked(String author, int id, boolean isFollowed, OnFollowActionCompleteListener listener) {
            mListener.onFollowBtnClicked(author, id, isFollowed, listener);
        }

        @Override
        public void onShareButtonClicked(PostObject post) {
            mListener.onShareButtonClicked(post);
        }

        @Override
        public void onGameClicked(String game, String gameId) {
            mListener.onGameClicked(game, gameId);
        }

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {

        }


        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    private SimpleExoPlayer getPlayer(Context context) {
        mPlayer = ExoPlayerFactory.newSimpleInstance(context,
                new DefaultRenderersFactory(context),
                new DefaultTrackSelector(), new DefaultLoadControl());
        return mPlayer;
    }

    private PlayerView getPlayerView(Context context) {
        if (mPlayerView == null) {
            try {
                mPlayerView = new PlayerView(context);
                mPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                mPlayerView.setUseController(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "Good network quality");
        return mPlayerView;
    }


    public class SuperPrimeStreamersViewHolder extends BaseViewHolder implements SuperPrimeStreamersAdapter.SuperPrimeClickInterface {

        ItemSuperStreamersCarouselBinding mBinding;
        Context mContext;

        public SuperPrimeStreamersViewHolder(Context context, ItemSuperStreamersCarouselBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            this.mContext = context;
        }

        @Override
        public void onBind(int position) {
            final FeedObject result = mPostList.get(position);
            if (result.getTitle() != null) {
                mBinding.headerTitle.setVisibility(View.VISIBLE);
                mBinding.title.setText(result.getTitle());
            } else {
                mBinding.headerTitle.setVisibility(View.GONE);
            }
            mBinding.categoryTitleSeeAll.setVisibility(View.GONE);
            mBinding.seeAllIcon.setVisibility(View.GONE);
            SuperPrimeStreamersAdapter adapter = new SuperPrimeStreamersAdapter(result.getPosts(), result.getTitle());
            adapter.setListener(this);
            ViewGroup.LayoutParams params = mBinding.viewpager.getLayoutParams();
            //params.width = superPrimeStreamerCardWidth;
            params.height = superPrimeStreamerCardHeight;
            mBinding.viewpager.setLayoutParams(params);
            mBinding.viewpager.setAdapter(adapter);
            mBinding.viewpager.setPageMargin((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, mContext.getResources().getDisplayMetrics()));
        }

        @Override
        public void onRemindMeClick(PostObject result) {
            mListener.onSuperPrimeReminderListener(result);
        }

        @Override

        public void onPostItemClick(String id, List<PostObject> results, PostObject postObject, String title) {
            mListener.onCarouselItemClicked(id, results, postObject, title);

        }
    }


    public class BlogViewHolder extends BaseViewHolder implements PostItemViewModel.BlogItemViewModelListener {

        private ItemPostViewBinding mBinding;

        private PostItemViewModel mBlogItemViewModel;

        public BlogViewHolder(ItemPostViewBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            final FeedObject result = mPostList.get(position);
            mBlogItemViewModel = new PostItemViewModel(result.getPost(), this);
            BindingUtils.setImageUrlUsingCache(mBinding.videoThumbnail, result.getPost().getThumbnail(), true);
            if (result.getPost().getAuthor() != null)
                BindingUtils.setProfileImageUrlFromCache(mBinding.userProfilePic, result.getPost().getAuthor().getProfilePic(), true);
            View moreOptionsLayout = mBinding.moreOptionsLayout;

            mBinding.setViewModel(mBlogItemViewModel);
            mBinding.parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (moreOptionsLayout.isShown()) {
                        hideMoreOptionsLayout(moreOptionsLayout);
                    } else {
                        showMoreOptionsLayout(moreOptionsLayout);
                    }
                    return true;
                }
            });

            mBinding.userProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onAuthorClicked(result.getPost().getAuthor().getUser().getUsername());
                }
            });
            moreOptionsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });
            mBinding.moreOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (moreOptionsLayout.isShown()) {
                        hideMoreOptionsLayout(moreOptionsLayout);
                    } else {
                        showMoreOptionsLayout(moreOptionsLayout);
                    }
                }
            });


            moreOptionsLayout.findViewById(R.id.delete_btn).setVisibility(View.GONE);
            moreOptionsLayout.findViewById(R.id.download_btn).setVisibility(View.GONE);
            moreOptionsLayout.findViewById(R.id.follow_btn).setVisibility(View.GONE);
            moreOptionsLayout.findViewById(R.id.share_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onShareButtonClicked(result.getPost());
                    hideMoreOptionsLayout(moreOptionsLayout);

                }
            });

            moreOptionsLayout.findViewById(R.id.report_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onMoreOptionsBtnClick(result.getPost().getId());
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });
            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();
        }

        @Override
        public void onItemClick(String id, PostObject post) {
            sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_CARD_TYPE, "normal_post");
            mListener.onItemClick(id, post);
        }

        @Override
        public void onLikeButtonClicked(String postId, Result post) {
            mListener.onLikeButtonClicked(postId, post);
        }

        @Override
        public void onShareButtonClicked(String postId, PostObject post) {
            mListener.onShareButtonClicked(post);
        }

        @Override
        public void onAuthorClicked(String authorUserName) {
            mListener.onAuthorClicked(authorUserName);
        }

        @Override
        public void onSeeMoreClicked() {

        }

        @Override
        public void onMoreOptionsClicked(String postId) {
            //mListener.onMoreOptionsBtnClick(postId);
        }

        @Override
        public void onGameClicked(String game, String gameId) {
            mListener.onGameClicked(game, gameId);
        }

        @Override
        public void onDeleteVideoClicked(String postId, int itemPos) {

        }

        @Override
        public void onDownloadVideoClicked(String postId, int itemPos) {

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

   /* public class AlertCardViewHolder extends BaseViewHolder implements AlertItemViewModel.AlertItemViewModelListener {
        ItemAlertViewBinding mBinding;

        AlertItemViewModel alertItemViewModel;

        public AlertCardViewHolder(ItemAlertViewBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onCardClicked(Result result) {
            ListHolder.getInstance().setAlertInfoObject(result);
            mListener.onAlertCardClicked();
        }

        @Override
        public void onBind(int position) {
           *//* Result result = mPostList.get(position);
            AlertItemViewModel model = new AlertItemViewModel(this);
            model.setData(result);
            mBinding.setViewModel(model);*//*
        }
    }*/

    public class FooterLoadingViewHolder extends BaseViewHolder {

        public FooterLoadingViewHolder(FooterLoadingLayoutBinding binding) {
            super(binding.getRoot());
        }

        @Override
        public void onBind(int position) {

        }
    }


    public class TopStreamersViewHolder extends BaseViewHolder implements TopStreamersListAdapter.OnTopStreamerItemClickListener {
        ItemTopStreamersCardBinding mBinding;
        boolean isItemDecoratorAdded = false;

        public TopStreamersViewHolder(ItemTopStreamersCardBinding itemTopStreamersCardBinding) {
            super(itemTopStreamersCardBinding.getRoot());
            mBinding = itemTopStreamersCardBinding;
        }

        @Override
        public void onBind(int position) {
            FeedObject result = mPostList.get(position);
            mBinding.title.setText(result.getTitle());
            mBinding.streamersRv.setLayoutManager(new LinearLayoutManager(mBinding.streamersRv.getContext(), LinearLayoutManager.HORIZONTAL, false));
            if (!isItemDecoratorAdded) {
                isItemDecoratorAdded = true;
                TopStreamerItemDecorator itemDecoration = new TopStreamerItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mBinding.streamersRv.getContext().getResources().getDisplayMetrics()));
                mBinding.streamersRv.addItemDecoration(itemDecoration);
            }
            TopStreamersListAdapter adapter = new TopStreamersListAdapter();
            mBinding.streamersRv.setAdapter(adapter);
            mBinding.streamersRv.setRecycledViewPool(recycledViewPool);
            adapter.setData(result.getStreamers());
            adapter.setListener(this);
        }

        @Override
        public void onFollowBtnClicked(StreamerObject streamerObject, boolean isFollowed, OnFollowActionCompleteListener listener) {
            mListener.onFollowBtnClicked(streamerObject.getUsername(), streamerObject.getId(), isFollowed, listener);
        }

        @Override
        public void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener) {
            mListener.onProfileViewAction(authorUsername, listener);
        }
    }

    public class TopGamesViewHolder extends BaseViewHolder implements TopGamesGridAdapter.OnTopGamesCardClick {

        ItemPostTopGamesLayoutBinding mBinding;

        boolean isItemDecorationAdded;

        public TopGamesViewHolder(ItemPostTopGamesLayoutBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            FeedObject result = mPostList.get(position);
            mBinding.title.setVisibility(View.VISIBLE);
            mBinding.title1.setVisibility(View.GONE);
            mBinding.title.setText(result.getTitle());
            LinearLayoutManager layoutManager = new LinearLayoutManager(mBinding.topGamesRv.getContext(), GridLayoutManager.HORIZONTAL, false);
            mBinding.topGamesRv.setLayoutManager(layoutManager);

            TopGamesRvItemDecorator itemDecorator = new TopGamesRvItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mBinding.topGamesRv.getContext().getResources().getDisplayMetrics()));
            if (!isItemDecorationAdded) {
                isItemDecorationAdded = true;
                mBinding.topGamesRv.addItemDecoration(itemDecorator);
            }
            TopGamesGridAdapter adapter = new TopGamesGridAdapter(result.getGames());
            adapter.setListener(this);
            mBinding.topGamesRv.setAdapter(adapter);


        }

        @Override
        public void onTopGameCardClick(String gameName, String gameId) {
            Properties properties = new Properties();
            properties.put("game", gameName);

            mListener.onGameClicked(gameName, gameId);
        }
    }

    public class EmptyViewHolder extends BaseViewHolder implements PostEmptyItemViewModel.PostEmptyItemViewModelListener {
        private ShimmerPostLayoutBinding mBinding;

        public EmptyViewHolder(ShimmerPostLayoutBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {

        }

        @Override
        public void onRetryClick() {
            mListener.onRetryClick();
        }
    }

    public boolean isUpdatingPlayer() {
        return isUpdatingPlayer;
    }

    @Override
    public void onViewRecycled(@NonNull BaseViewHolder holder) {
        if (holder.getItemViewType() == AppConstants.VIEW_TYPE_CAROUSEL) {
            if (((CarouselViewHolder) holder).mBinding.getRoot().getTag() == LIVE_VIDEO_TAG) {
                if (!isUpdatingPlayer) {
                    releasePlayer();
                } else {
                    isUpdatingPlayer = false;
                }
            }
        }
        super.onViewRecycled(holder);
    }

    public void releasePlayer() {
        if (mPlayer != null && mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        if (mPlayerView != null) {

            ViewGroup parent = (ViewGroup) mPlayerView.getParent();
            if (parent == null) {
                return;
            }
            int childIndex = parent.indexOfChild(mPlayerView);
            if (childIndex >= 0) {
                parent.removeViewAt(childIndex);
            }
        }
    }

    public class StoryViewHolder extends BaseViewHolder implements StoryAdapter.OnStoryInteractionListener {
        LayoutStoryContainerBinding binding;
        boolean isDecoratorAdded;

        public StoryViewHolder(LayoutStoryContainerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            Context context = binding.getRoot().getContext();
            if (storyAdapter == null)
                storyAdapter = new StoryAdapter();
            storyAdapter.submitList(mPostList.get(position).getStoryAuthors());
            storyAdapter.setListener(this);
            if (!isDecoratorAdded) {
                isDecoratorAdded = true;
                int spacingInPixels = context.getResources().getDimensionPixelSize(R.dimen.margin_16);
                binding.storyRv.addItemDecoration(new HorizontalSpacesItemDecoration(spacingInPixels));
            }

            binding.storyRv.setAdapter(storyAdapter);
            binding.storyRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager mLayoutManager = (LinearLayoutManager) binding.storyRv.getLayoutManager();

                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

                    // Load more if we have reach the end to the recyclerView
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        storyAdapter.showLoading(true);
                        storyInteractionListener.loadNextStory();
                    }
                }
            });

            binding.executePendingBindings();
        }

        @Override
        public void onStoryClicked(ProfileResult author, int position) {
            storyInteractionListener.onStoryClicked(author, position);
        }

        @Override
        public void onAddNewStoryClicked() {
            storyInteractionListener.onAddNewStoryClicked();
        }

        @Override
        public void loadNextStory() {
            storyInteractionListener.loadNextStory();
        }
    }
}