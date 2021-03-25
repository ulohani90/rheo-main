package com.rheotv.android.ui.activities.player.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.play.RequestPlayResponse;
import com.rheotv.android.data.network.models.play.ResultsItem;
import com.rheotv.android.data.network.models.postlisting.responses.Result;
import com.rheotv.android.databinding.FragmentRequestPlayBinding;
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment;
import com.rheotv.android.ui.customViews.WebviewActivity;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.DownloadShareManager;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_FIRST_PLAY_REQUEST;
import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_PLAY_REQUEST;
import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_PLAY_REQUEST_ACTION_ACCEPT;
import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_PLAY_REQUEST_ACTION_REFUND;
import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_PLAY_REQUEST_ACTION_REJECT;
import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_SUBMIT_CUSTOM_ROOM_DETAILS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestPlayFragment.OnRequestToPlayFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestPlayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestPlayFragment extends BaseBottomSheetDialogFragment<FragmentRequestPlayBinding, RequestPlayViewModel> implements
        RequestPlayNavigator, PlayRequestListener, RequestToPlayDialogFragment.OnRequestToPlayInteractionListener,
        NotAbleToPlayDialogFragment.NotAbleToPlayDialogListener {

    public static final String TAG = "RequestPlayFragment";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    PlayRequestAdapter adapter;

    private RequestPlayViewModel mViewModel;
    private FragmentRequestPlayBinding mBinding;
    private Result currentPost;
    private String userId;
    private RequestToPlayDialogFragment acceptDialog;
    private NotAbleToPlayDialogFragment refundDialog;
    private boolean isMe = false;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    private SegmentTracker tracker = SegmentTracker.getInstance(getContext());

    public static RequestPlayFragment newInstance(String source, Result post) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        RequestPlayFragment fragment = new RequestPlayFragment();
        fragment.currentPost = post;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        ViewGroup.LayoutParams layoutParams = mBinding.container.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (ViewUtils.getScreenHeightInPx(getContext()) * 0.6));
        }
        layoutParams.height = (int) (ViewUtils.getScreenHeightInPx(getContext()) * 0.6);
        mBinding.container.setLayoutParams(layoutParams);
        adjustWindow(view);
        setupViews();
    }

//    int count = 0;
//
//    private ResultsItem getItem() {
//        ResultsItem resultsItem = new ResultsItem();
//        resultsItem.setId("2b227e04-7b54-4299-bf2f-6234bf107c49");
//        resultsItem.setToPost(currentPost.getId());
//        resultsItem.setGameUsername("chhatrasal");
//        FromUserProfile fromUserProfile = new FromUserProfile();
//        fromUserProfile.setId("a51a23d3-4ba5-445a-983f-86d1ea01a73a");
//        fromUserProfile.setProfilePic("https://rheovideos.blob.core.windows.net/rheovideos/cache/32/1e/321e0e92f32f17069746bf999b252e11.webp");
//        User user = new User();
//        user.setId(1592253);
//        user.setFirstName("Chhatrasal");
//        user.setLastName("Singh");
//        user.setUsername(CommonUtils.getUserName(getContext()));
//        fromUserProfile.setUser(user);
//        resultsItem.setFromUserProfile(fromUserProfile);
//        return resultsItem;
//    }

    PublishSubject<ResultsItem> waitingPublishSubject;
    PublishSubject<ResultsItem> pendingPlayerPublishSubject;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ResultsItem resultsItem) {
        if (resultsItem == null || adapter == null) return;
        Log.i(TAG, "Play request grpc data received!!!!");
        addPlayer(resultsItem);
        if (isMe) {
            if (pendingPlayerPublishSubject != null)
                pendingPlayerPublishSubject.onNext(resultsItem);
        } else {
            if (waitingPublishSubject != null) waitingPublishSubject.onNext(resultsItem);
        }
    }

    @Override
    public void updateWaitingNumber(String waitingNumber) {
        if (adapter != null) adapter.updateWaitingNumber(waitingNumber);
    }

    private void attachDebounce() {
        waitingPublishSubject = PublishSubject.create();
        pendingPlayerPublishSubject = PublishSubject.create();
        setupDebounce();
    }

    private void detachDebounce() {
        if (waitingPublishSubject != null) waitingPublishSubject.onComplete();
        if (pendingPlayerPublishSubject != null) pendingPlayerPublishSubject.onComplete();
    }

    private void setupDebounce() {
        waitingPublishSubject
                .throttleLatest(30, TimeUnit.SECONDS, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResultsItem>() {
                    @Override
                    public void onNext(ResultsItem s) {
                        mViewModel.loadPlayRequest(false, true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        pendingPlayerPublishSubject
                .throttleLatest(30, TimeUnit.SECONDS, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResultsItem>() {
                    @Override
                    public void onNext(ResultsItem s) {
                        if (adapter != null && mViewModel != null && (
                                (mViewModel.getPollUrl() == null || adapter.getItemCount() <= 11))) {
                            mViewModel.fetchPendingPlayRequest();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public void setupViews() {
//        count = 0;
        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_REQUEST_TO_PLAY);

        if (currentPost != null) {
            adapter.setListener(this);
            adapter.setGame(currentPost.getGame());
            mBinding.historyRecyclerView.setAdapter(adapter);
            mBinding.setViewModel(mViewModel);
            userId = CommonUtils.getUserName(getContext());
            mViewModel.postId = currentPost.getId();

//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (!isAdded() || !isResumed() || isDetached() || !isVisible()) return;
//                    if (adapter == null) {
//                        new Handler(Looper.getMainLooper()).postDelayed(this, 15000);
//                    }
//                    ResultsItem item = getItem();
//                    item.setToPost(currentPost.getId());
//                    count++;
//                    if (count % 3 == 1) {
//                        item.setState("ACCEPTED");
//                    } else if (count % 3 == 2) {
//                        item.setState("ACCEPTED");
//                    } else if (count % 3 == 0) {
//                        item.setState("ACCEPTED");
//                    }
//                    EventBus.getDefault().post(item);
//                    new Handler(Looper.getMainLooper()).postDelayed(this, 15000);
//                }
//            }, 15000);
            mBinding.historyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy < 0) return;
                    LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                    if (linearLayoutManager == null) return;
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    if (!adapter.isPaginating() && !mViewModel.getLoading() && mViewModel.getNext() != null &&
                            (visibleItemCount + firstVisibleItemPosition) >= adapter.getItemCount() - 3
                            && firstVisibleItemPosition >= 0) {
                        adapter.setPaginating(true);
                        mViewModel.loadPlayRequest(false, false);
                    }
                }
            });
            baseProperties.put("postId", currentPost.getId());
            baseProperties.put("userId", userId);
            baseProperties.put("language", currentPost.getLanguage());
            baseProperties.put("authorId", getAuthorId());
            baseProperties.put("author", getAuthorName());
            baseProperties.put("game", currentPost.getGame());
            baseProperties.put("custom_room_enabled", currentPost.isCustomRoomEnabled());
            baseProperties.put("play_request_enabled", currentPost.canRequestPlay());


            if (CommonUtils.isUserLoggedin()) {
                if (currentPost.getIsLive()) {
                    if (currentPost.isCustomRoomEnabled()) {
                        mViewModel.loadPlayRequest(true, false);
                        mBinding.errorTextView.setVisibility(View.GONE);
                        mBinding.refreshBtn.setVisibility(View.VISIBLE);
                        mBinding.refreshBtn.setOnClickListener(view -> mViewModel.refreshPlayRequest());
                    } else if (currentPost.canRequestPlay()) {
                        mViewModel.loadPlayRequest(true, false);
                        mBinding.errorTextView.setVisibility(View.GONE);
                    } else {
                        mViewModel.setLoading(false);
                        mBinding.errorTextView.setVisibility(View.VISIBLE);
                        mBinding.errorTextView.setText(getString(R.string.cannot_request_play_message));
                    }
                } else {
                    mViewModel.setLoading(false);
                    mBinding.errorTextView.setVisibility(View.VISIBLE);
                    mBinding.errorTextView.setText(getString(R.string.request_play_non_live_message));
                }
                mBinding.loginButton.setVisibility(View.GONE);
            } else {
                mViewModel.setLoading(false);
                mBinding.errorTextView.setVisibility(View.GONE);
                mBinding.loginButton.setVisibility(View.VISIBLE);
                mBinding.infoImageView.setVisibility(View.GONE);
                mBinding.loginButton.setText(getString(R.string.login_to_play_message, getAuthorName()));
            }
            if (currentPost.isCustomRoomEnabled()) {
                if (currentPost.getCustomRoomDetailUrl() != null && !currentPost.getCustomRoomDetailUrl().isEmpty()) {
                    mBinding.infoImageView.setVisibility(View.VISIBLE);
                    mBinding.infoImageView.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), WebviewActivity.class);
                        intent.putExtra("URL", currentPost.getCustomRoomDetailUrl());
                        startActivity(intent);
                    });
                } else {
                    mBinding.infoImageView.setVisibility(View.GONE);
                }

            } else {
                mBinding.infoImageView.setVisibility(View.VISIBLE);
                mBinding.infoImageView.setOnClickListener(v -> {

                    Intent intent = new Intent(getContext(), WebviewActivity.class);
                    intent.putExtra("URL", "https://www.rheotv.com/play-request-instructions/");
                    startActivity(intent);
                });
            }

        } else {
            mBinding.errorTextView.setVisibility(View.VISIBLE);
            mBinding.errorTextView.setText(getString(R.string.request_play_non_live_message));
        }

        mBinding.loginButton.setOnClickListener(v -> {
            if (getActivity() instanceof PlayerActivity && !CommonUtils.isUserLoggedin())
                ((PlayerActivity) getActivity()).openLoginFlow();
        });

        mViewModel.showLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                mBinding.loader.setVisibility(View.VISIBLE);
            } else {
                mBinding.loader.setVisibility(View.GONE);
            }
        });
    }

    public void trackEvent(String event, HashMap<String, Object> properties) {
        tracker.trackEvent(event, properties);
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_request_play;
    }

    @Override
    public RequestPlayViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(RequestPlayViewModel.class);
        mViewModel.setNavigator(this);
        return mViewModel;
    }

    @Override
    public void addPlayers(RequestPlayResponse response) {
        if (response == null || getContext() == null || !isAdded()) return;

        mViewModel.customRoomWinnerUsername = getWinnerUserNameforCustomRoom(response.getResults());
        mViewModel.setCurrentRoomRheoCoin(response.getCoinsRequired());
        mBinding.customRoomNoRequestTextView.setVisibility(View.GONE);
        mBinding.errorTextView.setVisibility(View.GONE);
        mViewModel.isCustomRoom.set(currentPost.isCustomRoomEnabled());
        List<ResultsItem> players = response.getResults();
        String postUserName = getAuthorName();

        adapter.setCoinAndQueuePosition(response.getCoinsRequired(), response.getWaitingNumber(), postUserName, response.getGamerUserName(), currentPost.isCustomRoomEnabled());
        if (currentPost.isCustomRoomEnabled()) {
            if (response.getCustomRoomUsername() != null && !response.getCustomRoomUsername().isEmpty() && response.getCustomRoomPassword() != null && !response.getCustomRoomPassword().isEmpty()) {
                adapter.setCustomRoomDetails(response.getCustomRoomUsername(), response.getCustomRoomPassword());
            }
        }
        isMe = CommonUtils.getUserName(getContext()).equals(postUserName);

        if (players.isEmpty()) {
            if (response.isIsAllowedToRequest() || currentPost.isCustomRoomEnabled()) {
                ResultsItem item = new ResultsItem();
                item.setId(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                item.setType(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                players.add(item);
                if (isMe) {
                    if (response.isCustomRoomEnabled()) {
                        mBinding.customRoomNoRequestTextView.setVisibility(View.VISIBLE);
                        mBinding.customRoomNoRequestTextView.setText(getString(R.string.custom_room_request_empty_message));
                    } else {
                        mBinding.customRoomNoRequestTextView.setVisibility(View.GONE);
                    }
                } else {
                    mBinding.customRoomNoRequestTextView.setVisibility(View.GONE);
                }
                mBinding.errorTextView.setVisibility(View.GONE);
            } else {
                mBinding.errorTextView.setVisibility(View.VISIBLE);
                if (isMe) {
                    mBinding.errorTextView.setText(getString(R.string.player_request_message));
                } else {
                    mBinding.errorTextView.setText(getString(R.string.not_allow_to_request));
                }
            }
        } else {
            ResultsItem first = null;

            if (isMe) {
                for (ResultsItem item : players) {
                    if (item.getState().equals(getString(R.string.state_pending)))
                        item.setType(AppConstants.PLAY_VIEW_TYPE_PENDING);
                    else
                        item.setType(AppConstants.PLAY_VIEW_TYPE_REQUESTED);
                }
                first = players.get(0);
                if (currentPost.isCustomRoomEnabled() && !adapter.isPaginating()) {
                    ResultsItem item = new ResultsItem();
                    item.setId(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                    item.setType(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                    players.add(0, item);
                }
            } else {
                for (ResultsItem item : players) {
                    item.setType(AppConstants.PLAY_VIEW_TYPE_PENDING);
                }
                first = players.get(0);
            }

            String currentUserId = first.getFromUserProfile().getUser().getUsername();
            if (userId.equals(currentUserId)) {
                if (first.getState().equalsIgnoreCase(getString(R.string.state_accepted))) {
                    if (currentPost.isCustomRoomEnabled()) {
                        ResultsItem item = new ResultsItem();
                        item.setId(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                        item.setType(AppConstants.CUSTOM_ROOM_VIEW_TYPE_REQUEST_ACCEPTED);
                        item.setState("ACCEPTED");
                        players.add(0, item);
                    } else {
                        first.setType(AppConstants.PLAY_VIEW_TYPE_REQUESTED);
                    }
                } else {
                    first.setType(AppConstants.PLAY_VIEW_TYPE_REQUESTED);
                }
            } else if (!isMe && !adapter.isPaginating()) {
                ResultsItem item = new ResultsItem();
                item.setId(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                item.setType(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                players.add(0, item);
            }

            if (mBinding.errorTextView.getVisibility() == View.VISIBLE)
                mBinding.errorTextView.setVisibility(View.GONE);
        }

        adapter.addPlayers(players);
    }

    private String getWinnerUserNameforCustomRoom(List<ResultsItem> results) {
        String winnerUsername = null;
        for (ResultsItem item : results) {
            if (item.isWinner()) {
                winnerUsername = item.getFromUserProfile().getUser().getUsername();
            }
        }
        return winnerUsername;
    }

    private int pendingCount(ArrayList<ResultsItem> items) {
        int count = 0;
        for (ResultsItem item : items) {
            if (item.getState().equals(AppConstants.STATUS_PENDING))
                count += 1;
        }
        return count;
    }

    private int getAuthorId() {
        return currentPost != null && currentPost.getAuthor() != null && currentPost.getAuthor().getUser() != null && currentPost.getAuthor().getUser().getId() != null ? currentPost.getAuthor().getUser().getId() : -1;
    }

    private String getAuthorName() {
        return currentPost != null && currentPost.getAuthor() != null && currentPost.getAuthor().getUser() != null && currentPost.getAuthor().getUser().getUsername() != null ? currentPost.getAuthor().getUser().getUsername() : "StreamerObject";
    }

    public void addPlayer(ResultsItem response) {
        List<ResultsItem> players = new ArrayList<>();
        boolean me = CommonUtils.getUserName(getContext()).equals(getAuthorName());
        players.add(response);

        if (me) {
            for (ResultsItem item : players) {
                if (item.getState().equals(getString(R.string.state_pending)))
                    item.setType(AppConstants.PLAY_VIEW_TYPE_PENDING);
                else
                    item.setType(AppConstants.PLAY_VIEW_TYPE_REQUESTED);
            }
            if (currentPost.isCustomRoomEnabled()) {
                mBinding.customRoomNoRequestTextView.setVisibility(View.GONE);
                if (!adapter.isPaginating()) {
                    ResultsItem item = new ResultsItem();
                    item.setId(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                    item.setType(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                    players.add(0, item);
                }
            }
        } else {
            for (ResultsItem item : players) {
                item.setType(AppConstants.PLAY_VIEW_TYPE_PENDING);
            }
        }

        String currentUserId = response.getFromUserProfile().getUser().getUsername();
        if (userId.equals(currentUserId)) {
            if (response.getState().equalsIgnoreCase(getString(R.string.state_accepted))) {
                if (currentPost.isCustomRoomEnabled()) {
                    if (!response.isWinner()) {
                        ResultsItem resultsItem = new ResultsItem();
                        resultsItem.setId(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
                        resultsItem.setType(AppConstants.CUSTOM_ROOM_VIEW_TYPE_REQUEST_ACCEPTED);
                        resultsItem.setState(getString(R.string.state_accepted));
                        resultsItem.setFromUserProfile(response.getFromUserProfile());
                        players.add(0, resultsItem);
                    }
                } else
                    response.setType(AppConstants.PLAY_VIEW_TYPE_REQUESTED);
            } else {
                response.setType(AppConstants.PLAY_VIEW_TYPE_REQUESTED);
            }

        } else if (!me && !adapter.isPaginating()) {
            ResultsItem item = new ResultsItem();
            item.setId(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
            item.setType(AppConstants.PLAY_VIEW_TYPE_REQUEST_NOW);
            players.add(0, item);
        }
        if (adapter.updatePlayer(players) && mBinding.errorTextView.getVisibility() == View.VISIBLE)
            mBinding.errorTextView.setVisibility(View.GONE);
    }

    @Override
    public void onPlayRequest(String gameUserName) {
        mViewModel.requestToPlay(gameUserName);

        HashMap<String, Object> properties = new HashMap<>(this.baseProperties);
        properties.put("gameUserName", gameUserName);

        if (CommonUtils.isFirstRequestToPlayNotTracked()) {
            CommonUtils.setFirstRequestToPlayEventTracked();
            trackEvent(EVENT_FIRST_PLAY_REQUEST, properties);
        }

        properties.put("is_first", CommonUtils.isFirstPlayRequest());
        properties.put("author", getAuthorName());
        properties.put("RheoCoinsSpent", mViewModel.getCurrentRoomRheoCoin());
        trackEvent(EVENT_PLAY_REQUEST, properties);
        CommonUtils.setFirstPlayRequest();

//        if (getActivity() != null && getActivity() instanceof StreamPlayerContainerFragment) {
//            ((StreamPlayerContainerFragment) getActivity()).sendUpdateCustomRoomFragmentMessageInChat();
//        }
    }

    @Override
    public void onAction(String requestId, String action, String userName, String gameUserName, String profileUrl) {
        if (action.equals(AppConstants.PLAY_REQUEST_REJECT)) {
            mViewModel.requestAction(requestId, action);
            HashMap<String, Object> properties = new HashMap<>(this.baseProperties);
            properties.put("requestId", requestId);
            trackEvent(EVENT_PLAY_REQUEST_ACTION_REJECT, properties);

        } else if (action.equals(AppConstants.PLAY_REQUEST_ACCEPT)) {
            //mViewModel.requestAction(requestId, action);
            onPlayRequestAccept(requestId, action);
            //showAcceptDialog(requestId, userName, gameUserName, profileUrl, false);
        } else if (action.equals(AppConstants.PLAY_REQUEST_REFUND)) {
            showRefundDialog(requestId, userName, profileUrl);
        }
    }

    @Override
    public void onPlayerClick(String requestId, String userName, String gameUserName, String profileUrl, boolean isRequestAccepted) {
        if (isMe) {
            showAcceptDialog(requestId, userName, gameUserName, profileUrl, true, isRequestAccepted && mViewModel.customRoomWinnerUsername == null);
        }
    }

    @Override
    public void recordSegmentAction(String event) {
        if (SegmentConstants.EVENT_ADD_ANOTHER_CUSTOM_ROOM_DETAILS.equalsIgnoreCase(event)) {
            baseProperties.put("author", getAuthorName());
        }
        HashMap<String, Object> properties = new HashMap<>(this.baseProperties);
        trackEvent(event, properties);
    }

    @Override
    public void onSubmitCustomRoomDetailsClick(String roomId, String roomPass, boolean isEdit) {
        HashMap<String, Object> properties = new HashMap<>(this.baseProperties);

        properties.put("roomId", roomId);
        properties.put("is_editing_details", isEdit);
        properties.put("author", getAuthorName());
        trackEvent(EVENT_SUBMIT_CUSTOM_ROOM_DETAILS, properties);

        new AlertDialog.Builder(getContext(), R.style.AlertDialogDarkBackgroundStyle)
                .setTitle("Confirm?")
                .setMessage("Are you sure you want to save the details? Once saved it will be visible to all the viewers whose request you have accepted.")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    showLoading();
                    mViewModel.submitCustomRoomDetails(currentPost.getId(), roomId, roomPass, isEdit);
                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    private void showAcceptDialog(String requestId, String userName, String gameUserName, String profileUrl, boolean isViewOnly, boolean isRequestAccepted) {
        if (acceptDialog != null && (acceptDialog.isAdded() || acceptDialog.isVisible()))
            acceptDialog.dismiss();

        acceptDialog = RequestToPlayDialogFragment.newInstance(
                requestId, userName, currentPost.getGame(), gameUserName, profileUrl, isViewOnly, currentPost.isCustomRoomEnabled(), isRequestAccepted);
        acceptDialog.show(getChildFragmentManager(), AppConstants.REQUEST_ACCEPT_DIALOG, this);
    }

    private void showRefundDialog(String requestId, String userName, String profileUrl) {
        if (refundDialog != null && (refundDialog.isAdded() || refundDialog.isVisible()))
            refundDialog.dismiss();

        refundDialog = NotAbleToPlayDialogFragment.newInstance(requestId, userName, profileUrl);
        refundDialog.show(getChildFragmentManager(), AppConstants.REQUEST_ACCEPT_DIALOG, this);
    }

    @Override
    public void onPlayRequestAccept(String requestId, String action) {
        mViewModel.requestAction(requestId, action);
        HashMap<String, Object> properties = new HashMap<>(this.baseProperties);
        properties.put("requestId", requestId);
        trackEvent(EVENT_PLAY_REQUEST_ACTION_ACCEPT, properties);

    }

    @Override
    public void onWinnerSelected(String requestId) {
        mViewModel.submitCustomRoomWinner(requestId);
    }

    @Override
    public void handleActionSuccessResponse(String requestId, String action) {
        adapter.updateState(requestId, action);
        if (action.equals(AppConstants.PLAY_REQUEST_REJECT)) {
            Toast.makeText(getActivity(), "Request rejected successfully", Toast.LENGTH_SHORT).show();
        } else if (action.equals(AppConstants.PLAY_REQUEST_ACCEPT)) {
            Toast.makeText(getActivity(), "Request accepted successfully", Toast.LENGTH_SHORT).show();
        } else if (action.equals(AppConstants.PLAY_REQUEST_REFUND)) {
            Toast.makeText(getActivity(), "Refund completed successfully", Toast.LENGTH_SHORT).show();
        }
        if (adapter.getItemCount() == 0) {
            mBinding.errorTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefundAction(String requestId, String action) {
        mViewModel.requestAction(requestId, action);
        HashMap<String, Object> properties = new HashMap<>(this.baseProperties);
        properties.put("requestId", requestId);
        trackEvent(EVENT_PLAY_REQUEST_ACTION_REFUND, properties);
    }

    @Override
    public void handleErrorResponse(String message) {
        if (!isAdded()) return;
        if (message == null)
            message = getString(R.string.error_message);
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleErrorResponse() {
        if (!isAdded()) return;
        Toast.makeText(getContext(), getString(R.string.error_message), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        attachDebounce();
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        detachDebounce();
        super.onStop();
    }

    @Override
    public void handleSubmitCustomRoomDetailsError(String error) {
        hideLoading();
    }

    @Override
    public void handleSubmitCustomRoomDetailsSuccess(String customRoomUsername, String customRoomPassword) {
        hideLoading();
        adapter.setCustomRoomDetails(customRoomUsername, customRoomPassword);
        mViewModel.loadPlayRequest(true, false);
    }

    @Override
    public void onRoomDetailsCopied(String gameName, boolean isRoomId) {
        String packageName = null;
        switch (gameName) {
            case AppConstants.GAME_NAME_PUBG_MOBILE:
                packageName = "com.tencent.ig";
                break;
            case AppConstants.GAME_NAME_COD:
                packageName = "com.activision.callofduty.shooter";
                break;
            case AppConstants.GAME_NAME_FREE_FIRE:
                packageName = "com.dts.freefireth";
                break;
            case AppConstants.GAME_NAME_PUBG_LITE:
                packageName = "com.tencent.iglite";
                break;
        }
        if (packageName != null)
            checkInstalledPackage(packageName, gameName, isRoomId);
    }

    private void checkInstalledPackage(String packageName, String gameName, boolean isRoomId) {
        if (DownloadShareManager.isAppInstalled(packageName)) {

            new AlertDialog.Builder(getActivity(), R.style.AlertDialogDarkBackgroundStyle)
                    .setTitle("Open " + gameName)
                    .setMessage((isRoomId ? "Room Id" : "Room password") + " has been copied")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        try {
                            Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        }
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
    public interface OnRequestToPlayFragmentInteractionListener {

        Result getCurrentPostResult();

        void onUpdatePlayerCountBadge(int count);

    }
}
