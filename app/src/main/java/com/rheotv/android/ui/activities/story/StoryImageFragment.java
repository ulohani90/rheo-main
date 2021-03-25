package com.rheotv.android.ui.activities.story;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.emoji.text.EmojiCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.SearchSuggestionObject;
import com.rheotv.android.databinding.FragmentStoryImageBinding;
import com.rheotv.android.ui.adapters.StoryMentionAdapter;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;
import com.rheotv.story.Constants;
import com.rheotv.story.model.Story;
import com.rheotv.story.model.StoryCTAData;

import java.util.HashMap;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoryImageFragment extends BaseFragment<FragmentStoryImageBinding, StoryImageViewModel> {
    private static final String ARG_IMAGE = "image";

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    StoryImageViewModel mViewModel;

    private FragmentStoryImageBinding mBinding;

    private StoryMentionAdapter mentionAdapter;

    private OnStoryInteractionListener mListener;
    private Handler searchApiHitHandler;
    private long DELAY_SUGGESTIONS_CALL = 300;
    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static StoryImageFragment getInstance(Story story, String sourceScreen) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_IMAGE, story);
        bundle.putString(AppConstants.SCREEN_SOURCE, sourceScreen);
        StoryImageFragment fragment = new StoryImageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_story_image;
    }

    @Override
    public StoryImageViewModel getViewModel() {
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(StoryImageViewModel.class);
        if (getArguments() != null) {
            mViewModel.story.set(getArguments().getParcelable(ARG_IMAGE));
        }

        mViewModel.suggestionsLiveData.observe(this, list ->
                mentionAdapter.submitList(list)
        );
        return mViewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        mBinding.setViewModel(mViewModel);

        mentionAdapter = new StoryMentionAdapter();
        mentionAdapter.setListener(this::onMentionClicked);
        mBinding.addedUserRv.setAdapter(mentionAdapter);
        mBinding.shareBtn.setOnClickListener(v -> mListener.onStoryShareOptionClicked(mViewModel.story.get()));
        mBinding.deleteBtn.setOnClickListener(v -> {
            mListener.onStoryDeleteOptionClicked(mViewModel.story.get());
            recordDeleteClickEvent();
        });
        // mBinding.askInterestButton.setOnClickListener(v -> askInterest());

        showStoryView();

        try {
            if (Constants.TEXT.equalsIgnoreCase(mViewModel.story.get().getType())) {
                Story.TextStory textStory = new Gson().fromJson(mViewModel.story.get().getMetaData(), Story.TextStory.class);
                mBinding.imageView4.setVisibility(View.GONE);
                mBinding.placeHolderImageView.setBackgroundColor(Color.parseColor(textStory.getBackgroundColor()));
                mBinding.storyText.setText(textStory.getText());
                mBinding.storyText.setVisibility(View.VISIBLE);
            }
        } catch (JsonSyntaxException e) {
            mViewModel.story.get().setType(Constants.IMAGE);
            showStoryView();
            e.printStackTrace();
        }
        mBinding.mentionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null && editable.toString().trim().length() > 0) {
                    if (searchApiHitHandler != null)
                        searchApiHitHandler.removeCallbacks(searchApiRunnable);
                    setUpSearchHandler();
                }
            }
        });

        String interestedCount = mViewModel.getInterestedCount();
        if (interestedCount != null && mViewModel.shouldShowLoveCount()) {
            mBinding.interestCountButton.setVisibility(View.VISIBLE);
            mBinding.interestCountButton.setText(interestedCount);
        }


        if (mViewModel.story.get() != null) {
            mBinding.watchCountLayout.setVisibility(View.VISIBLE);
            mBinding.watchCountTextView.setText(CommonUtils.formatValue(mViewModel.story.get().getWatchCount()));
        } else {
            mBinding.watchCountLayout.setVisibility(View.GONE);
        }

        StoryCTAData mentionData = mViewModel.getMentionCTA();
        if (mentionData != null) {
            mBinding.mentionTextView.setVisibility(View.VISIBLE);
            setMentionText(mentionData.getUsername());
        } else {
            mBinding.mentionTextView.setVisibility(View.GONE);
        }

        mBinding.mentionButton.setVisibility((mViewModel.story.get() != null && mViewModel.story.get().getState() != null && mViewModel.story.get().getState().equalsIgnoreCase(Constants.PUBLISHED)) ? View.GONE : View.VISIBLE);
        // mBinding.askInterestButton.setVisibility((mViewModel.story.get() != null && mViewModel.story.get().getState() != null && mViewModel.story.get().getState().equalsIgnoreCase(Constants.PUBLISHED)) ? View.GONE : View.VISIBLE);
        mBinding.shareBtn.setVisibility((mViewModel.story.get() != null && mViewModel.story.get().getState() != null && mViewModel.story.get().getState().equalsIgnoreCase(Constants.PUBLISHED)) ? View.VISIBLE : View.GONE);
        mBinding.mentionButton.setOnClickListener(v -> {
            mBinding.mentionEditText.setEnabled(true);
            mBinding.mentionEditText.requestFocus();
            mBinding.mentionEditText.setVisibility(View.VISIBLE);
            mBinding.addedUserRv.setVisibility(View.VISIBLE);
            mBinding.mentionButton.setVisibility(View.INVISIBLE);
            //  mBinding.askInterestButton.setVisibility(View.INVISIBLE);
            mBinding.mentionTextView.setVisibility(View.GONE);
            mBinding.deleteBtn.setVisibility(View.GONE);
            showKeyboard();
            recordMentionClickEvent();
        });

        mBinding.watchCountLayout.setOnClickListener(v -> {
            if (mViewModel.story.get() != null && mViewModel.story.get().getWatchCount() > 0)
                mListener.onWatchCountClicked(mViewModel.story.get());
        });

        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE)) {
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        }
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_CREATE_STORY);
        baseProperties.put("type", mViewModel.story.get().getType());
    }

    private void showStoryView() {
        if (mViewModel.story.get().getType().equalsIgnoreCase(Constants.VIDEO)) {
            mBinding.videoView.setVisibility(View.VISIBLE);
            mBinding.imageView4.setVisibility(View.GONE);
            mBinding.playerIndicator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mBinding.videoView.isPlaying()) {
                        mBinding.videoView.pause();
                        mBinding.playerIndicator.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.videoView.start();
                        mBinding.playerIndicator.setVisibility(View.GONE);
                    }
                }
            });
            mBinding.videoView.setOnClickListener(v -> {
                if (mBinding.videoView.isPlaying()) {
                    mBinding.videoView.pause();
                    mBinding.playerIndicator.setVisibility(View.VISIBLE);
                } else {
                    mBinding.videoView.start();
                    mBinding.playerIndicator.setVisibility(View.GONE);
                }
            });
            mBinding.videoView.setOnCompletionListener(mediaPlayer -> mBinding.playerIndicator.setVisibility(View.VISIBLE));
        } else {
            mBinding.videoView.setVisibility(View.GONE);
            mBinding.playerIndicator.setVisibility(View.GONE);
            mBinding.imageView4.setVisibility(View.VISIBLE);
        }
    }


    private void askInterest() {
        if (mViewModel.isInterested) {
            mViewModel.isInterested = false;
            mBinding.askInterestButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.transparent)));
            mViewModel.removeInterestedCTA();
        } else {
            mViewModel.isInterested = true;
            mBinding.askInterestButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.color_accent)));

        }

        recordAskInterestEvent();
    }

    public void setUpSearchHandler() {
        if (searchApiHitHandler == null) {
            searchApiHitHandler = new Handler();
        }
        searchApiHitHandler.postDelayed(searchApiRunnable, DELAY_SUGGESTIONS_CALL);
    }

    private Runnable searchApiRunnable = new Runnable() {
        @Override
        public void run() {
            String query = mBinding.mentionEditText.getText().toString();
            mViewModel.fetchSuggestions(query);
            recordSearchMentionEvent(query);
        }
    };

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        //inputMethodManager.showSoftInput(mBinding.mentionEditText, InputMethodManager.SHOW_FORCED);
    }

    public void onMentionClicked(SearchSuggestionObject result) {
        CommonUtils.hideKeyboard(getActivity());
        mBinding.addedUserRv.setVisibility(View.GONE);
        mBinding.mentionEditText.setVisibility(View.GONE);
        mBinding.mentionButton.setVisibility(View.VISIBLE);
        mBinding.mentionTextView.setVisibility(View.VISIBLE);
        mBinding.askInterestButton.setVisibility(View.VISIBLE);
        mBinding.deleteBtn.setVisibility(View.VISIBLE);
        setMentionText(result.getTitle());
        mViewModel.addMentionCTA(result);
        recordUserMentionedEvent(result.getTitle());
    }

    private void setMentionText(String text) {
        if (text == null || TextUtils.isEmpty(text)) return;
        SpannableString content = new SpannableString("\u0040" + text);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        mBinding.mentionTextView.setText(content);
    }

    private void recordAskInterestEvent() {
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_ASK_INTEREST_CLICKED, baseProperties);
    }

    private void recordSearchMentionEvent(String query) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("query", query);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_MENTION_USER_SEARCH, properties);
    }

    private void recordUserMentionedEvent(String username) {
        HashMap<String, Object> properties = new HashMap<>(baseProperties);
        properties.put("username", username);
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_USER_MENTIONED, properties);
    }

    private void recordMentionClickEvent() {
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_MENTION_CLICKED, baseProperties);
    }

    private void recordPublishClickEvent() {
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_PUBLISH_CLICKED, baseProperties);
    }

    private void recordDeleteClickEvent() {
        SegmentTracker.getInstance(getContext()).trackEvent(SegmentConstants.EVENT_STORY_DELETE_CLICKED, baseProperties);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStoryInteractionListener) {
            mListener = (OnStoryInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnStoryInteractionListener {
        void onStoryDeleteOptionClicked(Story story);

        void onStoryShareOptionClicked(Story story);

        void onWatchCountClicked(Story story);
    }

}
