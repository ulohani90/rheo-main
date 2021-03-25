package com.rheotv.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.RecentlyRedeemedObject;
import com.rheotv.android.databinding.ListItemRecentlyRedeemedBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RecentlyRedeemedListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    List<RecentlyRedeemedObject> mResults;

    public RecentlyRedeemedListAdapter(ArrayList<RecentlyRedeemedObject> results) {
        this.mResults = results;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRecentlyRedeemedBinding mBinding = ListItemRecentlyRedeemedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RecentlyRedeemedViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mResults != null ? mResults.size() : 0;
    }

    public void setRecentlyRedeemedObjects(List<RecentlyRedeemedObject> recentlyRedeemedObjects) {
        this.mResults = recentlyRedeemedObjects;
        notifyDataSetChanged();
    }


    public class RecentlyRedeemedViewHolder extends BaseViewHolder {
        ListItemRecentlyRedeemedBinding mBinding;

        public RecentlyRedeemedViewHolder(ListItemRecentlyRedeemedBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @Override
        public void onBind(int position) {
            RecentlyRedeemedObject recentlyRedeemedObject = mResults.get(position);
            mBinding.setRecentlyRedeemedObj(recentlyRedeemedObject);
            //BindingUtils.setProfileImageUrlRounded(mBinding.profilePic, recentlyRedeemedObject.getProfilePic());
        }
    }
}
