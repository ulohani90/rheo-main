package com.rheotv.android.ui.activities.player.activity;


import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.rheotv.android.data.network.models.postlisting.responses.Result;

public class PlayerItemViewModel {

    public final ObservableField<String> author;

    public final ObservableField<String> content;

    public final ObservableField<String> date;

    public final ObservableField<String> imageUrl;

    public final ObservableField<String> authorProfileimageUrl;

    public final ObservableField<String> title;

    public final ObservableField<String> totalLikes;

    public final ObservableField<String> totalViews;

    public final ObservableField<String> totalShares;

    public final ObservableField<String> totalDuration;

    public final ObservableBoolean isLiked;

    public final ObservableBoolean isPlaying;

    private final PlayerItemViewModelListener mBlogitemViewModelListener;

    private final Result mListItem;

    private final PlayerItemViewModelListener mPlayerItemViewModelListener;

    public PlayerItemViewModel(Result mListItem, PlayerItemViewModelListener listener) {
        this.mListItem = mListItem;
        this.mBlogitemViewModelListener = listener;
        this.mPlayerItemViewModelListener = null;

        if (mListItem.getAuthor() != null) {
            author = new ObservableField<>(mListItem.getAuthor().getUser().getFirstName());
            authorProfileimageUrl = new ObservableField<>((mListItem.getAuthor().getProfilePic()));
        } else {
            author = new ObservableField<>("Mojo Times");
            authorProfileimageUrl = new ObservableField<>("");
        }

        imageUrl = new ObservableField<>(mListItem.getThumbnail());
        title = new ObservableField<>(mListItem.getTitle());
        date = new ObservableField<>(mListItem.getoFormattedCreatedAt());
        content = new ObservableField<>(mListItem.getDescription());


        totalLikes = new ObservableField<>(mListItem.getTotalLikes());

        int totalNumViews = mListItem.getTotalViews();
        totalViews = new ObservableField<>((totalNumViews / 1000 >= 1) ? (totalNumViews / 1000) + "." + ((totalNumViews % 1000) / 100) + "K" : totalNumViews + "" + "");


        totalShares = new ObservableField<>(mListItem.getTotalShares());
        totalDuration = new ObservableField<>(mListItem.getFormattedDuration(String.valueOf((int) mListItem.getDuration())));
        isLiked = new ObservableBoolean(mListItem.getIsLiked());
        isPlaying = new ObservableBoolean(mListItem.isPlaying());
    }

    public void onItemClick() {
        mBlogitemViewModelListener.onItemClick(mListItem.getId());
    }

    public interface PlayerItemViewModelListener {
        void onItemClick(String id);
    }

}
