package com.rheotv.android.ui.activities.chatActivity;



import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.postlisting.responses.support.ChatModel;
import com.rheotv.android.databinding.ItemPostEmptyBinding;
import com.rheotv.android.databinding.ItemSupportChatBinding;
import com.rheotv.android.ui.activities.tabcontainer.posts.PostEmptyItemViewModel;
import com.rheotv.android.ui.adapters.PostListAdapter;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SharedPrefsUtils;

import java.util.List;

public class ChatFragmentAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<ChatModel> mPostList;

    private PostListAdapter.BlogAdapterListener mListener;

    private boolean isLoading = true;
    private SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();


    public ChatFragmentAdapter(List<ChatModel> mPostList) {
        this.mPostList = mPostList;
    }

    @Override
    public int getItemCount() {
        if (mPostList != null && mPostList.size() > 0) {
            Log.d("POSTLISTADAPTER", mPostList.size() + " size ki list");
            return mPostList.size();
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mPostList != null && !mPostList.isEmpty()) {
            return AppConstants.VIEW_TYPE_CHAT_ITEM;
        } else {
            return AppConstants.VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case AppConstants.VIEW_TYPE_CHAT_ITEM:
                ItemSupportChatBinding itemSupportChatBinding = ItemSupportChatBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new ChatFragmentAdapter.ChatItemViewHolder(itemSupportChatBinding);

            default:
                ItemPostEmptyBinding emptyBinding = ItemPostEmptyBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new ChatFragmentAdapter.EmptyViewHolder(emptyBinding);
        }
    }

    public void addItems(List<ChatModel> mPostList) {
        this.isLoading = mPostList != null && mPostList.size() > 0;
        this.mPostList.addAll(mPostList);
        notifyDataSetChanged();
    }

    public void clearItems() {
        mPostList.clear();
    }

    public void setListener(PostListAdapter.BlogAdapterListener listener) {
        this.mListener = listener;
    }

    public class ChatItemViewHolder extends BaseViewHolder {

        private ItemSupportChatBinding mBinding;

        private SupportChatItemViewModel chatItemViewModel;


        public ChatItemViewHolder(ItemSupportChatBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            final ChatModel result = mPostList.get(position);
            chatItemViewModel = new SupportChatItemViewModel(result);

            mBinding.setViewModel(chatItemViewModel);

            if (result.getSupportExecutive() == null) {
                mBinding.chatText.setVisibility(View.VISIBLE);
                mBinding.chatTextServer.setVisibility(View.GONE);
                mBinding.chatText.setText(result.getMessage());
            } else {
                mBinding.chatText.setVisibility(View.GONE);
                mBinding.chatTextServer.setVisibility(View.VISIBLE);
                mBinding.chatTextServer.setText(result.getMessage());
            }

            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();
        }
    }

    public class EmptyViewHolder extends BaseViewHolder implements PostEmptyItemViewModel.PostEmptyItemViewModelListener {
        private ItemPostEmptyBinding mBinding;

        public EmptyViewHolder(ItemPostEmptyBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            PostEmptyItemViewModel emptyItemViewModel = new PostEmptyItemViewModel(this);
            if (isLoading) {
                mBinding.linearLayoutView.setVisibility(View.GONE);
            } else {
                mBinding.linearLayoutView.setVisibility(View.VISIBLE);
            }
            mBinding.setViewModel(emptyItemViewModel);
        }

        @Override
        public void onRetryClick() {
            mListener.onRetryClick();
        }
    }
}