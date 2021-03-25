package com.rheotv.android.ui.activities.tabcontainer.posts.multiviewitem;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.databinding.MultiviewRecyclerItemBinding;
import com.rheotv.android.databinding.MultiviewRecyclerItemFirstBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;

import java.util.List;

public class MultiViewRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<PostObject> mPostList;
    private MultiViewAdapterListener mListener;
    private int MAX_DISPLAY_ITEMS_COUNT = 5;

    public MultiViewRecyclerAdapter(List<PostObject> mPostList) {
        this.mPostList = mPostList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return AppConstants.VIEW_TYPE_FIRST;
        } else {
            return AppConstants.VIEW_TYPE_NON_FIRST;
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case AppConstants.VIEW_TYPE_FIRST:
                MultiviewRecyclerItemFirstBinding multiviewRecyclerItemFirstBinding = MultiviewRecyclerItemFirstBinding.inflate(LayoutInflater.from(viewGroup.getContext()),
                        viewGroup, false);
                return new MultiViewRecyclerItemFirstHolder(multiviewRecyclerItemFirstBinding);

            case AppConstants.VIEW_TYPE_NON_FIRST:
            default:
                MultiviewRecyclerItemBinding multiviewRecyclerItemBinding = MultiviewRecyclerItemBinding.inflate(LayoutInflater.from(viewGroup.getContext()),
                        viewGroup, false);
                return new MultiViewRecyclerItemHolder(multiviewRecyclerItemBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int i) {
        baseViewHolder.onBind(i);
    }

    @Override
    public int getItemCount() {
        if (mPostList.size() >= MAX_DISPLAY_ITEMS_COUNT) {
            return MAX_DISPLAY_ITEMS_COUNT;
        } else {
            return mPostList.size();
        }
    }

    public void setListener(MultiViewAdapterListener listener) {
        this.mListener = listener;
    }

    public interface MultiViewAdapterListener {
        void onMultiViewItemClick(String id);

        void onAuthorClicked(String id);
    }

    public void addItems(List<PostObject> mPostList) {
        this.mPostList.addAll(mPostList);
        notifyDataSetChanged();
    }

    public void clearItems() {
        mPostList.clear();
    }

    public class MultiViewRecyclerItemFirstHolder extends BaseViewHolder implements MultiViewRecyclerItemViewModel.MultiViewItemViewModelListener {

        private MultiviewRecyclerItemFirstBinding mBinding;
        private MultiViewRecyclerItemViewModel mViewModel;

        public MultiViewRecyclerItemFirstHolder(MultiviewRecyclerItemFirstBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @Override
        public void onItemClick(String id) {
            mListener.onMultiViewItemClick(id);
        }

        @Override
        public void onAuthodClicked(String id) {
            mListener.onAuthorClicked(id);
        }

        @Override
        public void onSeeMoreClicked() {

        }

        @Override
        public void onBind(int position) {
            PostObject result = mPostList.get(position);
            mViewModel = new MultiViewRecyclerItemViewModel(result, this);
            BindingUtils.setImageUrlUsingCache(mBinding.itemThumbnail, result.getThumbnail(), true);
            BindingUtils.setImageUrlUsingCache(mBinding.userProfilePic, result.getAuthor().getProfilePic(), true);
            mBinding.setViewModel(mViewModel);
            /*if(!result.getIsLive()){
                mBinding.liveTag.setVisibility(View.VISIBLE);
            }else{
                mBinding.liveTag.setVisibility(View.GONE);
            }*/

            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();
        }
    }

    public class MultiViewRecyclerItemHolder extends BaseViewHolder implements MultiViewRecyclerItemViewModel.MultiViewItemViewModelListener {

        private MultiviewRecyclerItemBinding mBinding;
        private MultiViewRecyclerItemViewModel multiViewRecyclerItemViewModel;

        public MultiViewRecyclerItemHolder(MultiviewRecyclerItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @Override
        public void onBind(int position) {
            PostObject result = mPostList.get(position);
            multiViewRecyclerItemViewModel = new MultiViewRecyclerItemViewModel(result, this);

            mBinding.itemTitle.setText(result.getTitle());

            BindingUtils.setImageUrlUsingCache(mBinding.itemThumbnail, result.getThumbnail(), true);
            BindingUtils.setImageUrlUsingCache(mBinding.userProfilePic, result.getAuthor().getProfilePic(), true);
            mBinding.setViewModel(multiViewRecyclerItemViewModel);

            // Immediate Binding
            // When a variable or observable changes, the binding will be scheduled to change before
            // the next frame. There are times, however, when binding must be executed immediately.
            // To force execution, use the executePendingBindings() method.
            mBinding.executePendingBindings();

        }

        @Override
        public void onItemClick(String id) {
            mListener.onMultiViewItemClick(id);
        }

        @Override
        public void onAuthodClicked(String id) {
            mListener.onAuthorClicked(id);
        }

        @Override
        public void onSeeMoreClicked() {

        }
    }
}
