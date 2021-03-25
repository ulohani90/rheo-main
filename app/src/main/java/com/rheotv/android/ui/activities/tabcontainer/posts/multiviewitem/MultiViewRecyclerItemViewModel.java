package com.rheotv.android.ui.activities.tabcontainer.posts.multiviewitem;


import androidx.databinding.ObservableField;

import com.rheotv.android.data.network.models.objects.PostObject;

public class MultiViewRecyclerItemViewModel {

    public ObservableField<String> imageUrl;
    public ObservableField<String> title;
    public ObservableField<String> totalViews;
    public ObservableField<String> author;
    public ObservableField<String> date;
    public ObservableField<String> totalShares;
    public ObservableField<String> totalFacebookShares;
    public ObservableField<Boolean> is_live;
    public ObservableField<String> userProfilePic;
    public ObservableField<String> game;
    public ObservableField<String> duration;

    private PostObject mListItem;
    private MultiViewItemViewModelListener listener;

    public MultiViewRecyclerItemViewModel(PostObject mListItem, MultiViewItemViewModelListener listener) {
        this.mListItem = mListItem;
        this.listener = listener;

        if (mListItem != null) {
            title = new ObservableField<>(capitalize(mListItem.getTitle()));
        } else {
            title = new ObservableField<>("");
        }

        if (mListItem.getGame() != null) {
            game = new ObservableField<>(mListItem.getGame().getName());
        } else {
            game = new ObservableField<>("");
        }

        if (mListItem.getAuthor() != null & mListItem.getAuthor().getProfilePic() != null) {
            userProfilePic = new ObservableField<>(mListItem.getAuthor().getProfilePic());
        } else {
            userProfilePic = new ObservableField<>("");
        }

        if (mListItem.getThumbnail() != null) {
            imageUrl = new ObservableField<>(mListItem.getThumbnail());
        } else {
            imageUrl = new ObservableField<>("");
        }

        if (mListItem.getTotalViews() != 0) {
            int totalNumViews = mListItem.getTotalViews();
            totalViews = new ObservableField<>((totalNumViews / 1000 >= 1) ? (totalNumViews / 1000) + "." + ((totalNumViews % 1000) / 100) + "K" : totalNumViews + "" + "");
        } else {
            totalViews = new ObservableField<>("");
        }

        //duration = new ObservableField<>(mListItem.getFormattedDuration(String.valueOf((int) mListItem.getDuration())));

        if (mListItem.getAuthor() != null) {
            if (mListItem.getAuthor().getUser() != null) {
                if (mListItem.getAuthor().getUser().getUsername() != null) {
                    author = new ObservableField<>(capitalize(mListItem.getAuthor().getUser().getUsername()));
                } else {
                    author = new ObservableField<>("");
                }
            } else {
                author = new ObservableField<>("");
            }
        } else {
            author = new ObservableField<>("");
        }

        //date = new ObservableField<>(mListItem.getoFormattedCreatedAt());

        /*if (mListItem.getTotalShares() != null) {
            totalShares = new ObservableField<>(mListItem.getTotalShares());
        } else {
            totalShares = new ObservableField<>("");
        }

        if (mListItem.getTotalFacebookShares() != null) {
            totalFacebookShares = new ObservableField<>(mListItem.getTotalFacebookShares());
        } else {
            totalFacebookShares = new ObservableField<>("");
        }
        if (mListItem.getIsLive()) {
            is_live = new ObservableField<>(mListItem.getIsLive());
        }*/

    }

    public void onAuthtorClciked() {
        listener.onAuthodClicked(mListItem.getAuthor().getUser().getUsername());
    }

    public ObservableField<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(ObservableField<String> imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ObservableField<String> getTitle() {
        return title;
    }

    public void setTitle(ObservableField<String> title) {
        this.title = title;
    }

    public interface MultiViewItemViewModelListener {
        void onItemClick(String id);

        void onAuthodClicked(String id);

        void onSeeMoreClicked();
    }

    public void onItemClick() {
        //TODO: Implement click listener
        listener.onItemClick(mListItem.getId());
    }

    // todo - move to utils
    private static String capitalize(String str) {
        if (str != null && str.length() > 1) {
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
        return str;
    }
}
