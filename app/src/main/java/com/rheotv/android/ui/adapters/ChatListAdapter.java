package com.rheotv.android.ui.adapters;

import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.databinding.ChatItemV2Binding;
import com.rheotv.android.databinding.ChatItemV2MediaBinding;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.ui.activities.profile.viewprofile.utils.UserAction;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import static com.rheotv.android.utils.CommonUtils.getRandomNumberInRange;

public class ChatListAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<CommentChat> chatNoteList;
    ChatItemClickListenerV2 mListener;

    private boolean showLoading = false;
    private boolean isNewChatItem;
    private int chatStickerSize;
    private boolean isSelfStream;
    private boolean isWelcomeMessageShown = false;
    private int orientation;
    private boolean isWelcomeNoteShown;
    private boolean isNoMessageTextShown;

    public List<CommentChat> getList() {
        return chatNoteList;
    }

    int[] heartEmojis = {0x1F499, 0x1F49A, 0x1F49B, 0x1F49C};
    String[] heartEmojiText = {"Kya baat hai", "Wah ", "Mast "};

    public ChatListAdapter() {
        this.chatNoteList = new ArrayList<>();
        this.orientation = Configuration.ORIENTATION_PORTRAIT;
    }

    public ChatListAdapter(List<CommentChat> chatNoteList, int orientation, boolean isFirstpage, boolean isNoMessages) {
        this.chatNoteList = chatNoteList;
        this.orientation = orientation;
        if (isFirstpage) {
            this.chatNoteList.add(new CommentChat.WelcomeComment(
                    "Use of abusive language in chat can result in a permanent ban from the platform.",
                    "", 14f, Color.RED));
            isWelcomeMessageShown = true;
        } else if (isNoMessages) {
            this.chatNoteList.add(new CommentChat.WelcomeComment(
                    "No Messages.",
                    "", 14f, Color.RED));
            this.isNoMessageTextShown = true;
        }
    }


    public ChatListAdapter(List<CommentChat> chatNoteList, int orientation, boolean isFirstpage) {
        this.chatNoteList = chatNoteList;
        this.orientation = orientation;
        if (isFirstpage) {
            this.chatNoteList.add(new CommentChat.WelcomeComment(
                    "Use of abusive language in chat can result in a permanent ban from the platform.",
                    "", 14f, Color.RED));
            isWelcomeMessageShown = true;
        }
    }

    public void addInitialNote() {
        if (!isWelcomeMessageShown) {
            this.chatNoteList.add(new CommentChat.WelcomeComment(
                    "Use of abusive language in chat can result in a permanent ban from the platform.",
                    "", 14f, Color.RED));
            isWelcomeMessageShown = true;
        }
    }

    public void addWelcomeNote(String username) {
        if (isWelcomeNoteShown || isWelcomeMessageShown) return;
        this.chatNoteList.add(0, new CommentChat.WelcomeComment(
                RheoTvApp.getNonUiContext().getString(R.string.welcome_chat_message, username),
                "", 14f, Color.RED));
        isWelcomeNoteShown = true;
        notifyDataSetChanged();
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        notifyDataSetChanged();
    }

    public void setChatStickerSize(int chatStickerSize) {
        this.chatStickerSize = chatStickerSize;
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyDataSetChanged();
    }

    public boolean isShowLoading() {
        return showLoading;
    }

    public void setSelfStream(boolean selfStream) {
        isSelfStream = selfStream;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == chatNoteList.size()) return AppConstants.VIEW_TYPE_LOADING_FOOTER;
        if (chatNoteList.get(position).isMedia()) return AppConstants.VIEW_TYPE_CHAT_MEDIA;
        return AppConstants.VIEW_TYPE_CHAT_MESSAGE;
    }

    public void setListener(ChatItemClickListenerV2 mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AppConstants.VIEW_TYPE_LOADING_FOOTER) {
            FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
        }
        if (viewType == AppConstants.VIEW_TYPE_CHAT_MEDIA) {
            ChatItemV2MediaBinding bindingMedia = ChatItemV2MediaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ChatMediaViewHolder(bindingMedia);
        }

        ChatItemV2Binding chatItemBinding = ChatItemV2Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatListViewHolder(chatItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int i) {
        baseViewHolder.onBind(i);
    }

    public void submitItems(List<CommentChat> list) {
        if (list.size() == 1)
            addItem(list.get(0));
        else
            addItems(list);
    }

    public void addItems(List<CommentChat> chatNoteList) {
        if (chatNoteList == null) return;
        if (isWelcomeMessageShown && chatNoteList.size() == 9) {
            this.chatNoteList.clear();
            isWelcomeMessageShown = false;
        }
        if (isNoMessageTextShown) {
            this.chatNoteList.clear();
            isNoMessageTextShown = false;
        }
        showLoading = false;
        this.chatNoteList.addAll(chatNoteList);
        notifyDataSetChanged();
    }

    public void onUserAction(Pair<UserAction, CommentChat> pair) {
        if (pair.first == UserAction.Add.INSTANCE) {
            addItem(pair.second);
        } else {
            removeChatItem(pair.second.getMessage(), pair.second.getUsername());
        }
    }

    public void clearChatNoteList() {
        this.chatNoteList.clear();
    }

    public void addItem(CommentChat commentChat) {
        Log.i(getClass().getSimpleName(), "addItem " + commentChat.getMessage() + " & " + commentChat.getMessageType());
        isNewChatItem = true;
        if (isNoMessageTextShown) {
            this.chatNoteList.clear();
            isNoMessageTextShown = false;
        }
       /* if (isWelcomeMessageShown && chatNoteList.size() == 9) {
            this.chatNoteList.remove(this.chatNoteList.size() - 1);
            isWelcomeMessageShown = false;
            this.chatNoteList.add(0, commentChat);
            notifyDataSetChanged();
            return;
        }*/
        if (!chatNoteList.isEmpty() && chatNoteList.get(0).equals(commentChat))
            return;
        int index = idAtIndex(commentChat.getId());
        if (!"".equalsIgnoreCase(commentChat.getId()) && index != -1) {
            chatNoteList.set(index, commentChat);
            notifyItemChanged(index);
            return;
        }
        this.chatNoteList.add(0, commentChat);
        notifyItemInserted(0);
    }

    public Integer idAtIndex(String id) {
        if (id == null) return -1;
        for (int i = 0; i < chatNoteList.size(); i++) {
            if (id.equalsIgnoreCase(chatNoteList.get(i).getId())) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int getItemCount() {
        if (chatNoteList == null) {
            return 0;
        }
        return showLoading ? chatNoteList.size() + 1 : chatNoteList.size();
    }

    public void removeChatItem(int position) {
        try {
            chatNoteList.remove(position);
            notifyItemRemoved(position);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void removeChatItem(CommentChat commentChat) {
        try {
            removeChatItem(chatNoteList.indexOf(commentChat));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeChatItem(String message, String sender) {
        int positionToRemove = -1;
        for (int i = 0; i < chatNoteList.size(); i++) {
            CommentChat chatNote = chatNoteList.get(i);
            if (chatNote.getMessage().equalsIgnoreCase(message) && chatNote.getUsername().equalsIgnoreCase(sender)) {
                positionToRemove = i;
                break;
            }
        }
        if (positionToRemove != -1) {
            removeChatItem(positionToRemove);
        }
    }

    public void setMediaStatus(String id, Status status) {
//        Toast.makeText(RheoTvApp.getNonUiContext(), "status: " + status, Toast.LENGTH_LONG).show();
        for (int i = 0; i < chatNoteList.size(); i++) {
            CommentChat chatNote = chatNoteList.get(i);
            if (chatNote.getId().equalsIgnoreCase(id)) {
                chatNote.setStatus(status);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void updateMediaProgress(String id, int progress) {
//        Toast.makeText(RheoTvApp.getNonUiContext(), "progress: " + progress, Toast.LENGTH_LONG).show();
        for (int i = 0; i < chatNoteList.size(); i++) {
            CommentChat chatNote = chatNoteList.get(i);
            if (chatNote.getId().equalsIgnoreCase(id)) {
                chatNote.setProgress(progress);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public class ChatListViewHolder extends BaseViewHolder {
        private ChatItemV2Binding mBinding;

        ChatListViewHolder(ChatItemV2Binding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            Log.i(getClass().getSimpleName(), "message_is: " + chatNoteList.get(position).getUsername() + ": " + chatNoteList.get(position).getMessage() + " and " + chatNoteList.get(position).getMessageType());
            if (position >= chatNoteList.size()) {
                return;
            }
            CommentChat chatNote = chatNoteList.get(position);
            int color;
            if (chatNote instanceof CommentChat.WelcomeComment) {
                color = ((CommentChat.WelcomeComment) chatNote).getTextColor();
            } else if (position == 0) {
                if (!isNewChatItem)
                    color = AppUtils.randomColorForText(mBinding.chatUserName.getContext(), position % 7);
                else
                    color = AppUtils.randomColorForText(mBinding.chatUserName.getContext(), getRandomNumberInRange(0, 6));
            } else
                color = AppUtils.randomColorForText(mBinding.chatUserName.getContext(), position % 7);

            mBinding.setUserColor(color);
            mBinding.setChatItem(chatNote);
            mBinding.getRoot().setTag(chatNote);
            mBinding.executePendingBindings();
            if (chatNote instanceof CommentChat.WelcomeComment) {
                CommentChat.WelcomeComment welcomeComment = (CommentChat.WelcomeComment) chatNote;
                mBinding.chatMessage.setText(welcomeComment.getSpannableMessage(), TextView.BufferType.SPANNABLE);
                mBinding.chatMessage.setTextSize(welcomeComment.getTextSize());
                mBinding.itemParent.setPadding(ViewUtils.dpToPx(4), ViewUtils.dpToPx(4), 0, 0);
                mBinding.itemParent.setBackground(null);
                mBinding.itemParent.setBackgroundColor(Color.TRANSPARENT);
            } else {
                mBinding.chatMessage.setText(chatNote != null && chatNote.getMessage() != null ? chatNote.getMessage().trim() : "");
                mBinding.chatMessage.setTextSize(13f);
                mBinding.itemParent.setBackgroundResource(R.drawable.chat_item_rounded_corner_bg);
                mBinding.itemParent.setPadding(0, 0, 0, 0);
            }

//            mBinding.chatMedia.setOnClickListener(view -> {
//                // todo remove second condition once api start returning message_type
//                if (chatNote.isMedia()) {
//                    mListener.onMediaClicked(chatNote);
//                }
//            });

            mBinding.getRoot().setOnClickListener(view -> {
                if (mListener != null)
                    mListener.onCommentClicked(chatNote);
            });
        }

    }

    public class ChatMediaViewHolder extends BaseViewHolder {
        private ChatItemV2MediaBinding mBinding;

        ChatMediaViewHolder(ChatItemV2MediaBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            Log.i(getClass().getSimpleName(), "message_is: " + chatNoteList.get(position).getUsername() + ": " + chatNoteList.get(position).getMessage() + " and " + chatNoteList.get(position).getMessageType());
            if (position >= chatNoteList.size()) {
                return;
            }
            CommentChat chatNote = chatNoteList.get(position);
            mBinding.setChatItem(chatNote);

            mBinding.chatMedia.setOnClickListener(view -> {
                // todo remove second condition once api start returning message_type
                if (chatNote.isMedia()) {
                    mListener.onMediaClicked(chatNote);
                }
            });

            mBinding.getRoot().setOnClickListener(view -> {
                if (mListener != null)
                    mListener.onCommentClicked(chatNote);
            });
        }

    }

    public class FooterLoadingViewHolder extends BaseViewHolder {

        FooterLoadingViewHolder(FooterLoadingLayoutBinding binding) {
            super(binding.getRoot());
        }

        @Override
        public void onBind(int position) {

        }
    }

    public interface ChatItemClickListener {
        void onReportButtonClick(int position, String username, String comment);

        void onUserProfileClicked(String username);

        void onBlockUserClicked(int position, String username, String comment);
    }

    public interface ChatItemClickListenerV2 {
        void onUserClicked(CommentChat commentChat);

        void onCommentClicked(CommentChat commentChat);

        void onMediaClicked(CommentChat commentChat);
    }

    public String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }
}
