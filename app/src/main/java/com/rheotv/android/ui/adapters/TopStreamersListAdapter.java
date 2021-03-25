package com.rheotv.android.ui.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.StreamerObject;
import com.rheotv.android.databinding.StreamerItemFollowLayoutBinding;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TopStreamersListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    List<StreamerObject> topStreamerObjects;

    OnTopStreamerItemClickListener mListener;


    public TopStreamersListAdapter() {
        topStreamerObjects = new ArrayList<>();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        StreamerItemFollowLayoutBinding binding = StreamerItemFollowLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StreamerFollowLayoutViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {
        baseViewHolder.onBind(position);
    }

    public void setData(List<StreamerObject> topStreamerObjects) {
        this.topStreamerObjects.clear();
        this.topStreamerObjects.addAll(topStreamerObjects);
        notifyDataSetChanged();
    }

    public void setListener(OnTopStreamerItemClickListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getItemCount() {
        return topStreamerObjects.size();
    }

    public class StreamerFollowLayoutViewHolder extends BaseViewHolder implements Serializable {
        StreamerItemFollowLayoutBinding mBinding;

        public StreamerFollowLayoutViewHolder(StreamerItemFollowLayoutBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            StreamerObject result = topStreamerObjects.get(position);
            BindingUtils.setImageUrlCircular(mBinding.userImg, result.getProfilePic(), 110, 140);
            mBinding.username.setText(result.getUsername());
            mBinding.followersCount.setText(result.getFollowersCountStr() + " followers");

            mBinding.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String, Object> properties = new HashMap<>();
                    properties.put("username", result.getUsername());
                    SegmentTracker.getInstance(mBinding.userImg.getContext())
                            .trackEvent(SegmentConstants.EVENT_TOP_STREAMER_PROFILE_PIC_CLICK, properties);
                    mListener.onProfileViewAction(result.getUsername(), onFollowActionCompleteListener);
                }
            });
            boolean isFollowed = result.isFollowed();
            android:

            if (isFollowed) {
                mBinding.followBtn.setBackground(mBinding.followBtn.getContext().getDrawable(R.drawable.follow_selected_bg));
                mBinding.followBtn.setSelected(true);
                mBinding.followBtn.setText("Following");
                //mBinding.followBtn.setTextColor(mBinding.followBtn.getContext().getResources().getColor(R.color.white));
            } else {
                mBinding.followBtn.setBackground(mBinding.followBtn.getContext().getDrawable(R.drawable.follow_normal_bg));
                mBinding.followBtn.setSelected(false);
                mBinding.followBtn.setText("Follow");
                //mBinding.followBtn.setTextColor(mBinding.followBtn.getContext().getResources().getColor(R.color.white));
            }
            mBinding.followBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onFollowBtnClicked(result, result.isFollowed(), new OnFollowActionCompleteListener() {
                        @Override
                        public void onFollowActionComplete(boolean isFollowed) {
                            if (!isFollowed) {
                                mBinding.followBtn.setBackground(mBinding.followBtn.getContext().getDrawable(R.drawable.follow_normal_bg));
                                mBinding.followBtn.setSelected(false);
                                mBinding.followBtn.setText("Follow");
                            } else {
                                mBinding.followBtn.setBackground(mBinding.followBtn.getContext().getDrawable(R.drawable.follow_selected_bg));
                                mBinding.followBtn.setSelected(true);
                                mBinding.followBtn.setText("Following");
                            }
                            result.setFollowed(isFollowed);
                        }
                    });

                }
            });
        }

    }

    OnFollowActionCompleteListener onFollowActionCompleteListener = new OnFollowActionCompleteListener() {
        @Override
        public void onFollowActionComplete(boolean isFollowed) {

        }
    };

    public interface OnTopStreamerItemClickListener {
        void onFollowBtnClicked(StreamerObject streamerObject, boolean isFollowed, OnFollowActionCompleteListener listener);

        void onProfileViewAction(String authorUsername, OnFollowActionCompleteListener listener);
    }


}
