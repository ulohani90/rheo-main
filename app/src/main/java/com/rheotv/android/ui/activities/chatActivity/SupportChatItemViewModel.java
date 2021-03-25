package com.rheotv.android.ui.activities.chatActivity;

import com.rheotv.android.data.network.models.postlisting.responses.support.ChatModel;

public class SupportChatItemViewModel {
    ChatModel chatNote;

    public SupportChatItemViewModel(ChatModel chatNote) {
        this.chatNote = chatNote;
    }
}
