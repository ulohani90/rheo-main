package com.rheotv.android.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.databinding.ListItemAddedStoryBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.story.Constants;
import com.rheotv.story.model.Story;

import java.util.ArrayList;

import static com.rheotv.android.utils.AppConstants.MAX_STORY_LIMIT;

public class AddedStoryAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private ArrayList<Story> mList;
    private AddedStoryInteractionListener mListener;
    private int selectedPosition = -1;

    public AddedStoryAdapter(AddedStoryInteractionListener mListener, ArrayList<Story> list) {
        this.mList = list;
        this.mListener = mListener;
        this.mList.add(new Story(Constants.ADD_MORE));
    }

    public AddedStoryAdapter(AddedStoryInteractionListener mListener) {
        this.mList = new ArrayList<>();
        this.mListener = mListener;
        this.mList.add(new Story(Constants.ADD_MORE));
    }

    public void submitList(ArrayList<Story> list) {
        this.mList = list;
    }

    public void addItems(ArrayList<Story> story) {
        try {
            this.mList.addAll(mList.size() - 1, story);
            selectedPosition = mList.size() - 2;
            if (getItemCount() - 1 > MAX_STORY_LIMIT) {
                mList.remove(getItemCount() - 1);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addItem(Story story) {
        try {
            this.mList.add(mList.size() - 1, story);
            notifyItemInserted(mList.size() - 2);

            selectedPosition = mList.size() - 2;
            if (getItemCount() > MAX_STORY_LIMIT) {
                mList.remove(getItemCount() - 1);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Story removeItem(Story story) {
        try {
            Story nextStory;
            int index = mList.indexOf(story);
            if (index == 0 && mList.size() <= 2) {
                nextStory = null;
                selectedPosition = -1;
            } else if (index == mList.size() - 1) {
                selectedPosition = mList.size() - 2;
                nextStory = mList.get(selectedPosition);
            } else if (mList.get(index + 1).getType().equalsIgnoreCase(Constants.ADD_MORE)) {
                selectedPosition = mList.size() - 3;
                nextStory = mList.get(selectedPosition);
            } else {
                nextStory = mList.get(mList.indexOf(story) + 1);
            }

            mList.remove(story);
            if (mList.size() < MAX_STORY_LIMIT && !mList.get(mList.size() - 1).getType().equalsIgnoreCase(Constants.ADD_MORE))
                mList.add(new Story(Constants.ADD_MORE));
            notifyDataSetChanged();
            return nextStory;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addListener(AddedStoryInteractionListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemAddedStoryBinding binding = ListItemAddedStoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ClipViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ClipViewHolder extends BaseViewHolder {
        ListItemAddedStoryBinding binding;

        ClipViewHolder(ListItemAddedStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            Context context = binding.getRoot().getContext();

            Story story = mList.get(position);
            binding.setStory(story);
            binding.setShowIndicator(position == selectedPosition);
            if (story.getType().equals(Constants.ADD_MORE)) {
                binding.thumbnailImageView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_default_accent));
            }

            binding.getRoot().setOnClickListener(v -> {
                Log.i(getClass().getSimpleName(), "media_item_is: " + story.getType() + " path " + story.getUrl());
                mListener.onAddMoreClick(story);
                if (!story.getType().equals(Constants.ADD_MORE)) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });
            binding.executePendingBindings();
        }
    }

    public interface AddedStoryInteractionListener {
        void onAddMoreClick(Story story);
    }
}
