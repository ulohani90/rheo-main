package com.rheotv.android.ui.activities.player.activity;

import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;

import java.util.List;

public interface ChatNavigator {

    void addItemsInChat(String postId, List<CommentChat> commentChats);

    void showReportPostSuccessToast();

    void onBlockUserSuccess();

//    void onStickersLoadComplete(List<Sticker> stickers);

    void onHeartUpdate(int count);

    void showDeleteSuccessToast();
}
