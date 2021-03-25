package com.rheotv.android.ui.activities.profile.viewprofile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.StreamerData;
import com.rheotv.android.databinding.ListItemUserAnalyticsBinding;
import com.rheotv.android.ui.activities.tabcontainer.profile.analytics.AnalyticsGraphsAdapter;

import java.util.ArrayList;

public class UserAnalyticsAdapter extends PagerAdapter {
    private ArrayList<StreamerData> items = new ArrayList<>();

    public void submitList(ArrayList<StreamerData> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ListItemUserAnalyticsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(container.getContext()), R.layout.list_item_user_analytics, container, false);
        binding.setData(items.get(position));
        container.addView(binding.getRoot());
        binding.graphRv.setAdapter(new AnalyticsGraphsAdapter(items.get(position).getGraphObjects()));
        return binding.getRoot();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return this.items.get(position).getType();
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
