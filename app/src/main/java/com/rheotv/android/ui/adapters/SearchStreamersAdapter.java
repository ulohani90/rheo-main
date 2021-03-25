package com.rheotv.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.objects.StreamerObject;
import com.rheotv.android.data.network.models.postlisting.responses.SearchItem;
import com.rheotv.android.databinding.ListItemTopStreamersBinding;
import com.rheotv.android.ui.activities.search.fragment.SearchFragmentAdapter;
import com.rheotv.android.ui.activities.tabcontainer.posts.OnFollowActionCompleteListener;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.segment.analytics.Properties;

import java.util.HashMap;
import java.util.List;

public class SearchStreamersAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<SearchItem> topStreamerObjects;
    private SearchFragmentAdapter.SearchItemSnippetClickListener mListener;
    private boolean isInTopSearch = false;

    public SearchStreamersAdapter(List<SearchItem> topStreamerObjects) {
        this.topStreamerObjects = topStreamerObjects;
    }

    public SearchStreamersAdapter(List<SearchItem> topStreamerObjects, SearchFragmentAdapter.SearchItemSnippetClickListener mListener) {
        this.topStreamerObjects = topStreamerObjects;
        this.mListener = mListener;
    }

    public SearchStreamersAdapter(List<SearchItem> topStreamerObjects, SearchFragmentAdapter.SearchItemSnippetClickListener mListener, boolean isInTopSearch) {
        this.topStreamerObjects = topStreamerObjects;
        this.mListener = mListener;
        this.isInTopSearch = isInTopSearch;
    }

    public void setListener(SearchFragmentAdapter.SearchItemSnippetClickListener mListener) {
        this.mListener = mListener;
    }

    public void submitItems(List<SearchItem> topStreamerObjects) {
        this.topStreamerObjects = topStreamerObjects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ListItemTopStreamersBinding binding = ListItemTopStreamersBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new StreamerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {
        baseViewHolder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return topStreamerObjects == null ? 0 : (isInTopSearch && topStreamerObjects.size() > 3 ? 3 : topStreamerObjects.size());
    }

    public class StreamerViewHolder extends BaseViewHolder {
        ListItemTopStreamersBinding mBinding;

        StreamerViewHolder(ListItemTopStreamersBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            SearchItem result = topStreamerObjects.get(position);
            mBinding.setStreamer(result);
            BindingUtils.setImageUrlCircular(mBinding.streamerThumbnailImageView, result.getUrl(), 54, 54);

            mBinding.getRoot().setOnClickListener(view -> {
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("username", result.getName());
                SegmentTracker.getInstance(mBinding.getRoot().getContext())
                        .trackEvent(SegmentConstants.EVENT_TOP_STREAMER_PROFILE_PIC_CLICK, properties);
                mListener.onItemClicked(result, AppConstants.TYPE_HORIZONTAL_IMAGE_TEXT_ITEMS);
            });
//            boolean isFollowed = result.isFollowed();

//            if (isFollowed) {
//                mBinding.followBtn.setBackground(mBinding.followBtn.getContext().getDrawable(R.drawable.follow_selected_bg));
//                mBinding.followBtn.setSelected(true);
//                mBinding.followBtn.setText("Following");
//                //mBinding.followBtn.setTextColor(mBinding.followBtn.getContext().getResources().getColor(R.color.white));
//            } else {
//                mBinding.followBtn.setBackground(mBinding.followBtn.getContext().getDrawable(R.drawable.follow_normal_bg));
//                mBinding.followBtn.setSelected(false);
//                mBinding.followBtn.setText("Follow");
//                //mBinding.followBtn.setTextColor(mBinding.followBtn.getContext().getResources().getColor(R.color.white));
//            }
//            mBinding.followBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    mListener.onFollowBtnClicked(result, result.isFollowed(), new OnFollowActionCompleteListener() {
//                        @Override
//                        public void onFollowActionComplete(boolean isFollowed) {
//                            if (!isFollowed) {
//                                mBinding.followBtn.setBackground(mBinding.followBtn.getContext().getDrawable(R.drawable.follow_normal_bg));
//                                mBinding.followBtn.setSelected(false);
//                                mBinding.followBtn.setText("Follow");
//                            } else {
//                                mBinding.followBtn.setBackground(mBinding.followBtn.getContext().getDrawable(R.drawable.follow_selected_bg));
//                                mBinding.followBtn.setSelected(true);
//                                mBinding.followBtn.setText("Following");
//                            }
//                            result.setFollowed(isFollowed);
//                        }
//                    });
//
//                }
//            });
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