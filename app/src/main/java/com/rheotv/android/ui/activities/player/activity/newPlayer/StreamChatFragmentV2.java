package com.rheotv.android.ui.activities.player.activity.newPlayer;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.databinding.FragmentStreamChatV2Binding;
import com.rheotv.android.ui.activities.player.activity.ChatMenuOptionBottomSheet;
import com.rheotv.android.ui.activities.player.activity.ListOption;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.adapters.ChatListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.EqualSpaceItemDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StreamChatFragmentV2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StreamChatFragmentV2 extends BaseFragment<FragmentStreamChatV2Binding, StreamPlayerViewModelV2> implements ChatListAdapter.ChatItemClickListenerV2 {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    public StreamPlayerViewModelV2 mViewModel;
    private FragmentStreamChatV2Binding mBinding;
    private ChatListAdapter chatAdapter;

    private Observable.OnPropertyChangedCallback askToCommentCallback;
    private Observable.OnPropertyChangedCallback chatBoxCallback;

    public static final String DEFAULT_KEY = "androidx.lifecycle.ViewModelProvider.DefaultKey";
    private String chatFragmentPosition;

    public static final int VIEW_PROFILE = 0x00;
    public static final int FOLLOW_USER = 0x01;
    public static final int REPORT_USER = 0x02;
    public static final int BLOCK_USER = 0x03;
    public static final int DELETE_COMMENT = 0x04;
    public static final int BLOCK_COMMENT = 0x05;
    public static final int REPORT_POST = 0x06;
    public static final int PIN_COMMENT = 0x07;
    public static final int MOVE_THRESHOLD = 50;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StreamChatFragmentV2.
     */
    public static StreamChatFragmentV2 newInstance(String postId) {
        StreamChatFragmentV2 fragment = new StreamChatFragmentV2();
        fragment.chatFragmentPosition = postId != null ? postId : "Default";
        Bundle args = new Bundle();
        args.putString(AppConstants.KEY_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_stream_chat_v2;
    }

    @Override
    public StreamPlayerViewModelV2 getViewModel() {
        if (mViewModel == null) {
            mViewModel = new ViewModelProvider(getParentFragment() != null ? getParentFragment() : this, mViewModelFactory)
                    .get(DEFAULT_KEY + chatFragmentPosition, StreamPlayerViewModelV2.class);
            mViewModel.loadInitialComments(null);
        }
        return null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("position", chatFragmentPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            chatFragmentPosition = savedInstanceState.getString("position");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        if (chatAdapter == null)
            chatAdapter = new ChatListAdapter(new ArrayList<>(), getResources().getConfiguration().orientation, true);
        chatAdapter.setChatStickerSize(stickerSize());
        chatAdapter.setListener(this);
        LinearLayoutManager layoutManager = (LinearLayoutManager) mBinding.recyclerView.getLayoutManager();
        mBinding.recyclerView.addItemDecoration(new EqualSpaceItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics())));
        mBinding.recyclerView.setAdapter(chatAdapter);
        mBinding.recyclerView.setNestedScrollingEnabled(false);
        mBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (layoutManager == null) return;
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (!mViewModel.isLoading.get() && mViewModel.commentNextUrl != null && totalItemCount >= 10 && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && chatAdapter != null) {
                    mViewModel.isLoading.set(true);
                    mBinding.recyclerView.post(() -> chatAdapter.setShowLoading(true));
                    mViewModel.loadComments();
                }

                if (firstVisibleItemPosition == 0)
                    mViewModel.unreadChatCount.set(0);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.comments.observe(getViewLifecycleOwner(), comments -> chatAdapter.addItems(comments));
        mViewModel.incomingComment.observe(getViewLifecycleOwner(), comment -> chatAdapter.addItem(comment));
        mViewModel.removeChat.observe(getViewLifecycleOwner(), pair -> chatAdapter.removeChatItem(pair.first, pair.second));
        mViewModel.updateCheckViews.observe(getViewLifecycleOwner(), check -> updateChatViews());
        mViewModel.blockUserStatus.observe(getViewLifecycleOwner(), this::onUserBlock);
        mViewModel.reportComment.observe(getViewLifecycleOwner(), this::onCommentReport);
        mViewModel.deleteComment.observe(getViewLifecycleOwner(), this::onCommentDelete);
        if (askToCommentCallback == null) {
            askToCommentCallback = new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
                        return;
                    if (mViewModel != null && mViewModel.askToComment != null && mViewModel.askToComment.get() != null && mViewModel.askToComment.get())
                        chatAdapter.addWelcomeNote(mViewModel.authorUsername());
                }
            };
        }
        if (chatBoxCallback == null) {
            chatBoxCallback = new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
                        return;
                    if (mViewModel != null && mViewModel.isChatBoxVisible != null && mViewModel.isChatBoxVisible.get() != null &&
                            !mViewModel.isChatBoxVisible.get() && mViewModel.isChatSentWhenKeyboardOpened) {
                        mViewModel.isChatSentWhenKeyboardOpened = false;
                        chatAdapter.notifyDataSetChanged();
                        RecyclerView.LayoutManager layoutManager = mBinding.recyclerView.getLayoutManager();
                        if (layoutManager != null) {
                            layoutManager.scrollToPosition(0);
                        }
                    }
                }
            };
        }
        mViewModel.askToComment.addOnPropertyChangedCallback(askToCommentCallback);
        mViewModel.isChatBoxVisible.addOnPropertyChangedCallback(chatBoxCallback);
    }

    @Override
    public void onCommentClicked(CommentChat commentChat) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (commentChat instanceof CommentChat.WelcomeComment) {
            return;
        }
        showMenuBottomSheet(commentChat);
    }

    @Override
    public void onMediaClicked(CommentChat commentChat) {

    }

    @Override
    public void onUserClicked(CommentChat commentChat) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (commentChat instanceof CommentChat.WelcomeComment) {
            return;
        }
        showMenuBottomSheet(commentChat);
    }

    private void updateChatViews() {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (!(mBinding.recyclerView.getLayoutManager() instanceof LinearLayoutManager)) return;
        LinearLayoutManager chatLayoutManager = (LinearLayoutManager) mBinding.recyclerView.getLayoutManager();
        if (chatLayoutManager.findFirstVisibleItemPosition() != 0) {
            mViewModel.unreadChatCount.set(mViewModel.unreadChatCount.get() + 1);
        } else {
            mBinding.recyclerView.scrollToPosition(0);
        }
    }

    private int stickerSize() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return (getResources().getDisplayMetrics().widthPixels - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
        else
            return ((getResources().getDisplayMetrics().widthPixels / 2) - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
    }

    public void onReportButtonClick(int position, String username, String comment) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        HashMap<String, Object> property = new HashMap<>(mViewModel.baseProperties);
        if (CommonUtils.isUserLoggedin() && mViewModel.isModerator()) {
            property.put("reported_comment_user", username);
            property.put("reported_comment", comment);
            SegmentTracker.getInstance(getActivity()).trackEvent(SegmentConstants.EVENT_REPORT_COMMENT_ON_SELF_STREAM, property);
            onDeleteCommentClick(position, comment, username);
            mViewModel.reportComment(username, comment, true);
        } else {
            property.put("reported_comment_user", username);
            property.put("reported_comment", comment);
            SegmentTracker.getInstance(getActivity()).trackEvent(SegmentConstants.EVENT_REPORT_COMMENT, property);
            mViewModel.reportComment(username, comment, false);
        }
    }

    private void onDeleteCommentClick(int position, String message, String username) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        chatAdapter.removeChatItem(position);
        mViewModel.sendDeletedMessage(message, username, AppConstants.MSG_TYPE_DELETED);
    }

    private void onBlockCommentClick(int position, String message, String username) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        chatAdapter.removeChatItem(position);
        mViewModel.sendDeletedMessage(message, username, AppConstants.MSG_TYPE_BLOCKED);
    }

    public void onUserProfileClicked(String username) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        Intent intent = ProfileActivity.getCallingIntent(getActivity());
        intent.putExtra("author_name", username);
        startActivity(intent);
    }

    public void onBlockUserClicked(int position, String username, String comment) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        HashMap<String, Object> property = new HashMap<>(mViewModel.baseProperties);
        property.put("blocked_user", username);
        property.put("blocked_msg", comment);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_BLOCK_USER, property);
        onBlockCommentClick(position, comment, username);
        mViewModel.blockUser(username, comment);
    }

    private void onCommentDelete(Status status) {
        if (status == Status.SUCCESS) showToast(getString(R.string.delete_comment_success));
    }

    private void onCommentReport(Status status) {
        if (status == Status.SUCCESS) showToast(getString(R.string.post_report_success));
    }

    private void onUserBlock(Status status) {
        if (status == Status.SUCCESS) showToast(getString(R.string.user_block_message));
    }

    private void showToast(String message) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showMenuBottomSheet(CommentChat commentChat) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (commentChat == null || (commentChat.getUsername() != null && commentChat.getUsername().equalsIgnoreCase(CommonUtils.getUserName(getContext()))))
            return;
        ArrayList<ListOption> listOptions = new ArrayList<>();
        listOptions.add(new ListOption.Header(VIEW_PROFILE));
        listOptions.add(new ListOption.Item(REPORT_USER, "Report", R.drawable.avd_report, null));
        if (mViewModel.isModerator() || mViewModel.isStreamer()) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_block);
            listOptions.add(new ListOption.Item(BLOCK_USER, "Block User", -1, ViewUtils.setTint(drawable, Color.rgb(251, 251, 251))));
            listOptions.add(new ListOption.Item(DELETE_COMMENT, "Delete Comment", R.drawable.ic_delete_outline_white, null));
            listOptions.add(new ListOption.Item(PIN_COMMENT, "Pin Comment", -1, ViewUtils.setTint(ContextCompat.getDrawable(getContext(), R.drawable.avd_pin), Color.rgb(251, 251, 251))));
        }

        ChatMenuOptionBottomSheet bottomSheet = ChatMenuOptionBottomSheet.Companion.newInstance(
                listOptions,
                (ListOption listOption) -> {
                    if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
                        return null;
                    if (listOption instanceof ListOption.Header) {
                        onUserProfileClicked(commentChat.getUsername());
                    } else {
                        switch (((ListOption.Item) listOption).getId()) {
                            case VIEW_PROFILE:
                                onUserProfileClicked(commentChat.getUsername());
                                break;
                            case REPORT_USER:
                                onReportButtonClick(chatAdapter.getList().indexOf(commentChat), commentChat.getUsername(), commentChat.getMessage());
                                break;
                            case BLOCK_USER:
                                onBlockUserClicked(chatAdapter.getList().indexOf(commentChat), commentChat.getUsername(), commentChat.getMessage());
                                break;
                            case DELETE_COMMENT:
                                onDeleteCommentClick(chatAdapter.getList().indexOf(commentChat), commentChat.getMessage(), commentChat.getUsername());
                                break;
                            case PIN_COMMENT:
                                mViewModel.pinComment(commentChat);
                                break;
                        }
                    }
                    return null;
                }
        );
        bottomSheet.setChatMenuOptionData(mViewModel.getChatOptionMenuBottomSheetData(commentChat, commentChat.getUsername(), commentChat.getProfile_pic()));
        try {
            bottomSheet.show(getChildFragmentManager(), ChatMenuOptionBottomSheet.TAG);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        mViewModel.askToComment.removeOnPropertyChangedCallback(askToCommentCallback);
        mViewModel.isChatBoxVisible.removeOnPropertyChangedCallback(chatBoxCallback);
        super.onDestroy();
    }

    public void unpinViewpager() {
        if (chatAdapter != null) {
            chatAdapter.setListener(null);
            chatAdapter = null;
        }
        if (mBinding != null)
            mBinding.recyclerView.setAdapter(null);
    }
}