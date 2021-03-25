package com.rheotv.android.ui.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.databinding.PrimeStreamersCarouselItemBinding;
import com.rheotv.android.utils.AppUtils;
import com.rheotv.android.utils.BindingUtils;
import com.rheotv.android.utils.SharedPrefsUtils;
import com.rheotv.android.utils.TimeUtils;

import java.util.List;

public class SuperPrimeStreamersAdapter extends PagerAdapter {

    List<PostObject> results;

    PrimeStreamersCarouselItemBinding mBinding;

    SuperPrimeClickInterface mListener;

    SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();
    String title;

    public SuperPrimeStreamersAdapter(List<PostObject> results, String title) {
        this.results = results;
        this.title = title;
    }

    public void setListener(SuperPrimeClickInterface mListener) {
        this.mListener = mListener;
    }

    @Override
    public int getCount() {
        return results != null ? results.size() : 0;
    }

    @Override
    public float getPageWidth(int position) {

        if (results != null && position == results.size() - 1) {
            return 1.0f;
        }
        return 0.9f;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        PostObject result = results.get(position);
        mBinding = PrimeStreamersCarouselItemBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        BindingUtils.setImageUrlUsingCache(mBinding.itemThumbnail, result.getThumbnail(), true);
        String reminderSetIds = sharedPrefsUtils.getStringPreference(container.getContext(), SharedPrefsUtils.REMINDER_SET_POST_ID);
        if (TimeUtils.hasStreamNotStarted(result.getStartFrom())) {
            if (reminderSetIds != null) {
                String[] remindedPosts = reminderSetIds.split(",");
                if (AppUtils.hasPostId(remindedPosts, result.getId())) {
                    mBinding.remindMeBtn.setText("Reminder Set");
                } else {
                    mBinding.remindMeBtn.setText("Remind Me");
                }
            } else {
                mBinding.remindMeBtn.setText("Remind Me");
            }
            mBinding.remindMeBtn.setVisibility(View.VISIBLE);
            mBinding.streamStartText.setText(TimeUtils.getStreamStartText(result.getStartFrom()));


        } else {
            mBinding.remindMeBtn.setText("WATCH NOW");
            mBinding.streamStartText.setVisibility(View.INVISIBLE);
        }

        mBinding.remindMeBtn.setOnClickListener(view -> {
            if (((TextView) view).getText().toString().equalsIgnoreCase("Remind Me")) {
                mListener.onRemindMeClick(result);
                ((TextView) view).setText("Reminder Set");
                sharedPrefsUtils.setStringPreference(container.getContext(),
                        SharedPrefsUtils.REMINDER_SET_POST_ID, reminderSetIds != null ? reminderSetIds + "," + result.getId() : result.getId());
            } else if (((TextView) view).getText().toString().equalsIgnoreCase("Watch Now")) {

                mListener.onPostItemClick(result.getId(), results, result, title);
            }
        });
        mBinding.parent.setOnClickListener(view -> mListener.onPostItemClick(result.getId(), results, result, title));

        container.addView(mBinding.parent);
        return mBinding.parent;

    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(container);
    }

    public interface SuperPrimeClickInterface {
        void onRemindMeClick(PostObject result);

        void onPostItemClick(String id, List<PostObject> results, PostObject postObject, String title);
    }

}
