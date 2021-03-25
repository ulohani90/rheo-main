package com.rheotv.android.ui.activities.gamify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentRewardPagerBinding;
import com.rheotv.android.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RewardPagerFragment extends Fragment {

    public RewardPagerFragment() {
        // Required empty public constructor
    }

    private FragmentRewardPagerBinding mViewDataBinding;
    private String mSource;

    public static RewardPagerFragment newInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        RewardPagerFragment fragment = new RewardPagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(AppConstants.SCREEN_SOURCE))
                mSource = getArguments().getString(AppConstants.SCREEN_SOURCE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mViewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_reward_pager, container, false);
        return mViewDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewDataBinding.tabLayout.setupWithViewPager(mViewDataBinding.redeemViewPager);
        mViewDataBinding.redeemViewPager.setAdapter(new RedeemPageAdapter(getChildFragmentManager(), mSource));

    }

    private static class RedeemPageAdapter extends FragmentStatePagerAdapter {

        RedeemPageAdapter(@NonNull FragmentManager fragmentManager, String source) {
            super(fragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//            mList.add(RewardRedeemFragment.newInstance(source));
            mList.add(RewardGiveawayFragment.newInstance(source));
        }

        private List<Fragment> mList = new ArrayList<>();

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mList.get(position);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "Giveaway";
            } else {
                return "Game";
            }
        }
    }
}
