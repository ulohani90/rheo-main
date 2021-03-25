package com.rheotv.android.ui.activities.tabcontainer.profile.videos;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.databinding.ItemPostViewBinding;
import com.rheotv.android.databinding.ShimmerPostLayoutBinding;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostEmptyItemViewModel;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostItemViewModel;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;

public class VideoFragmentAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<PostObject> mPostList = new ArrayList<>();

    private PostListAdapter.BlogAdapterListener mListener;

    private boolean isLoading = true;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    boolean showLoadingView;

    boolean showingSelfVideos;

    int heightSingleItemCarousel;

    public void calculateHeightForCarouselItems(Context context) {
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        int width = outMetrics.widthPixels - (2 * (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, outMetrics));
        heightSingleItemCarousel = ((width * 9) / 16) + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, outMetrics);
    }


    public VideoFragmentAdapter(Context context, List<PostObject> postList) {
        addItemToList(postList);
        calculateHeightForCarouselItems(context);
    }

    @Override
    public int getItemCount() {
        if (mPostList != null && mPostList.size() > 0) {
            Log.d("POSTLISTADAPTER", mPostList.size() + " size ki list");
            return showLoadingView ? mPostList.size() + 1 : mPostList.size();
        } else {
            return 2;
        }
    }

    public void setShowingSelfVideos(boolean showingSelfVideos) {
        this.showingSelfVideos = showingSelfVideos;
    }

    public List<PostObject> getmPostList() {
        return mPostList;
    }

    @Override
    public int getItemViewType(int position) {
        if (mPostList != null && !mPostList.isEmpty()) {

            if (position == mPostList.size()) {
                return AppConstants.VIEW_TYPE_LOADING_FOOTER;
            }
            return AppConstants.VIEW_TYPE_NORMAL;
            /*if (mPostList.get(position).getType() == 0)

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
            } else if (mPostList.get(position).getType() == 10) {
                return AppConstants.VIEW_TYPE_UPCOMING_STREAM;
            } else {
                return AppConstants.VIEW_TYPE_NORMAL;
            }*/
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
                ViewGroup.LayoutParams params = blogViewBinding.parent.getLayoutParams();
                params.height = heightSingleItemCarousel;
                blogViewBinding.parent.setLayoutParams(params);
                return new VideoFragmentAdapter.BlogViewHolder(blogViewBinding);
            case AppConstants.VIEW_TYPE_LOADING_FOOTER:
                FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new FooterLoadingViewHolder(footerLoadingLayoutBinding);

            default:
                ShimmerPostLayoutBinding emptyBinding = ShimmerPostLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new VideoFragmentAdapter.EmptyViewHolder(emptyBinding);
        }
    }

    public void addItems(List<PostObject> postList) {
        this.isLoading = postList != null && postList.size() > 0;
        addItemToList(postList);
        notifyDataSetChanged();
    }

    private void addItemToList(List<PostObject> postList) {
        mPostList.addAll(AppUtilsKt.INSTANCE.getDistinctValue(postList, PostObject::getId));
    }

    public void clearItems() {
        mPostList.clear();
    }

    public void setListener(PostListAdapter.BlogAdapterListener listener) {
        this.mListener = listener;
    }

    public void setShowLoadingView(boolean showLoadingView) {
        this.showLoadingView = showLoadingView;
        notifyDataSetChanged();
    }

    public void removeItemAtPos(int position) {
        if (position >= mPostList.size()) return;
        mPostList.remove(position);
        notifyItemRemoved(position);
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
            final PostObject result = mPostList.get(position);
            mBlogItemViewModel = new PostItemViewModel(result, this);
            mBlogItemViewModel.setItemPosition(position);

            BindingUtils.setImageUrlUsingCache(mBinding.videoThumbnail, result.getThumbnail(), true);
            if (result.getAuthor() != null && result.getAuthor().getProfilePic() != null)
                BindingUtils.setProfileImageUrlFromCache(mBinding.userProfilePic, result.getAuthor().getProfilePic(), true);
            View moreOptionsLayout = mBinding.moreOptionsLayout;

            mBinding.setViewModel(mBlogItemViewModel);

            View followLayout = mBinding.moreOptionsLayout.findViewById(R.id.follow_btn);
            View deleteLayout = mBinding.moreOptionsLayout.findViewById(R.id.delete_btn);
            View downloadLayout = mBinding.moreOptionsLayout.findViewById(R.id.download_btn);
            View reportLayout = mBinding.moreOptionsLayout.findViewById(R.id.report_btn);
            View shareLayout = mBinding.moreOptionsLayout.findViewById(R.id.share_btn);
            if (showingSelfVideos) {
                followLayout.setVisibility(View.GONE);
                deleteLayout.setVisibility(View.VISIBLE);
                reportLayout.setVisibility(View.GONE);
                if (result.isCanDownloadVideo()) {
                    downloadLayout.setVisibility(View.VISIBLE);
                } else {
                    downloadLayout.setVisibility(View.GONE);
                }
                shareLayout.setVisibility(View.VISIBLE);
            } else {
                followLayout.setVisibility(View.GONE);
                deleteLayout.setVisibility(View.GONE);
                reportLayout.setVisibility(View.VISIBLE);
                downloadLayout.setVisibility(View.GONE);
                shareLayout.setVisibility(View.VISIBLE);
            }

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

            mBinding.userProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onAuthorClicked(result.getAuthor().getUser().getUsername());
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
            followLayout.setOnClickListener(new View.OnClickListener() {
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
            shareLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onShareButtonClicked(result);
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });

            reportLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onMoreOptionsBtnClick(result.getId());
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });

            downloadLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onDownloadVideoClicked(result.getId(), position);
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });

            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onDeleteVideoClicked(result.getId(), position);
                    hideMoreOptionsLayout(moreOptionsLayout);
                }
            });

            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();
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
            mListener.onDeleteVideoClicked(postId, itemPos);
        }

        @Override
        public void onDownloadVideoClicked(String postId, int itemPos) {
            mListener.onDownloadVideoClicked(postId, itemPos);
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

    public class FooterLoadingViewHolder extends BaseViewHolder {

        public FooterLoadingViewHolder(FooterLoadingLayoutBinding binding) {
            super(binding.getRoot());
        }

        @Override
        public void onBind(int position) {

        }
    }
}