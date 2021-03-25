package com.rheotv.android.ui.activities.player.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.databinding.FragmentStreamChatBinding;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.adapters.ChatListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.AppUtilsKt;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.EqualSpaceItemDecorator;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

public class StreamChatFragment extends BaseFragment<FragmentStreamChatBinding, StreamPlayerViewModel> implements ChatListAdapter.ChatItemClickListenerV2 {
    public static final String TAG = "StreamChatFragment";
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    public StreamPlayerViewModel mViewModel;
    private FragmentStreamChatBinding mBinding;
    private ChatListAdapter chatAdapter;
    private HashMap<String, Object> baseProperties;

    private ChatScrollListener mListener;
    private int mSlop;

    private String chatFragmentPosition;

    private Observable.OnPropertyChangedCallback askToCommentCallback;

    private Observable.OnPropertyChangedCallback chatBoxCallback;

    public static StreamChatFragment newInstance(String postId, boolean isModerator) {
        StreamChatFragment fragment = new StreamChatFragment();
        fragment.chatFragmentPosition = postId != null ? postId : "Default";
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.KEY_POST_ID, postId);
        bundle.putBoolean(AppConstants.ARG_IS_MODERATOR, isModerator);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setmListener(ChatScrollListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_stream_chat;
    }

    public static final String DEFAULT_KEY = "androidx.lifecycle.ViewModelProvider.DefaultKey";

    @Override
    public StreamPlayerViewModel getViewModel() {
        if (mViewModel == null) {
            mViewModel = new ViewModelProvider(getParentFragment() != null ? getParentFragment() : this, mViewModelFactory).get(DEFAULT_KEY + chatFragmentPosition, StreamPlayerViewModel.class);

            if (getArguments() != null) {
                mViewModel.postId = getArguments().getString("post_id", "");
                mViewModel.isModerator = getArguments().getBoolean("is_moderator", false);
            }

            mViewModel.loadInitialComments(null);
        }
//        mViewModel.loadComments();
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
        mViewModel.comments.observe(this, comments -> chatAdapter.addItems(comments));
        mViewModel.incomingComment.observe(this, comment -> chatAdapter.addItem(comment));
        mViewModel.removeChat.observe(this, pair -> chatAdapter.removeChatItem(pair.first, pair.second));
        mViewModel.updateCheckViews.observe(this, check -> updateChatViews());
        mViewModel.blockUserStatus.observe(this, this::onUserBlock);
        mViewModel.reportComment.observe(this, this::onCommentReport);
        mViewModel.deleteComment.observe(this, this::onCommentDelete);
        mViewModel.askToComment.addOnPropertyChangedCallback(askToCommentCallback);
        return mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            chatFragmentPosition = savedInstanceState.getString("position");
        }
        super.onCreate(savedInstanceState);
        mViewModel.isChatBoxVisible.addOnPropertyChangedCallback(chatBoxCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("position", chatFragmentPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppUtilsKt.INSTANCE.runGC();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        setupViews();
        baseProperties = new HashMap<>(mViewModel.baseProperties);
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT, baseProperties);
    }

    private void setupViews() {
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

                if (firstVisibleItemPosition == 0) {
                    mViewModel.unreadChatCount.set(0);
                }
            }
        });

        mBinding.unreadButton.setOnClickListener(v -> mBinding.recyclerView.smoothScrollToPosition(0));
        mSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
        //mBinding.recyclerView.setOnTouchListener(containerTouchListener);
    }

    private int stickerSize() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return (getResources().getDisplayMetrics().widthPixels - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
        else
            return ((getResources().getDisplayMetrics().widthPixels / 2) - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
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

    private void showMenuBottomSheet(CommentChat commentChat) {
        if (mViewModel == null || mBinding == null || isStateSaved() || !isAdded())
            return;
        if (commentChat == null || (commentChat.getUsername() != null && commentChat.getUsername().equalsIgnoreCase(CommonUtils.getUserName(getContext()))))
            return;
        ArrayList<ListOption> listOptions = new ArrayList<>();
        listOptions.add(new ListOption.Header(StreamPlayerFragment.VIEW_PROFILE));
        listOptions.add(new ListOption.Item(StreamPlayerFragment.REPORT_USER, "Report", R.drawable.avd_report, null));
        if (mViewModel.isModerator() || mViewModel.isStreamer()) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_block);
            listOptions.add(new ListOption.Item(StreamPlayerFragment.BLOCK_USER, "Block User", -1, ViewUtils.setTint(drawable, Color.rgb(251, 251, 251))));
            listOptions.add(new ListOption.Item(StreamPlayerFragment.DELETE_COMMENT, "Delete Comment", R.drawable.ic_delete_outline_white, null));
            listOptions.add(new ListOption.Item(StreamPlayerFragment.PIN_COMMENT, "Pin Comment", -1, ViewUtils.setTint(ContextCompat.getDrawable(getContext(), R.drawable.avd_pin), Color.rgb(251, 251, 251))));
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
                            case StreamPlayerFragment.VIEW_PROFILE:
                                onUserProfileClicked(commentChat.getUsername());
                                break;
                            case StreamPlayerFragment.REPORT_USER:
                                onReportButtonClick(chatAdapter.getList().indexOf(commentChat), commentChat.getUsername(), commentChat.getMessage());
                                break;
                            case StreamPlayerFragment.BLOCK_USER:
                                onBlockUserClicked(chatAdapter.getList().indexOf(commentChat), commentChat.getUsername(), commentChat.getMessage());
                                break;
                            case StreamPlayerFragment.DELETE_COMMENT:
                                onDeleteCommentClick(chatAdapter.getList().indexOf(commentChat), commentChat.getMessage(), commentChat.getUsername());
                                break;
                            case StreamPlayerFragment.PIN_COMMENT:
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

   /* private View.OnTouchListener containerTouchListener = new View.OnTouchListener() {
        private int initialX;

        private float initialTouchX;

        private float initialY;

        private float initialTouchY;

        int lastAction;

        boolean isDirectionFound;

        boolean isHorizontalScroll;

        long tapDownTS;

        long tapUpTS;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mListener != null) {
                        tapDownTS = System.currentTimeMillis();
                        //remember the initial position.
                        int[] containerPos = mListener.currentContainerPos();
                        initialX = containerPos[0];
                        initialY = containerPos[1];

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        Log.i(PlayerHeadService.class.getCanonicalName(), "Initial X " + initialX);
                        Log.i(PlayerHeadService.class.getCanonicalName(), "Initial Y " + initialX);
                        Log.i(PlayerHeadService.class.getCanonicalName(), "Initial Touch X " + initialTouchX);
                        Log.i(PlayerHeadService.class.getCanonicalName(), "Initial Touch Y " + initialTouchX);

                        lastAction = event.getAction();
                        isDirectionFound = false;
                        isHorizontalScroll = false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:

                case MotionEvent.ACTION_UP:
                    //As we implemented on touch listener with ACTION_MOVE,
                    //we have to check if the previous action was ACTION_DOWN
                    //to identify if the user clicked the view or not.

                    lastAction = event.getAction();
                    mListener.handleChatContentVisibility();
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    tapUpTS = System.currentTimeMillis();
                    if (tapUpTS - tapDownTS < 300 && !isDirectionFound) {
                        View tappedView = mBinding.recyclerView.findChildViewUnder(event.getX(), event.getY());
                        if (tappedView != null) {
                            CommentChat commentClicked = (CommentChat) tappedView.getTag();
                            if (commentClicked != null)
                                onUserClicked(commentClicked);
                        }
                    }
                    if (isHorizontalScroll) {
                        return true;
                    } else {
                        return false;
                    }

                case MotionEvent.ACTION_MOVE:
                    //Calculate the X and Y coordinates of the view.
                    int dx = (int) (event.getRawX() - initialTouchX);
                    int dy = (int) (event.getRawY() - initialTouchY);
                    if (!isDirectionFound) {
                        if (Math.abs(dy) > mSlop && Math.abs(dx) < mSlop) {
                            //Vertical Scroll More
                            mBinding.recyclerView.getParent().requestDisallowInterceptTouchEvent(false);
                            isDirectionFound = true;
                            return false;
                        } else if (Math.abs(dx) > mSlop && Math.abs(dy) < mSlop) {
                            //Horizontal Scroll More
                            mBinding.recyclerView.getParent().requestDisallowInterceptTouchEvent(true);
                            isHorizontalScroll = true;
                            isDirectionFound = true;
                            return true;
                        }

                    } else if (isHorizontalScroll) {
                        long finalX = (initialX + (int) (event.getRawX() - initialTouchX));
                        //Log.i("Current Position", "Moving to X::" + params.x + "  Y::" + params.y);
                        if (finalX < 0) {
                            finalX = 0;
                        }
                        mListener.updatePos(finalX);
                        return true;
                    } else {
                        return false;
                    }
                    lastAction = event.getAction();
                    //view.getParent().requestDisallowInterceptTouchEvent(true);

            }
            return false;
        }
    };*/

    public interface ChatScrollListener {
        void updatePos(long pos);

        void handleChatContentVisibility();

        int[] currentContainerPos();
    }


}
