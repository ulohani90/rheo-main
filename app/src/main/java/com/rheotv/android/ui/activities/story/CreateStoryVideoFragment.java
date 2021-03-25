package com.rheotv.android.ui.activities.story;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.rheotv.android.R;
import com.rheotv.android.databinding.FragmentCreateStoryVideoBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.story.model.Story;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnVideoInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateStoryVideoFragment#getInstance(Story)} factory method to
 * create an instance of this fragment.
 */
public class CreateStoryVideoFragment extends BaseFragment<FragmentCreateStoryVideoBinding, CreateStoryVideoViewModel> {
    private static final String ARG_VIDEO = "video";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    CreateStoryVideoViewModel mViewModel;

    private FragmentCreateStoryVideoBinding mBinding;

    private OnVideoInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param video Parameter 1.
     * @return A new instance of fragment CreateStoryVideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateStoryVideoFragment getInstance(Story video) {
        CreateStoryVideoFragment fragment = new CreateStoryVideoFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_VIDEO, video);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();

        setupViews();
    }

    private void setupViews() {


        mBinding.moreButton.setOnClickListener(v -> mListener.onVideoMoreOptionClicked(mViewModel.video.get()));
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_create_story_video;
    }

    @Override
    public CreateStoryVideoViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(CreateStoryVideoViewModel.class);
        if (getArguments() != null) {
            mViewModel.video.set(getArguments().getParcelable(ARG_VIDEO));
        }
        return mViewModel;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVideoInteractionListener) {
            mListener = (OnVideoInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnVideoInteractionListener {
        // TODO: Update argument type and name
        void onVideoMoreOptionClicked(Story story);
    }


}
