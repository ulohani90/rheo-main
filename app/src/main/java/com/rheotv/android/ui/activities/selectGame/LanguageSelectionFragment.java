package com.rheotv.android.ui.activities.selectGame;


import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.onboarding.OnBoardingResponse;
import com.rheotv.android.databinding.FragmentLanguageSelectionBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.recyclerdecorators.GridItemDecoration;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class LanguageSelectionFragment extends BaseFragment<FragmentLanguageSelectionBinding, LanguageSelectionViewModel> implements LanguageSelectionAdapter.LanguageInteractionListener {
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    private FragmentLanguageSelectionBinding mBinding;
    private LanguageSelectionViewModel mViewModel;
    private LanguageSelectionListener mListener;
    private LanguageSelectionAdapter adapter;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static LanguageSelectionFragment getInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        LanguageSelectionFragment fragment = new LanguageSelectionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_language_selection;
    }

    @Override
    public LanguageSelectionViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(LanguageSelectionViewModel.class);
        mViewModel.fetchLanguage();
        mViewModel.boardingResponse.observe(this, this::setOnBoardingData);
        mViewModel.updatingLanguage.observe(this, status -> {
            if (status == Status.SUCCESS) {
                mListener.onLanguageUpdated();
                SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_ONBOARD_LANGUAGE_SELECTED, baseProperties);
            } else if (status == Status.ERROR) {
                Toast.makeText(getContext(), "Language Selection failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        adapter = new LanguageSelectionAdapter(new ArrayList<>());
        adapter.setListener(this);

        GridItemDecoration itemDecorator = new GridItemDecoration((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()), 3);
        mBinding.languageRecyclerView.addItemDecoration(itemDecorator);
        mBinding.languageRecyclerView.setAdapter(adapter);

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_LANGUAGE_SELECTION);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_LANGUAGE_SELECTION, baseProperties);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LanguageSelectionListener) {
            mListener = (LanguageSelectionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setOnBoardingData(OnBoardingResponse response) {
//        Log.i(getClass().getSimpleName(), "setOnBoardingData" + new Gson().toJson(response.getLanguageObjects()));
        adapter.submitList(response.getLanguageObjects());
    }

    @Override
    public void onLanguageItemClicked(Map<String, String> selectedLanguage) {
        mViewModel.selectedIds = new ArrayList<>(selectedLanguage.keySet());
        mViewModel.selectedLanguage = selectedLanguage;
    }

    public interface LanguageSelectionListener {
        void onLanguageUpdated();
    }
}
