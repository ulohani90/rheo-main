/*
 * *
 *  * Created by Asheesh Sharma on 1st January 2019.
 *  * Copyright (c) January 2019 . All rights reserved.
 *  * Last modified 03/01/19 01:16 AM
 *
 */

package com.rheotv.android.ui.activities.tabcontainer.posts;

import android.text.SpannableString;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;


public class PostItemViewModel {

    public ObservableField<String> author;

    public ObservableField<SpannableString> game;

    public ObservableField<String> gameName = new ObservableField<>();

    public ObservableField<String> postTitle = new ObservableField<>("");

    public ObservableField<String> language = new ObservableField<>();


    public ObservableField<String> imageUrl;

    //public ObservableField<String> shareUrl;

    public ObservableField<String> authorProfileimageUrl;

    private BlogItemViewModelListener mBlogitemViewModelListener;

    public ObservableField<String> title;

    public ObservableField<String> totalViews;

    public ObservableField<String> totalWatching;

    public ObservableField<String> totalDuration;


    public ObservableBoolean showMoreOptions;

    public ObservableBoolean isLive;

    private PostObject mListItem;

    private int itemPosition;

    public ObservableBoolean isSingleItem = new ObservableBoolean(false);

    public ObservableField<Boolean> showTotalView = new ObservableField<>(false);

    SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    public PostItemViewModel(PostObject mListItem, BlogItemViewModelListener listener) {
        if (mListItem == null) return;
        this.mListItem = mListItem;
        this.mBlogitemViewModelListener = listener;
        showMoreOptions = new ObservableBoolean(false);
        if (mListItem.getAuthor() != null) {
            if (mListItem.getAuthor().getUser() != null) {
                if (mListItem.getAuthor().getUser().getUsername() != null) {
                    author = new ObservableField<>(mListItem.getAuthor().getUser().getUsername());
                } else {
                    author = new ObservableField<>("");
                }
                if (mListItem.getAuthor().getProfilePic() != null) {
                    authorProfileimageUrl = new ObservableField<>((mListItem.getAuthor().getProfilePic()));
                } else {
                    authorProfileimageUrl = null;
                }
            } else {
                author = new ObservableField<>("");
            }
        } else {
            author = new ObservableField<>("");
        }
        if (mListItem.getThumbnail() != null) {
            imageUrl = new ObservableField<>(mListItem.getThumbnail());
        } else {
            imageUrl = null;
        }

        language.set(mListItem.getLanguage());

        postTitle.set(mListItem.getTitle());

        if (mListItem.getGame() != null) {
//            SpannableString gameString = new SpannableString((mListItem.isLive() ? "Streaming " : "Streamed ") + mListItem.getGame().getName());
//            gameString.setSpan(new ForegroundColorSpan(Color.parseColor("#aeaeb2")), 0, mListItem.isLive() ? 9 : 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            game = new ObservableField<>(gameString);
            gameName.set(mListItem.getGame().getName());
        } else {
            game = null;
        }


       /* if (mListItem.getShareUrl() != null) {
            shareUrl = new ObservableField<>(mListItem.getShareUrl());
        } else {
            shareUrl = null;
        }*/

        if (mListItem.getTitle() != null) {
            title = new ObservableField<>(mListItem.getTitle());
        } else {
            title = new ObservableField<>("");
        }

        /*date = new ObservableField<>(mListItem.getoFormattedCreatedAt());
        if (mListItem.getDescription() != null) {
            content = new ObservableField<>(mListItem.getDescription());
        } else {
            content = new ObservableField<>("");
        }
        if (mListItem.getTotalLikes() != null) {
            totalLikes = new ObservableField<>(mListItem.getTotalLikes());
        } else {
            totalLikes = new ObservableField<>("");
        }

        if (mListItem.getTotalDownloads() != null) {
            totalDownloads = new ObservableField<>(mListItem.getTotalDownloads());
        } else {
            totalDownloads = new ObservableField<>("");
        }

        if (mListItem.getTotalFacebookShares() != null) {
            totalFacebookShares = new ObservableField<>(mListItem.getTotalFacebookShares());
        } else {
            totalFacebookShares = new ObservableField<>("");
        }*/

        if (mListItem.getWatchingCount() > 0) {
            int totalWatch = mListItem.getWatchingCount();
            totalWatching = new ObservableField<>(((totalWatch / 1000 >= 1) ? (totalWatch / 1000) + "." + ((totalWatch % 1000) / 100) + "K" : totalWatch) + " Watching");
            showTotalView.set(false);
        } else {
            if (mListItem.getTotalViews() != 0) {
                int totalNumViews = mListItem.getTotalViews();
                totalViews = new ObservableField<>((totalNumViews / 1000 >= 1) ? (totalNumViews / 1000) + "." + ((totalNumViews % 1000) / 100) + "K" : totalNumViews + "" + "");
            } else {
                totalViews = new ObservableField<>("1");
            }
            showTotalView.set(true);
        }

        /*if (mListItem.getTotalShares() != null) {
            totalShares = new ObservableField<>(mListItem.getTotalShares());
        } else {
            totalShares = new ObservableField<>("");
        }

        if (mListItem.getCategory() != null) {
            category = new ObservableField<>(mListItem.getCategory());
        } else {
            category = new ObservableField<>("");
        }

        totalDuration = new ObservableField<>(mListItem.getFormattedDuration(String.valueOf((int) mListItem.getDuration())));
        isLiked = new ObservableBoolean(mListItem.getIsLiked());*/
        isLive = new ObservableBoolean(mListItem.isLive());

       /* if (mListItem.getHashtags() != null) {
            if (!mListItem.getHashtags().isEmpty()) {
                tagsList = mListItem.getHashtags();
                Log.d("KKKK", tagsList.size() + "TAG SIZE SET");
            } else {
                tagsList = new ArrayList<>();
            }
        } else {
            tagsList = new ArrayList<>();

        }
        if (mListItem.getResults() != null) {
            if (!mListItem.getResults().isEmpty()) {
                results = mListItem.getResults();
                if (results.size() == 1) {
                    isSingleItem.set(true);
                }
            } else {
                results = new ArrayList<>();
            }
        } else {
            results = new ArrayList<>();
        }*/
    }

    public void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }

    public void onItemClick() {
        mBlogitemViewModelListener.onItemClick(mListItem.getId(), mListItem);
        recordVideoClick();
    }

    private void recordVideoClick() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("postId", mListItem.getId());
        properties.put("is_live", mListItem.isLive());
        properties.put("type", mListItem.isLive() ? "live" : "fullRecorded");
        properties.put("game_id", mListItem.getGame().getId());
        properties.put("title", mListItem.getTitle());
        properties.put("game_name", mListItem.getGame().getName());
        SegmentTracker.getInstance(getNonUiContext()).trackEvent(SegmentConstants.EVENT_VIDEO_CLICKED, properties
        );
    }

   /* public void onLikeButtonClicked() {
        if (!showMoreOptions.get()) {
            showMoreOptions.set(true);
        } else {
            showMoreOptions.set(false);
        }
        mBlogitemViewModelListener.onLikeButtonClicked(mListItem.getId(), mListItem);
    }*/

    public void onShareButtonClicked() {
        mBlogitemViewModelListener.onShareButtonClicked(mListItem.getId(), mListItem);
    }

    public void onAuthorClicked() {
        mBlogitemViewModelListener.onAuthorClicked(String.valueOf(mListItem.getAuthor().getUser().getUsername()));
    }

    public void onGameClicked() {
        mBlogitemViewModelListener.onGameClicked(String.valueOf(mListItem.getGame()), String.valueOf(mListItem.getGame().getId()));
    }

    public interface BlogItemViewModelListener {
        void onItemClick(String id, PostObject post);

        void onLikeButtonClicked(String postId, Result post);

        void onShareButtonClicked(String postId, PostObject post);

        void onAuthorClicked(String userName);

        void onSeeMoreClicked();

        void onMoreOptionsClicked(String postId);

        void onGameClicked(String game, String gameId);

        void onDeleteVideoClicked(String postId, int itemPos);

        void onDownloadVideoClicked(String postId, int itemPos);

    }

    public void onSeeMoreClicked() {
        //TODO: Implement see more listener
        mBlogitemViewModelListener.onSeeMoreClicked();
    }

    public void onMoreOptionsClicked() {
        mBlogitemViewModelListener.onMoreOptionsClicked(mListItem.getId());
    }

    public void onDeleteVideoClicked() {
        mBlogitemViewModelListener.onDeleteVideoClicked(mListItem.getId(), itemPosition);
    }

    public void onDownloadVideoClicked() {
        mBlogitemViewModelListener.onDownloadVideoClicked(mListItem.getId(), itemPosition);
    }

}
