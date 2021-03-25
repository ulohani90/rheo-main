package com.rheotv.android.ui.activities.story;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.rheotv.android.data.network.models.useProfile.responses.ProfileResult;

import java.util.ArrayList;
import java.util.List;

public class StoryViewPagerAdapter extends FragmentStateAdapter {

    private List<StoryPagerFragment> mList;

    StoryViewPagerAdapter(@NonNull FragmentManager fm, Lifecycle lifecycle, List<ProfileResult> profiles) {
        super(fm, lifecycle);
        mList = new ArrayList<>();
        addListItem(profiles);
    }

    public StoryPagerFragment getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public void addListItem(List<ProfileResult> list) {
        for (ProfileResult profileResult : list) {
            mList.add(StoryPagerFragment.newInstance(profileResult.getId()));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryPagerFragment createFragment(int position) {
        return mList == null ? StoryPagerFragment.newInstance("") : mList.get(position);
    }
}

