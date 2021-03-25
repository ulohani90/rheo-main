package com.rheotv.android.ui.activities.selectGame;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.rheotv.android.BR;
import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentGameSelectionBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.recyclerdecorators.GridItemDecoration;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameSelectionFragment extends BaseFragment<FragmentGameSelectionBinding, GameSelectionViewModel> {
    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    GameSelectionAdapter adapter;

    private FragmentGameSelectionBinding mBinding;
    private GameSelectionViewModel mViewModel;
    private GameSelectionListener mListener;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static GameSelectionFragment newInstance(String source) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        GameSelectionFragment fragment = new GameSelectionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_game_selection;
    }

    @Override
    public GameSelectionViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(GameSelectionViewModel.class);
        mViewModel.loadGameDetails();
        mViewModel.gameResults.observe(this, list -> adapter.submitList(list));
        mViewModel.submitting.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (mViewModel.submitting.get() == Status.SUCCESS && mListener != null) {
                    SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_ONBOARD_GAME_SELECTED, baseProperties);
                    mListener.onGameUpdated();
                }
            }
        });
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);
        GridItemDecoration itemDecorator = new GridItemDecoration((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()), 3);
        adapter.setListener(mViewModel::setSelectedGame);
        mBinding.gameRecyclerView.addItemDecoration(itemDecorator);
        mBinding.gameRecyclerView.setAdapter(adapter);

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));

        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_ONBOARD_GAME_SELECTION);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GameSelectionListener) {
            mListener = (GameSelectionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface GameSelectionListener {
        void onGameUpdated();
    }

}
