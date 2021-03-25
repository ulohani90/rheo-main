package com.rheotv.android.ui.fragments;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.TopStreamerObject;
import com.rheotv.android.data.network.models.onboarding.LanguageObject;
import com.rheotv.android.databinding.FragmentOnBoardingBinding;
import com.rheotv.android.ui.activities.selectGame.LanguageSelectionFragment;
import com.rheotv.android.ui.adapters.TopStreamersRVAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppUtils;

import java.util.List;

import javax.inject.Inject;

public class TopStreamerSelectionFragment extends BaseFragment<FragmentOnBoardingBinding, TopStreamerSelectionViewModel>
        implements TopStreamerFragmentNavigator, TopStreamersRVAdapter.OnItemSelectedListener {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    TopStreamerSelectionViewModel mViewModel;

    TopStreamersRVAdapter adapter;

    FragmentOnBoardingBinding mBinding;
    private LanguageSelectionFragment.LanguageSelectionListener mListener;

    public static TopStreamerSelectionFragment newInstance() {
        Bundle args = new Bundle();
        TopStreamerSelectionFragment fragment = new TopStreamerSelectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_on_boarding;
    }

    @Override
    public TopStreamerSelectionViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(TopStreamerSelectionViewModel.class);
        mViewModel.setNavigator(this);
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewState(mViewModel.viewStatus);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        mBinding.errorTextView.setOnClickListener(v -> {
            mViewModel.fetchLanguage();
            mViewModel.loadTopStreamersData(true);
        });
        mBinding.streamersRv.setLayoutManager(manager);
        mBinding.streamersRv.addItemDecoration(new StreamerRVDecoration((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics())));
        adapter = new TopStreamersRVAdapter();
        adapter.setItemSelectedListener(this);
        mBinding.streamersRv.setAdapter(adapter);
        updateButtonState();
        mBinding.actionButton.setOnClickListener(v -> {
            mViewModel.followSelectedUsers(adapter.getSelectedStreamers());
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (mListener != null)
                    mListener.onLanguageUpdated();
            }, 500);
        });
        mBinding.streamersRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = manager.getChildCount();
                int totalItemCount = manager.getItemCount();
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();

                // Load more if we have reach the end to the recyclerView
                if (adapter != null && !adapter.isPaginating() && mViewModel.getNextUrl() != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2 && firstVisibleItemPosition >= 0) {
                    Log.i("Next url ", mViewModel.getNextUrl());
                    adapter.setPaginating(true);
                    mViewModel.loadTopStreamersData(false);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.result.observe(getViewLifecycleOwner(), topStreamerObjects -> adapter.addTopStreamers(topStreamerObjects));
        mViewModel.languagesLiveData.observe(getViewLifecycleOwner(), languageObjects -> {
            if (languageObjects == null || languageObjects.isEmpty()) return;
            for (LanguageObject languageObject : languageObjects) {
                Chip chip = new Chip(mBinding.getRoot().getContext(), null, R.attr.chipChoiceStyle);
                chip.setText(languageObject.getText());
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    languageObject.setSelected(isChecked);
                    if (isChecked) {
                        mViewModel.selectedLanguages.add(languageObject.getId());
                    } else {
                        mViewModel.selectedLanguages.remove(languageObject.getId());
                    }
                    adapter.setRefreshing(true);
                    mViewModel.loadTopStreamersData(true);
                });
                mBinding.languageGroup.addView(chip);
            }
        });
        mViewModel.fetchLanguage();
        mViewModel.loadTopStreamersData(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LanguageSelectionFragment.LanguageSelectionListener) {
            mListener = (LanguageSelectionFragment.LanguageSelectionListener) context;
        }
    }

    @Override
    public void onItemSelected(TopStreamerObject topStreamerObject) {
        updateButtonState();
    }

    private void updateButtonState() {
        if (adapter != null) {
            mBinding.actionButton.setEnabled(adapter.getSelectedStreamers().size() >= 5);
            mBinding.actionButton.setText(mBinding.getRoot().getContext().getString(R.string.follow_top_streamers, adapter.getSelectedStreamers().size()));
        }
    }

    public class StreamerRVDecoration extends RecyclerView.ItemDecoration {

        int mSpacing;

        public StreamerRVDecoration(int spacing) {
            mSpacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position % 2 == 0) {
                outRect.left = mSpacing;
                outRect.top = mSpacing;
                outRect.right = mSpacing / 2;
            } else {
                outRect.right = mSpacing;
                outRect.left = mSpacing / 2;
                outRect.top = mSpacing;
            }
        }
    }
}
