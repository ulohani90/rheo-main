package com.rheotv.android.ui.activities.player.activity;


import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;

public class ChatItemViewModel {

    CommentChat chatNote;

    public ChatItemViewModel(CommentChat chatNote) {
        this.chatNote = chatNote;
    }

}
