package com.rheotv.android.ui.activities.player.activity;

import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.segment.analytics.Properties;

import java.util.HashMap;
import java.util.List;

public interface PlayerNavigator {
    void playVideo(String postId);

    void playNext(String postId);

    void openPlayList();

    void onExitClicked();

    void onAuthorClicked(String authorUserName);

    void setDeepLinkPost(List<Result> list);

    void handleChat();

    void handleShareClick(Result post);

    void handleExpandCollapse(boolean expand);

    void setCurrentPlayingPost(Result post);

    void openLoginFlow();

    void addItemsInChat(String postId, List<CommentChat> commentChats);

    void settingsClicked();

    void showToast(String message);

    void onMoreOptionsClicked();

    void showReportPostSuccessToast();

    void onBlockUserSuccess();

    HashMap<String, Object> getProperties();

    void openGamePage(CharSequence text);

//    void openScratchCard(String coins);

    void onStickersLoadComplete(List<Sticker> stickers);

    void checkRewardAvailable();

    Result getCurrentPlayingPost();

    void updateFollowStatus(boolean isFollowed);
}
