package com.rheotv.android.ui.activities.universalActivity.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.exoplayer2.ui.PlayerView;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.FeedObject;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.objects.StreamerObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.ItemCarouselViewBinding;
import com.rheotv.android.databinding.ItemJobViewBinding;
import com.rheotv.android.databinding.ItemMultiViewBinding;
import com.rheotv.android.databinding.ItemPostEmptyBinding;
import com.rheotv.android.databinding.ItemPostViewBinding;
import com.rheotv.android.databinding.ItemTopStreamersCardBinding;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostEmptyItemViewModel;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostItemViewModel;
import com.rheotv.android.ui.activities.tabcontainer.posts.multiviewitem.MultiViewRecyclerAdapter;
import com.rheotv.android.ui.adapters.CarouselAdapter;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.adapters.TopStreamersListAdapter;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.ui.decorators.TopStreamerItemDecorator;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.SharedPrefsUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UniversalFragmentListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<FeedObject> mPostList;

    private PostListAdapter.BlogAdapterListener mListener;

    private boolean isLoading = true;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();


    public UniversalFragmentListAdapter(List<FeedObject> mPostList) {
        this.mPostList = mPostList;
    }

    @Override
    public int getItemCount() {
        if (mPostList != null && mPostList.size() > 0) {
            Log.d("POSTLISTADAPTER", mPostList.size() + " size ki list");
            return mPostList.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mPostList != null && !mPostList.isEmpty()) {
            if (mPostList.get(position).getType() == 0)
                return AppConstants.VIEW_TYPE_NORMAL;
            else if (mPostList.get(position).getType() == 3) {
                return AppConstants.VIEW_TYPE_JOB;
            } else if (mPostList.get(position).getType() == 2) {
                return AppConstants.VIEW_TYPE_CAROUSEL;
            } else if (mPostList.get(position).getType() == 4) {
                return AppConstants.VIEW_TYPE_MULTI_ITEM_CARD;
            } else if (mPostList.get(position).getType() == 5) {
                return AppConstants.VIEW_TYPE_LEADERBOARD;
            } else if (mPostList.get(position).getType() == 6) {
                return AppConstants.VIEW_TYPE_INVOICE_CARD;
            } else if (mPostList.get(position).getType() == 7) {
                return AppConstants.VIEW_TOP_STREAMERS;
            } else {
                return AppConstants.VIEW_TYPE_NORMAL;
            }
        } else {
            return AppConstants.VIEW_TYPE_EMPTY;
        }
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
                return new UniversalFragmentListAdapter.BlogViewHolder(blogViewBinding);

            case AppConstants.VIEW_TYPE_JOB:
                ItemJobViewBinding jobViewBinding = ItemJobViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new UniversalFragmentListAdapter.JobViewHolder(jobViewBinding);

            case AppConstants.VIEW_TYPE_CAROUSEL:
                ItemCarouselViewBinding carouselViewBinding = ItemCarouselViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);

                return new UniversalFragmentListAdapter.CarouselViewHolder(carouselViewBinding);

            case AppConstants.VIEW_TYPE_EMPTY:
                ItemPostEmptyBinding emptyViewBinding = ItemPostEmptyBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new UniversalFragmentListAdapter.EmptyViewHolder(emptyViewBinding);

            case AppConstants.VIEW_TYPE_MULTI_ITEM_CARD:
                ItemMultiViewBinding itemMultiViewBinding = ItemMultiViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new UniversalFragmentListAdapter.ItemMultiViewHolder(itemMultiViewBinding, parent.getContext());

            case AppConstants.VIEW_TOP_STREAMERS:
                ItemTopStreamersCardBinding itemTopStreamersCardBinding = ItemTopStreamersCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new TopStreamersViewHolder(itemTopStreamersCardBinding);
            default:
                ItemPostEmptyBinding emptyBinding = ItemPostEmptyBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new UniversalFragmentListAdapter.EmptyViewHolder(emptyBinding);
        }
    }

    public void addItems(List<FeedObject> mPostList) {
        this.isLoading = mPostList != null && mPostList.size() > 0;
        this.mPostList.addAll(mPostList);
        notifyDataSetChanged();
    }

    public void clearItems() {
        mPostList.clear();
    }

    public void setListener(PostListAdapter.BlogAdapterListener listener) {
        this.mListener = listener;
    }

    public class JobViewHolder extends BaseViewHolder implements PostItemViewModel.BlogItemViewModelListener {
        private ItemJobViewBinding mBinding;

        private PostItemViewModel mBlogItemViewModel;

        public JobViewHolder(ItemJobViewBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            /*FeedObject result = mPostList.get(position);
            mBlogItemViewModel = new PostItemViewModel(result, this);
            mBinding.setViewModel(mBlogItemViewModel);

            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();*/
        }

        @Override
        public void onItemClick(String id, PostObject post) {
            mListener.onItemClick(id, post);
        }

        @Override
        public void onLikeButtonClicked(String postId, Result post) {
            mListener.onLikeButtonClicked(postId, post);
        }

        @Override
        public void onShareButtonClicked(String postId, PostObject post) {
            //do nothing
        }

        @Override
        public void onAuthorClicked(String authorUserName) {
            //do nothing
        }


        @Override
        public void onSeeMoreClicked() {

        }

        @Override
        public void onMoreOptionsClicked(String postId) {

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

    public class ItemMultiViewHolder extends BaseViewHolder implements PostItemViewModel.BlogItemViewModelListener, MultiViewRecyclerAdapter.MultiViewAdapterListener {

        private ItemMultiViewBinding mBinding;
        private PostItemViewModel mBlogItemViewModel;
        private MultiViewRecyclerAdapter multiViewRecyclerAdapter;
        private Context context;
        private List<PostObject> multiViewList;

        public ItemMultiViewHolder(ItemMultiViewBinding mBinding, Context context) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            this.context = context;
        }


        @Override
        public void onBind(int position) {
            /*FeedObject result = mPostList.get(position);
            multiViewList = mPostList.get(position).getPosts();
            mBlogItemViewModel = new PostItemViewModel(result, this);
            mBinding.setViewModel(mBlogItemViewModel);

            mBinding.categoryTitle.setText(result.getTitle());

            multiViewRecyclerAdapter = new MultiViewRecyclerAdapter(multiViewList);
            multiViewRecyclerAdapter.setListener(this);
            mBinding.multiItemRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            mBinding.multiItemRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mBinding.multiItemRecyclerView.setAdapter(multiViewRecyclerAdapter);*/


        }

        @Override
        public void onItemClick(String id, PostObject post) {
            mListener.onItemClick(id, post);
        }

        @Override
        public void onLikeButtonClicked(String postId, Result post) {

        }

        @Override
        public void onShareButtonClicked(String postId, PostObject post) {

        }

        @Override
        public void onAuthorClicked(String userName) {
            mListener.onAuthorClicked(userName);
        }


        @Override
        public void onSeeMoreClicked() {
            mListener.onSeeMoreClicked(multiViewList);
        }

        @Override
        public void onMoreOptionsClicked(String postId) {

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

        @Override
        public void onMultiViewItemClick(String id) {
            sharedPrefsUtils.setStringPreference(RheoTvApp.getNonUiContext(), SharedPrefsUtils.HOME_CARD_TYPE, "multi_item_post");
            mListener.onMultiViewItemClicked(id, multiViewList);
        }
    }

    public class CarouselViewHolder extends BaseViewHolder implements PostItemViewModel.BlogItemViewModelListener, CarouselAdapter.CarouselItemClickListener, ViewPager.OnPageChangeListener {
        private ItemCarouselViewBinding mBinding;

        private PostItemViewModel mBlogItemViewModel;
        private Timer timer;
        private TimerTask timerTask;
        private ViewPager viewPager;
        private CarouselAdapter adapter;

        public CarouselViewHolder(ItemCarouselViewBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
            timer = new Timer();
        }

        @Override
        public void onBind(int position) {
            final FeedObject result = mPostList.get(position);
           /* viewPager = mBinding.viewpager;
            adapter = new CarouselAdapter(new ArrayList<>());
            adapter.setListener(this);
            viewPager.setAdapter(adapter);
            viewPager.setOnPageChangeListener(this);*/
//            autoScrollCarousel();

            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();
        }


        @Override
        public void onItemClick(String id, PostObject post) {
            mListener.onItemClick(id, post);
        }

        @Override
        public void onLikeButtonClicked(String postId, Result post) {

        }

        @Override
        public void onShareButtonClicked(String postId, PostObject post) {
        }

        @Override
        public void onAuthorClicked(String authorUserName) {
            //do nothing
        }

        @Override
        public void onSeeAllClicked(String gameId, String gameTitle) {

        }

        @Override
        public void onSeeMoreClicked() {

        }

        @Override
        public void onMoreOptionsClicked(String postId) {

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

        }

        @Override
        public void onShareButtonClicked(PostObject post) {
            mListener.onShareButtonClicked(post);
        }


        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            if (i > 0) {
                ((PlayerView) viewPager.getChildAt(0).findViewById(R.id.video_view)).getPlayer().setPlayWhenReady(false);
            } else {
                ((PlayerView) viewPager.getChildAt(0).findViewById(R.id.video_view)).getPlayer().setPlayWhenReady(true);
            }
        }


        @Override
        public void onPageScrollStateChanged(int i) {

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
            FeedObject result = mPostList.get(position);
            mBlogItemViewModel = new PostItemViewModel(result.getPost(), this);
            BindingUtils.setImageUrlUsingCache(mBinding.videoThumbnail, result.getPost().getThumbnail(), true);
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
            ImageView followImage = (ImageView) moreOptionsLayout.findViewById(R.id.follow_img);
            TextView followText = (TextView) moreOptionsLayout.findViewById(R.id.follow_text);
            /*if (result.isFollowed()) {
                followImage.setImageResource(R.drawable.ic_added_user);
                followText.setText("Following");
                followImage.setBackgroundResource(R.drawable.circle_grey_bg);
            } else {
                followImage.setImageResource(R.drawable.ic_add_user);
                followText.setText("Follow");
                followImage.setBackgroundResource(R.drawable.circle_white_bg);
            }
            moreOptionsLayout.findViewById(R.id.follow_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onFollowBtnClicked(result.getAuthor().getUser().getId(), result.isFollowed(), new OnFollowActionCompleteListener() {

                        @Override
                        public void onFollowActionComplete(boolean isFollowed) {
                            if (!isFollowed) {
                                followImage.setImageResource(R.drawable.ic_add_user);
                                followText.setText("Follow");
                                followImage.setBackgroundResource(R.drawable.circle_white_bg);
                            } else {
                                followImage.setImageResource(R.drawable.ic_added_user);
                                followText.setText("Following");
                                followImage.setBackgroundResource(R.drawable.circle_grey_bg);
                            }
                            result.setFollowed(isFollowed);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    hideMoreOptionsLayout(moreOptionsLayout);
                                }
                            }, 1000);
                        }
                    });
                }
            });*/
            moreOptionsLayout.findViewById(R.id.delete_btn).setVisibility(View.GONE);
            moreOptionsLayout.findViewById(R.id.download_btn).setVisibility(View.GONE);
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
            mListener.onMoreOptionsBtnClick(postId);
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

    }

    public class EmptyViewHolder extends BaseViewHolder implements PostEmptyItemViewModel.PostEmptyItemViewModelListener {
        private ItemPostEmptyBinding mBinding;

        public EmptyViewHolder(ItemPostEmptyBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            PostEmptyItemViewModel emptyItemViewModel = new PostEmptyItemViewModel(this);
            if (isLoading) {
                mBinding.linearLayoutView.setVisibility(View.GONE);
            } else {
                mBinding.linearLayoutView.setVisibility(View.VISIBLE);
            }
            mBinding.setViewModel(emptyItemViewModel);
        }

        @Override
        public void onRetryClick() {
            mListener.onRetryClick();
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
            adapter.setData(result.getStreamers());
            adapter.setListener(this);
        }

        @Override
        public void onFollowBtnClicked(StreamerObject streamerObject, boolean isFollowed, OnFollowActionCompleteListener listener) {
            mListener.onFollowBtnClicked(streamerObject.getUsername(),streamerObject.getId(), isFollowed, listener);
        }

        @Override
        public void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener) {
            mListener.onProfileViewAction(authorUsername, listener);
        }
    }
}