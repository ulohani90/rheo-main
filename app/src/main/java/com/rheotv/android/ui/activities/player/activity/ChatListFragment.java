package com.rheotv.android.ui.activities.player.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.postlisting.responses.CommentChat;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.FragmentChatListBinding;
import com.rheotv.android.helpers.grpc.ChatHelper;
import com.rheotv.android.ui.activities.profile.viewprofile.view.ProfileActivity;
import com.rheotv.android.ui.adapters.ChatListAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.Direction;
import com.rheotv.android.utils.NetworkUtils;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.ZeroGravityAnimation;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import goChat.Services;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatListFragment.OnChatListFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatListFragment extends BaseFragment<FragmentChatListBinding, ChatViewModel>
        implements ChatListAdapter.ChatItemClickListener, ChatNavigator,
        ChatHelperCallbacks {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private FragmentChatListBinding mBinding;
    private LinearLayoutManager chatLayoutManager;
    private ChatListAdapter chatListAdapter;
    private ChatViewModel mViewModel;

    private int newChatCount = 0;

    private boolean isLoading = false;
    private boolean isFirstTime = true;
    private boolean isSafeChatAdded = false;
    private boolean isInitialChatProcessed = false;
    private boolean isFilling = false;

    private Result currentPlayingPost;
    private int stickersSize;
    private float stickersLayoutHeight;
    private String[] heartEmojiText = {"Kya baat hai", "Maza aa gya yaar", "Ye Mast tha", "Epic yaar", "Just Amazing", "Superb"};

    private static final String ARG_CURRENT_POST = "current_post";

    private OnChatListFragmentInteractionListener mListener;

    private String rheoHeartMessage = "rheo_457_heart";
    private HashMap<String, Object> properties = new HashMap<>();

    public static ChatListFragment newInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        ChatListFragment fragment = new ChatListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentPlayingPost = mListener.getCurrentPost();
        Log.i(getClass().getName(), "handling_chat_onCreate " + currentPlayingPost);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public void onConnectionComplete() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AppConstants.ARG_HEART_COUNT, mViewModel.heartCount);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        if (savedInstanceState != null)
            mViewModel.totalHeartCount.set(savedInstanceState.getString(AppConstants.ARG_HEART_COUNT));


        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            properties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));


        properties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT);

        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT, properties);
        properties.putAll(mListener.getBaseProperties());
        mViewModel.baseProperties = properties;
        mViewModel.baseProperties = properties;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mBinding.parent.setBackground(null);
            stickersSize = (getResources().getDisplayMetrics().widthPixels - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
            mBinding.chatboxLL.getBackground().setAlpha(102);
        } else {
            mBinding.parent.setBackground(getResources().getDrawable(R.drawable.chat_window_gradient_bg));
            stickersSize = ((getResources().getDisplayMetrics().widthPixels / 2) - (int) (2 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()))) / 3;
            mBinding.chatboxLL.getBackground().setAlpha(255);
        }

        if (mBinding.stickerIcon != null) {
            mBinding.stickerIcon.setOnClickListener(view1 -> {
                if (CommonUtils.isUserLoggedin()) {
                    animateStickersRvIn();
                } else {
                    ((PlayerActivity) getActivity()).openLoginFlow();
                }
            });
        }

        Log.i(getClass().getName(), "chat_view_created " + currentPlayingPost);
        initializeChatViews();
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_chat_list;
    }

    @Override
    public ChatViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ChatViewModel.class);
        mViewModel.setNavigator(this);
        return mViewModel;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatListFragmentInteractionListener) {
            mListener = (OnChatListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChatListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

/*    @Override
    public void onStickerClicked(String stickerUrl, String stickerId) {
        Properties properties = new Properties();
        if (getContext() != null && !NetworkUtils.isNetworkConnected(getContext())) {
            showToast("Please check you internet connection");
            return;
        }

        animateStickersRvOut();
        addOrRemoveSafeChatMessage(true);
        if (!CommonUtils.isUserLoggedin()) {
            mListener.askForLogin();
            return;
        }
        SegmentTracker.getInstance(getActivity()).trackEvent(SegmentConstants.EVENT_CHAT_STICKER_SENT, properties.putValue("message_sticker", stickerId));
        adjustChatRVHeight();
        //activityPlayerBinding.chatbox.setText("");

        scrollChat();
        Log.d(getClass().getName(), "sending_chattask");
        ChatHelper.getInstance(getActivity()).sendMessage(stickerUrl, currentPlayingPost.getId(), this);
    }*/

    private void initializeChatViews() {
        if (currentPlayingPost == null || currentPlayingPost.getAuthor() == null) return;

        checkChatBoxState();

        chatListAdapter = new ChatListAdapter(new ArrayList<>(), getResources().getConfiguration().orientation, false);
        Log.i(getClass().getName(), "initializeChatViews currentPlayingPost is " + currentPlayingPost + " and " + currentPlayingPost.getHeartCount());
        mBinding.heartCount.setVisibility(View.VISIBLE);
        mViewModel.updateHeartCount(currentPlayingPost.getHeartCount());

        chatLayoutManager = new LinearLayoutManager(getContext());
        mBinding.userChatRV.setLayoutManager(chatLayoutManager);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            mBinding.userChatRV.addItemDecoration(new ChatItemDecorator((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics())));
        chatListAdapter.setChatStickerSize(stickersSize);
        mBinding.userChatRV.setAdapter(chatListAdapter);
        if (CommonUtils.isUserLoggedin() && (CommonUtils.getUserName(getContext()).equalsIgnoreCase(currentPlayingPost.getAuthor().getUser().getUsername())
                || (CommonUtils.getUserEmailAddress() != null && currentPlayingPost.getAuthor().getModerators() != null && currentPlayingPost.getAuthor().getModerators().contains(CommonUtils.getUserEmailAddress())))) {
            chatListAdapter.setSelfStream(true);
        }


        chatListAdapter.addItems(currentPlayingPost.getLiveChat());
//        chatListAdapter.setListener(this);
        adjustChatRVHeight();

        chatLayoutManager.setReverseLayout(true);
        chatLayoutManager.setStackFromEnd(true);
        mBinding.userChatRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = chatLayoutManager.getChildCount();
                int totalItemCount = chatLayoutManager.getItemCount();
                int firstVisibleItemPosition = chatLayoutManager.findFirstVisibleItemPosition();

                // Load more if we have reach the end to the recyclerView
                if (!isLoading && mViewModel.commentNextUrl != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isLoading = true;
                    chatListAdapter.setShowLoading(true);
                    mViewModel.fetchCommentsFromUrl(currentPlayingPost.getId());
                }
                if (firstVisibleItemPosition == 0) {
                    newChatCount = 0;
                    if (mBinding.unreadCommentLayout != null)
                        mBinding.unreadCommentLayout.setVisibility(View.GONE);
                    mListener.onChatBadgeUpdate(0);
                }
            }
        });

        Log.i(getClass().getName(), "isInitialChatProcessed : " + isInitialChatProcessed + " and " + currentPlayingPost.getIsLive());
        if (!isInitialChatProcessed) {
            isInitialChatProcessed = true;
            if (currentPlayingPost.getIsLive()) {
                mListener.onTotalViewUpdate();
            }
            handleChatSender();
            addOrRemoveSafeChatMessage(!isFirstTime);
            mViewModel.fetchComments(currentPlayingPost.getId());
        }
        adjustChatRVHeight();
    }

    public void checkChatBoxState() {
        if (mBinding != null)
            if (CommonUtils.isUserLoggedin()) {
                mBinding.chatbox.setVisibility(View.VISIBLE);
                mBinding.chatboxLoginBtn.setVisibility(View.GONE);
                paintChatBox();
            } else {
                mBinding.chatbox.setVisibility(View.GONE);
                mBinding.chatboxLoginBtn.setVisibility(View.VISIBLE);
                mBinding.chatboxLoginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((PlayerActivity) getActivity()).openLoginFlow();
                    }
                });
            }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //private boolean isInitialChatSend = false;
    private boolean isInitialHeartSend = false;

    private void handleChatSender() {
        paintChatBox();
        mBinding.sendButton.setOnClickListener(view -> {
            if (getContext() != null && !NetworkUtils.isNetworkConnected(getContext())) {
                showToast("Please check you internet connection");
                return;
            }

            addOrRemoveSafeChatMessage(true);
            if (!CommonUtils.isUserLoggedin()) {
                mListener.askForLogin();
                return;
            }

            if (mViewModel.canComment) {
                HashMap<String, Object> property = new HashMap<>(properties);
                String message = mBinding.chatbox.getText().toString();
                if (message == null || message.isEmpty() || message.trim().length() == 0) {
                    return;
                }
                property.put("orientation", getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "portrait" : "landscape");

                /*if (!isInitialChatSend) {
                    isInitialChatSend = true;
                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CHAT_SEND_FIRST_CLICKED, property.putValue("message", message));
                }*/
                property.put("message", message);
                property.put("game", currentPlayingPost.getGame());
                property.put("author", currentPlayingPost.getAuthor().getUser().getUsername());
                property.put("post_id", currentPlayingPost.getId());
                if (CommonUtils.isFirstCommentSendNotTracked()) {
                    CommonUtils.setFirstCommentSentEventTracked();
                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_CHAT_SEND_FIRST_CLICKED, property);
                }

                adjustChatRVHeight();
                mBinding.chatbox.setText("");
                CommonUtils.hideKeyboard(getActivity());
                scrollChat();
                Log.d(RheoTvApp.TAG, "sending chattask");
                ChatHelper.getInstance(getActivity()).sendMessage(message, currentPlayingPost.getId(), this);
            } else {
                CommonUtils.hideKeyboard(getActivity());
                mBinding.chatbox.setText("");
                Toast.makeText(getActivity(), "You are not allowed to post messages in this live stream.", Toast.LENGTH_SHORT).show();
            }
        });


        fillHeart(mBinding.heartImageView);
        mBinding.heartImageView.setOnClickListener(this::fadeAndScaleHeart);

    }

    private void paintChatBox() {
        String hint = "Send a nice message";
        if (!CommonUtils.isUserLoggedin()) {
            hint = "Login to chat.";
        }
        mBinding.chatbox.setHint(hint);
    }

    @Override
    public void onReportButtonClick(int position, String username, String comment) {
        if (!isAdded()) return;
        HashMap<String, Object> property = properties;
        if (CommonUtils.isUserLoggedin() && (CommonUtils.getUserName(getActivity()).equalsIgnoreCase(currentPlayingPost.getAuthor().getUser().getUsername())
                || (CommonUtils.getUserEmailAddress() != null && currentPlayingPost.getAuthor().getModerators() != null && currentPlayingPost.getAuthor().getModerators().contains(CommonUtils.getUserEmailAddress())))) {
            property.put("reported_comment_user", username);
            property.put("reported_comment", comment);
            SegmentTracker.getInstance(getActivity()).trackEvent(SegmentConstants.EVENT_REPORT_COMMENT_ON_SELF_STREAM, property);
            onDeleteCommentClick(position, comment, username, "deleted");
            mViewModel.reportComment(currentPlayingPost.getId(), username, comment, true);
        } else {
            property.put("reported_comment_user", username);
            property.put("reported_comment", comment);
            SegmentTracker.getInstance(getActivity()).trackEvent(SegmentConstants.EVENT_REPORT_COMMENT, property);
            mViewModel.reportComment(currentPlayingPost.getId(), username, comment, false);
        }
    }

    @Override
    public void onUserProfileClicked(String username) {
        if (!isAdded()) return;
        Intent intent = ProfileActivity.getCallingIntent(getActivity());
        intent.putExtra("author_name", username);
        intent.putExtra(AppConstants.SCREEN_SOURCE, SegmentConstants.SCREEN_NAME_VIDEO_PLAYER_CHAT);
        startActivity(intent);
    }

    @Override
    public void onBlockUserClicked(int position, String username, String comment) {
        if (!isAdded()) return;
        HashMap<String, Object> property = properties;
        property.put("blocked_user", username);
        property.put("blocked_msg", comment);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_BLOCK_USER, property);
        onDeleteCommentClick(position, comment, username, "blocked");
        mViewModel.blockUser(currentPlayingPost.getId(), username, comment);
    }

    private void showNewChatButton() {
        if (mBinding.unreadCommentLayout != null && mListener != null) {
            mBinding.unreadCommentLayout.setVisibility(View.VISIBLE);
            mBinding.unreadCommentCount.setText(newChatCount <= 50 ? newChatCount + "" : 50 + "+");
            mListener.onChatBadgeUpdate(newChatCount <= 50 ? newChatCount : 50);
            mBinding.unreadCommentLayout.setOnClickListener(view -> {
                scrollChat();
                newChatCount = 0;
                mBinding.unreadCommentLayout.setVisibility(View.GONE);
                mListener.onChatBadgeUpdate(0);
            });
        }
    }

    private void scrollChat() {
        mBinding.userChatRV.scrollToPosition(0);
//        new Handler().postDelayed(() -> mBinding.userChatRV.scrollToPosition(0), 200);
    }

    @Override
    public void addItemsInChat(String postId, List<CommentChat> comments) {
        if (!isAdded()) return;
        isLoading = false;
        if (currentPlayingPost != null && currentPlayingPost.getId() != null && currentPlayingPost.getId().equalsIgnoreCase(postId)) {
            chatListAdapter.addItems(comments);
            adjustChatRVHeight();
        }
    }

    private void adjustChatRVHeight() {
        if (getContext() == null) return;
        int orientation = this.getResources().getConfiguration().orientation;
        int defaultHeightDP = (int) ViewUtils.getScreenWidthInDP(getContext());
        defaultHeightDP = defaultHeightDP / 2;
        defaultHeightDP = 150;

        Log.d(RheoTvApp.TAG, "widht : " + defaultHeightDP);

        if (mBinding.userChatRV.getAdapter() == null && mBinding.userChatRV.getAdapter().getItemCount() < 1) {
            return;
        }
        /*if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams layoutParams = mBinding.userChatRV.getLayoutParams();
            int totalItemCount = mBinding.userChatRV.getAdapter().getItemCount();

            if (totalItemCount < 5) {
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {
                layoutParams.height = ViewUtils.dpToPx(defaultHeightDP);
            }

            mBinding.userChatRV.setLayoutParams(layoutParams);
        }*/
    }

    @Override
    public void showReportPostSuccessToast() {
        if (isAdded())
            Toast.makeText(getContext(), getString(R.string.post_report_success), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showDeleteSuccessToast() {
        if (isAdded())
            Toast.makeText(getContext(), getString(R.string.delete_comment_success), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBlockUserSuccess() {
        if (isAdded())
            Toast.makeText(getContext(), "Blocked user successfully", Toast.LENGTH_SHORT).show();
    }

    public void onDeleteCommentClick(int position, String message, String username, String messageType) {
        chatListAdapter.removeChatItem(position);
        ChatHelper.getInstance(getContext()).sendDeletedMessage(message, username, currentPlayingPost.getId(), messageType, this);
    }

    public void animateStickersRvIn() {
        mListener.updateStickerFlag();
//        CommonUtils.hideKeyboard(getActivity());
//        mBinding.stickerOverlay.setVisibility(View.VISIBLE);
//        mBinding.stickerOverlay.setOnClickListener(view -> animateStickersRvOut());
//        mBinding.closeStickers.setOnClickListener(view -> animateStickersRvOut());
//        if (stickersLayoutHeight == 0) {
//            stickersLayoutHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
//        }
//        mBinding.stickersLayout.setVisibility(View.VISIBLE);
//        ObjectAnimator anim = ObjectAnimator.ofFloat(mBinding.stickersLayout, View.TRANSLATION_Y, stickersLayoutHeight, 0);
//        anim.setDuration(300);
//        anim.setInterpolator(new AccelerateDecelerateInterpolator());
//        anim.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                setUpStickersRV();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        anim.start();
    }

    /*   public void animateStickersRvOut() {
           Log.i(getClass().getName(), "animateStickersRvOut " + currentPlayingPost);
           mListener.updateStickerFlag(false);
           mBinding.stickerOverlay.setVisibility(View.GONE);
           ObjectAnimator anim = ObjectAnimator.ofFloat(mBinding.stickersLayout, View.TRANSLATION_Y, 0, stickersLayoutHeight);
           anim.setDuration(300);
           anim.setInterpolator(new AccelerateDecelerateInterpolator());
           anim.addListener(new Animator.AnimatorListener() {
               @Override
               public void onAnimationStart(Animator animator) {

               }

               @Override
               public void onAnimationEnd(Animator animator) {
                   mBinding.stickersLayout.setVisibility(View.GONE);
               }

               @Override
               public void onAnimationCancel(Animator animator) {

               }

               @Override
               public void onAnimationRepeat(Animator animator) {

               }
           });
           anim.start();
       }

       private void setUpStickersRV() {
           if (stickersAdapter == null) {
               GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
               mBinding.stickersRv.setLayoutManager(layoutManager);
               stickersAdapter = new StickersRvAdapter(stickersSize);
               mBinding.stickersRv.setAdapter(stickersAdapter);
               stickersAdapter.setmListener(this);
               mBinding.stickersLoading.setVisibility(View.VISIBLE);
               if (currentPlayingPost != null)
                   mViewModel.loadStickers(currentPlayingPost.getId());
               mBinding.stickersRv.setOnScrollListener(new RecyclerView.OnScrollListener() {
                   @Override
                   public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                       super.onScrollStateChanged(recyclerView, newState);
                   }

                   @Override
                   public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                       super.onScrolled(recyclerView, dx, dy);
                       int visibleItemCount = layoutManager.getChildCount();
                       int totalItemCount = layoutManager.getItemCount();
                       int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                       // Load more if we have reach the end to the recyclerView
                       if (!isStickersLoading && mViewModel.stickersNextUrl != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                           isStickersLoading = true;
                           stickersAdapter.setShowLoading(true);
                           mViewModel.loadStickers(currentPlayingPost.getId());
                       }
                   }
               });
           }

       }

       @Override
       public void onStickersLoadComplete(List<Sticker> stickers) {
           mBinding.stickersLoading.setVisibility(View.GONE);
           isStickersLoading = false;
           stickersAdapter.setShowLoading(false);
           stickersAdapter.setStickers(stickers);
       }
   */
    private void addOrRemoveSafeChatMessage(boolean toRemove) {
        this.isFirstTime = false;
        Log.d(RheoTvApp.TAG, "adding safe chat");
        if (toRemove && isSafeChatAdded && chatListAdapter.getList().size() > 4) {
            isSafeChatAdded = false;
            List<CommentChat> chatList = chatListAdapter.getList();
            for (int index = 0; index < chatList.size(); index++) {
                CommentChat commentChat1 = chatList.get(index);
                if (commentChat1.getId().equals(CommonUtils.SAFE_CHAT_ID)) {
                    chatList.remove(index);
                    chatListAdapter.notifyItemRemoved(index);
                }
            }

        } else if (!toRemove) {
            String message = "Hey! \nI am star alien - Big Boss of this house.\uD83D\uDE0E.Be nice while having chat. \nEnjoy streaming and say 'hi' to the streamer. It's free! \n \uD83D\uDE4C";
            CommentChat commentChat = new CommentChat(CommonUtils.SAFE_CHAT_ID, message, "Star Alien", CommonUtils.STAR_ALIEN_PIC);
            isSafeChatAdded = true;
            chatListAdapter.addItem(commentChat);
//            adjustChatRVHeight();
            scrollChat();
        }
    }

    private Runnable heartRunner;
    private Handler heartHandler = new Handler();

    private void fillHeart(View view) {
        ImageView imageView = (ImageView) view;
        imageView.setVisibility(View.VISIBLE);
        Animatable animatable = (Animatable) imageView.getDrawable();
        animatable.start();

        heartRunner = () -> {
            Log.i(AppConstants.TAG, "checking_heart_state");
            animatable.stop();
            isFilling = false;
            scaleHeart(view);
        };
        heartHandler.postDelayed(heartRunner, 9000);
    }

    private void scaleHeart(View view) {
        Log.i(AppConstants.TAG, "scaling_Heart");
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 2f),
                PropertyValuesHolder.ofFloat("scaleY", 2f));
        scaleUp.setDuration(250);
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f));
        scaleDown.setDuration(250);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleDown).after(scaleUp);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    private void fadeAndScaleHeart(View view) {
        if (!NetworkUtils.isNetworkConnected(getContext())) {
            showToast("Please check you internet connection");
            return;
        }

        if (!CommonUtils.isUserLoggedin()) {
            mListener.askForLogin();
            return;
        }

        if (isFilling) {
            showToast("Filling Heart for you!");
            return;
        }

        String segmentUrl = mListener.getSegmentUrl();
        mViewModel.addHeart(segmentUrl, currentPlayingPost.getId(), CommonUtils.getUserName(getContext()), currentPlayingPost.getAuthor().getUser().getUsername(), getContext());
        String message = rheoHeartMessage;
        ChatHelper.getInstance(getContext()).sendMessage(message, currentPlayingPost.getId(), this);

        isFilling = true;

        ObjectAnimator fade = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        fade.setDuration(300);
        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 2.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 2.0f));
        scale.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fade, scale);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setAlpha(1.0f);
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
                //animateHeartUp();

                fillHeart(view);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    public void flyEmoji(final int resId) {
        ZeroGravityAnimation animation = new ZeroGravityAnimation();
        animation.setCount(1);
        animation.setScalingFactor(0.2f);
        animation.setOriginationDirection(Direction.BOTTOM);
        animation.setDestinationDirection(Direction.TOP);
        animation.setImage(resId);
        animation.setAnimationListener(new Animation.AnimationListener() {
                                           @Override
                                           public void onAnimationStart(Animation animation) {

                                           }

                                           @Override
                                           public void onAnimationEnd(Animation animation) {

                                           }

                                           @Override
                                           public void onAnimationRepeat(Animation animation) {

                                           }
                                       }
        );

        ViewGroup container = mBinding.heartContainer;
        animation.play(getActivity(), container);

    }


    private void animateHeartUp() {
        // You can change the number of emojis that will be flying on screen
        /*for (int i = 0; i < 5; i++) {
            flyEmoji(R.drawable.ic_like_56);
        }*/
        if (getActivity() != null) {
            if (mBinding.heartContainer.getChildCount() == 20) {
                return;
            }
            mViewModel.heartCount++;
            mViewModel.updateHeartCount();
            mListener.onHeartCountUpdate(mViewModel.localHeartCounter);
            ImageView heartImageView = new ImageView(getActivity());
            heartImageView.setImageResource(R.drawable.ic_heart_filled_16);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            if (mViewModel.heartCount % 3 == 1) {
                lp.leftMargin = CommonUtils.toPix(12);
            } else if (mViewModel.heartCount % 3 == 2) {
                lp.rightMargin = CommonUtils.toPix(12);
            }

            mBinding.heartContainer.addView(heartImageView, lp);

            //mBinding.heartAnimImageView.setVisibility(View.VISIBLE);
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(heartImageView, View.TRANSLATION_Y, 0, -mBinding.heartContainer.getHeight());
            ObjectAnimator animator3 = ObjectAnimator.ofFloat(heartImageView, View.SCALE_X, 1.0f, 1.5f);
            ObjectAnimator animator4 = ObjectAnimator.ofFloat(heartImageView, View.SCALE_Y, 1.0f, 1.5f);
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(heartImageView, View.ALPHA, 1, 0);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(2000);
        /*long delay = (long) AppUtils.getRandomDoubleBetweenRange(5000, 2000);

        animatorSet.setStartDelay(delay);*/
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    heartImageView.setVisibility(View.GONE);
                    mBinding.heartContainer.removeView(heartImageView);
                    mViewModel.heartCount--;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorSet.playTogether(animator1, animator2, animator3, animator4);
            animatorSet.start();
        }
    }


    @Override
    public void onHeartUpdate(int count) {
        if (isAdded())
            currentPlayingPost.setHeartCount(String.valueOf(count));
    }

    public void updateHeartCounter(String count) {
        mViewModel.totalHeartCount.set(count);
    }

    @Override
    public void onMessageSend(Services.ChatMessage note) {
        Log.i(getClass().getSimpleName(), "updateNewChat_1: " + note.getMessage() + " and type: " + note.getMsgType());
        if (getActivity() == null || mListener == null || !isAdded()) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(RheoTvApp.TAG, "Message : " + note.getMessage());

                if (note.getSender() != null && !note.getSender().isEmpty()) {
                    Log.i(getClass().getName(), "first_chat_update " + note.getSender() + " and " + CommonUtils.getUserName(getContext()) + " and " + RewardManager.getInstance().isFirstCommentRewardAvailable());
                    if (note.getSender().equals(CommonUtils.getUserName(getContext()))) {
                        mListener.checkFirstCommentReward();
                    }
                    // && !note.getUsername().equals(CommonUtils.getUserName(getBaseContext()))
                    if (note.getMsgType().equalsIgnoreCase("deleted") || note.getMsgType().equalsIgnoreCase("blocked")) {
                        String message = note.getMessage();
                        String sender = note.getSender();
                        chatListAdapter.removeChatItem(message, sender);
                        if (note.getMsgType().equalsIgnoreCase("blocked") && CommonUtils.isUserLoggedin() && CommonUtils.getUserName(getContext()).equalsIgnoreCase(sender)) {
                            mViewModel.canComment = false;
                        }
                    } else if (note.getMessage().equalsIgnoreCase(rheoHeartMessage)) {
                        animateHeartUp();
                    } else {
                        CommentChat commentChat = CommentChat.getComment(note);
                        chatListAdapter.addItem(commentChat);
//                    adjustChatRVHeight();
                        if (chatLayoutManager.findFirstVisibleItemPosition() != 0) {
                            newChatCount++;

                            showNewChatButton();
                        } else {
                            scrollChat();
                        }
                    }
                }
            }
        });
    }

    public void showToast(String message) {
        if (isAdded())
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageDelete(Services.ChatMessage chatMessage) {

    }

    @Override
    public void waitAndReconnect() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnChatListFragmentInteractionListener {
        void checkFirstCommentReward();

        void askForLogin();

        void onTotalViewUpdate();

        String getSegmentUrl();

        Result getCurrentPost();

        void updateStickerFlag();

        void onChatBadgeUpdate(int count);

        void onHeartCountUpdate(int count);

        HashMap<String, Object> getBaseProperties();
    }

    public class ChatItemDecorator extends RecyclerView.ItemDecoration {
        int spacing;

        public ChatItemDecorator(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildLayoutPosition(view);
            if (position == 0) {
                outRect.bottom = spacing;
            }
            outRect.top = spacing;
            outRect.left = spacing;
            outRect.right = spacing;
        }
    }

    @Override
    public void updateLiveCount(String liveCount) {

    }

    @Override
    public void setUpViewersRequest() {

    }
}
