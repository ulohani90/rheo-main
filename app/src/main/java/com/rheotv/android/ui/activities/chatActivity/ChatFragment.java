package com.rheotv.android.ui.activities.chatActivity;

import android.content.Context;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.support.ChatModel;
import com.rheotv.android.databinding.ActivityChatBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SharedPrefsUtils;

import java.util.Objects;

import javax.inject.Inject;

public class ChatFragment extends BaseFragment<ActivityChatBinding, ChatFragmentViewModel>
        implements ChatFragmentNavigator {

    @Inject
    ChatFragmentAdapter chatFragmentAdapter;
    Context context;
    ActivityChatBinding activityChatBinding;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private ChatFragmentViewModel chatFragmentViewModel;
    public SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    public static ChatFragment newInstance(String id, String gameId) {
        Bundle args = new Bundle();
        args.putString(AppConstants.SEE_ALL_TYPE, id);
        args.putString(AppConstants.SEE_ALL_TYPE_ID, gameId);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    public ChatFragmentViewModel getViewModel() {
        chatFragmentViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ChatFragmentViewModel.class);
        return chatFragmentViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatFragmentViewModel.setNavigator(this);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activityChatBinding = getViewDataBinding();
        setUp();
        subscribeToLiveData();
    }

    private void setUp() {
        String game = getArguments() != null && getArguments().getString(AppConstants.SEE_ALL_TYPE) != null ? getArguments().getString(AppConstants.SEE_ALL_TYPE) : "";
        String gameId = getArguments() != null && getArguments().getString(AppConstants.SEE_ALL_TYPE_ID) != null ? getArguments().getString(AppConstants.SEE_ALL_TYPE_ID) : "";
        if (TextUtils.isEmpty(game)) {
            Objects.requireNonNull(getActivity()).finish();
        }
        chatFragmentViewModel.fetchChatDetails(0, gameId);
        mLayoutManager = new LinearLayoutManager(context);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        activityChatBinding.blogRecyclerView.setLayoutManager(mLayoutManager);
        activityChatBinding.blogRecyclerView.setItemAnimator(new DefaultItemAnimator());
        activityChatBinding.blogRecyclerView.setAdapter(chatFragmentAdapter);
        activityChatBinding.refreshIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatFragmentViewModel.fetchChatDetails(0, gameId);
            }
        });
        activityChatBinding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(activityChatBinding.chatbox.getText().toString())) {
                    ChatModel chatModel = new ChatModel();
                    chatModel.setMessage(activityChatBinding.chatbox.getText().toString());
                    chatFragmentViewModel.addItemInFront(chatModel);
                    chatFragmentViewModel.postChatMessage(activityChatBinding.chatbox.getText().toString(), gameId);
                    activityChatBinding.chatbox.setText("");
                }
            }
        });
    }

    private void subscribeToLiveData() {
        chatFragmentViewModel.getBlogListLiveData().observe(this, blogs -> {
            activityChatBinding.progressBar.setVisibility(View.GONE);
            if (blogs != null && blogs.size() > 0) {
                chatFragmentViewModel.addBlogItemsToList(blogs);
            } else {
                activityChatBinding.blogRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void onError(Throwable throwable) {
        activityChatBinding.progressBar.setVisibility(View.GONE);
    }
}
