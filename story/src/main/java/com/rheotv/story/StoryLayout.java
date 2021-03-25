package com.rheotv.story;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.rheotv.story.model.Author;
import com.rheotv.story.model.Story;
import com.rheotv.story.model.StoryCTA;
import com.rheotv.story.model.StoryCTAData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.BlurTransformation;

@SuppressLint("ViewConstructor")
public class StoryLayout extends ConstraintLayout {
    private int currentlyShownIndex = 0;
    private String currentView = Constants.IMAGE;
    private Author author;
    private List<Story> storyList;
    private List<PausableProgressBar> libSliderViewList = new ArrayList<>();
    private StoryCallback callback;
    private View view;
    private ViewGroup passedInContainerView;
    private int mProgressDrawable;
    private boolean pausedState = false;
    private boolean canMoveBack = false;
    private boolean explicitPause = false;
    private GestureDetector gestureDetector;
    private StoryFactory factory = new StoryFactory();

    // ui elements
    private LinearLayout interestBtn;
    private ImageView interestIconImageView;
    private ConstraintLayout mentionContainer;
    private ImageView mentionUserAvatar;
    private TextView mentionUsername;
    private ImageButton mentionFollowBtn;
    private ImageView placeHolderImageView;
    private TextView watchCountView;
    private TextView loveCount;
    private ImageButton moreButton;
    private LinearLayout watchCountLayout;
    private LinearLayout loveCountLayout;
    private LinearLayout linearProgressIndicatorLay;
    private ProgressBar loaderProgressbar;
    // author details
    private TextView titleTextView;
    private TextView subTitleTextView;
    private ImageView titleImageView;
    private ImageButton closeButton;
    private View titleProtectorView;

    // variables
    private String myAuthorId = "";
    private boolean hasMentioned = false;

    public StoryLayout(Context context,
                       Author author,
                       ViewGroup passedInContainerView,
                       StoryCallback callback,
                       @DrawableRes int mProgressDrawable
    ) {
        super(context);
        this.author = author;
        this.storyList = author.getStoryList();
        this.passedInContainerView = passedInContainerView;
        this.callback = callback;
        this.mProgressDrawable = mProgressDrawable;
        initView();
        init();
    }

    public StoryLayout(Context context,
                       Author author,
                       ViewGroup passedInContainerView,
                       StoryCallback callback
    ) {
        super(context);
        this.author = author;
        this.storyList = author.getStoryList();
        this.passedInContainerView = passedInContainerView;
        this.callback = callback;
        this.mProgressDrawable = R.drawable.story_progress_drawable;
        initView();
        init();
    }

    public StoryLayout(Context context,
                       Author author,
                       ViewGroup passedInContainerView,
                       StoryCallback callback,
                       boolean canMoveBack,
                       String myAuthorId
    ) {
        super(context);
        this.author = author;
        this.storyList = author.getStoryList();
        this.passedInContainerView = passedInContainerView;
        this.callback = callback;
        this.mProgressDrawable = R.drawable.story_progress_drawable;
        this.canMoveBack = canMoveBack;
        this.myAuthorId = myAuthorId;
        initView();
        init();
    }

    private void init() {
        for (Story story : storyList) {
            ProgressTimeWatcher timer = new ProgressTimeWatcher() {
                @Override
                public void onEnd(int indexFinished) {
                    currentlyShownIndex = indexFinished;
                    next();
                }
            };

            PausableProgressBar progressBar = new PausableProgressBar(
                    getContext(),
                    storyList.indexOf(story),
                    story.getType().equals(Constants.VIDEO) ? 60 : 5,
                    timer,
                    mProgressDrawable);

            libSliderViewList.add(progressBar);
            linearProgressIndicatorLay.addView(progressBar);
        }
    }

    private long touchTime = 0;

    private void initView() {
        view = View.inflate(getContext(), R.layout.progress_story_view, this);
        linearProgressIndicatorLay = view.findViewById(R.id.linearProgressIndicatorLay);
        closeButton = view.findViewById(R.id.imageButton);
        watchCountView = view.findViewById(R.id.watch_count_text_view);
        loveCount =view.findViewById(R.id.love_count);
        watchCountLayout = view.findViewById(R.id.watch_count_layout);
        loveCountLayout =view.findViewById(R.id.love_count_layout);
        titleTextView = view.findViewById(R.id.title_textView);
        subTitleTextView = view.findViewById(R.id.subtitle_textView);
        titleImageView = view.findViewById(R.id.title_image_view);
        interestBtn = view.findViewById(R.id.show_interest_button);
        interestIconImageView = view.findViewById(R.id.interested_image_view);
        mentionContainer = view.findViewById(R.id.mention_container);
        mentionFollowBtn = view.findViewById(R.id.follow_button);
        mentionUserAvatar = view.findViewById(R.id.mention_image_view);
        mentionUsername = view.findViewById(R.id.mention_username_textView);
        placeHolderImageView = view.findViewById(R.id.place_holder_image_view);
        titleProtectorView = view.findViewById(R.id.title_protector_view);
        moreButton = view.findViewById(R.id.moreButton);
        loaderProgressbar = view.findViewById(R.id.loaderProgressbar);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        gestureDetector = new GestureDetector(getContext(), new SingleTapConfirm());

        OnTouchListener touchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    // single tap
                    if (v.getId() == view.findViewById(R.id.rightLay).getId()) {
                        next();
                    } else if (v.getId() == view.findViewById(R.id.leftLay).getId()) {
                        prev();
                    }
                    return true;
                } else {
                    // your code for move and drag
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchTime = System.currentTimeMillis();
                            callPause(true, false, false);
                            Log.i(getClass().getSimpleName(), "OnTouchListener_1 " + touchTime);
                            return true;
                        case MotionEvent.ACTION_UP:
                            callPause(false, false, false);
//                            callPause(false, (System.currentTimeMillis() - touchTime) > 100);
                            Log.i(getClass().getSimpleName(), "OnTouchListener_2 " + touchTime);
                            return true;
                        case MotionEvent.ACTION_MOVE:
//                            callPause(true, (System.currentTimeMillis() - touchTime) > 100);
//                            callPause(false, false, false);
                            Log.i(getClass().getSimpleName(), "OnTouchListener_3 " + touchTime);
                            return false;

                        default:
                            return false;
                    }
                }
            }
        };


        view.findViewById(R.id.leftLay).setOnTouchListener(touchListener);
        view.findViewById(R.id.rightLay).setOnTouchListener(touchListener);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.dismiss();
            }
        });

        titleTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.viewAuthorProfile(author.getName());
            }
        });
        subTitleTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.viewAuthorProfile(author.getName());
            }
        });

        loadRoundImage(titleImageView, author.getProfileUrl());
        titleTextView.setText(author.getName());
        subTitleTextView.setText(author.getCreatedAt());

        moreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storyList.isEmpty()) return;
                callback.moreOption(storyList.get(currentlyShownIndex));
            }
        });

        titleImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.viewAuthorProfile(author.getName());
            }
        });

        interestBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storyList.isEmpty()) return;
                if (!isInterested()) {
                    callback.onInterest(storyList.get(currentlyShownIndex));
                }
            }
        });

        mentionFollowBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storyList.isEmpty()) return;
                Story story = storyList.get(currentlyShownIndex);
                boolean isFollowing = isFollowing();
                callback.onMentionFollow(story, getMentionUserName(), getMentionUserId(), isFollowing);
            }
        });

        mentionContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.viewMentionedProfile(mentionUsername.getText().toString());
            }
        });

        this.setLayoutParams(params);
        passedInContainerView.addView(this);
    }

    private void show() {
        try {
            if (storyList.isEmpty()) {
                callback.done();
                Toast.makeText(getContext(), "This story is not available.", Toast.LENGTH_LONG).show();
                return;
            }
            loaderProgressbar.setVisibility(View.GONE);
            updateHeader();

            for (int index = 0; index < libSliderViewList.size(); ++index) {
                if (index < currentlyShownIndex) {
                    libSliderViewList.get(index).setProgress(100);
                } else {
                    libSliderViewList.get(index).setProgress(0);
                }
                libSliderViewList.get(index).cancelProgress();
            }

            currentView = storyList.get(currentlyShownIndex).getType();

            libSliderViewList.get(currentlyShownIndex).startProgress();
            Log.i(StoryLayout.class.getSimpleName(), "Current_index " + currentlyShownIndex);
            View storyView = factory.getView(storyList.get(currentlyShownIndex), this, currentlyShownIndex);
            ViewGroup viewGroup = view.findViewById(R.id.currentlyDisplayedView);

            viewGroup.removeAllViews();

            if (storyView != null) {
                int viewHeight = LayoutParams.WRAP_CONTENT;
                if (Constants.TEXT.equalsIgnoreCase(storyList.get(currentlyShownIndex).getType())) {
                    viewHeight = LayoutParams.MATCH_PARENT;
                }
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        viewHeight
                );
                params.rightToRight = R.id.currentlyDisplayedView;
                params.leftToLeft = R.id.currentlyDisplayedView;
                params.topToTop = R.id.currentlyDisplayedView;
                params.bottomToBottom = R.id.currentlyDisplayedView;
                viewGroup.addView(storyView, params);
            } else {
                next();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            next();
        }
    }

    public void callPause(boolean pause, boolean shouldAnimate, boolean explicitPause) {
        try {
            this.explicitPause = explicitPause;
            if (pause) {
                if (!pausedState) {
                    this.pausedState = !pausedState;
                    pause(false, shouldAnimate);
                }
            } else {
                if (pausedState) {
                    this.pausedState = !pausedState;
                    resume(shouldAnimate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void editDurationAndResume(int index, int newDurationInSeconds) {
        loaderProgressbar.setVisibility(View.GONE);
        libSliderViewList.get(index).editDurationAndResume(newDurationInSeconds);
        if (!isExplicitPause()) {
            libSliderViewList.get(index).startProgress();
        }
    }

    void pauseProgress() {
        if (!isExplicitPause()) {
            loaderProgressbar.setVisibility(View.VISIBLE);
        }
        libSliderViewList.get(currentlyShownIndex).pauseProgress();
    }

    void resumeProgress() {
        loaderProgressbar.setVisibility(View.GONE);
        libSliderViewList.get(currentlyShownIndex).resumeProgress();
    }

    public void pause(boolean withLoader, boolean shouldAnimate) {
        if (withLoader) {
            loaderProgressbar.setVisibility(View.VISIBLE);
        }
        libSliderViewList.get(currentlyShownIndex).pauseProgress();
        if (shouldAnimate)
            hideOnPause();
        pauseVideoPlayer();
    }

    public void resume(boolean shouldAnimate) {
        loaderProgressbar.setVisibility(View.GONE);
        libSliderViewList.get(currentlyShownIndex).resumeProgress();
        if (shouldAnimate)
            showOnResume();
        playIfVideo();
    }

    private void playIfVideo() {
        if (storyList.get(currentlyShownIndex).getType().equals(Constants.VIDEO)) {
            View childView = ((ConstraintLayout) view.findViewById(R.id.currentlyDisplayedView)).getChildAt(0);
            if (childView instanceof PlayerView) {
                if (((PlayerView) childView).getPlayer() != null) {
                    Player player = ((PlayerView) childView).getPlayer();
                    if (factory.listener == null) {
                        factory.addListener(player, this, currentlyShownIndex, storyList.get(currentlyShownIndex));
                    }
                    player.setPlayWhenReady(true);
                }
            }
        }
    }

    private void pauseVideoPlayer() {
        if (storyList.get(currentlyShownIndex).getType().equals(Constants.VIDEO)) {
            View childView = ((ConstraintLayout) view.findViewById(R.id.currentlyDisplayedView)).getChildAt(0);
            if (childView instanceof PlayerView) {
                if (((PlayerView) childView).getPlayer() != null) {
                    Player player = ((PlayerView) childView).getPlayer();
                    player.setPlayWhenReady(false);
                }
            }
        }
    }

    private void removePlayerListener() {
        if (storyList.get(currentlyShownIndex).getType().equals(Constants.VIDEO)) {
            View childView = ((ConstraintLayout) view.findViewById(R.id.currentlyDisplayedView)).getChildAt(0);
            if (childView instanceof PlayerView) {
                if (((PlayerView) childView).getPlayer() != null) {
                    Player player = ((PlayerView) childView).getPlayer();
                    player.seekTo(0);
                    player.setPlayWhenReady(false);
                    factory.videoView = factory.removeListener(player);
                }
            }
        }
    }

    public void next() {
        try {
            removePlayerListener();
            if (currentView.equals(storyList.get(currentlyShownIndex).getType())) {
                currentlyShownIndex++;

                if (storyList.size() <= currentlyShownIndex) {
                    currentlyShownIndex = storyList.size() - 1;
                    finish();
                    return;
                }
            }
            show();
        } catch (IndexOutOfBoundsException e) {
            finish();
        }
    }

    private void prev() {
        try {
            removePlayerListener();
            if (currentView.equals(storyList.get(currentlyShownIndex).getType())) {
                resetCurrentStory();
                currentlyShownIndex--;
                if (0 > currentlyShownIndex) {
                    currentlyShownIndex = 0;
                    callback.backward();
                    resetCurrentStory();
                    return;
                }
            }

            show();
        } catch (IndexOutOfBoundsException e) {
            currentlyShownIndex -= 2;
        }
    }

    private void finish() {
        callback.done();
        for (PausableProgressBar progressBar : libSliderViewList) {
            progressBar.cancelProgress();
            progressBar.setProgress(100);
        }
        resetCurrentStory();
        callPause(true, false, false);
    }

    public void resetCurrentStory() {
        if (libSliderViewList.isEmpty()) return;
        if (currentlyShownIndex < libSliderViewList.size()) {
            libSliderViewList.get(currentlyShownIndex).setProgress(0);
            libSliderViewList.get(currentlyShownIndex).startProgress();
            libSliderViewList.get(currentlyShownIndex).pauseProgress();
        }
    }

    public void checkAndReleasePlayer() {
        try {
            factory.videoView.getPlayer().removeListener(factory.listener);
            factory.videoView.getPlayer().stop();
            factory.videoView.getPlayer().release();
            factory.videoView.setPlayer(null);
            factory.videoView = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        show();
    }

    private void updateHeader() {
        if (storyList.isEmpty()) {
            callback.done();
            Toast.makeText(getContext(), "This story is not available.", Toast.LENGTH_LONG).show();
            return;
        }
        StoryCTA cta = getCTA(Constants.PLAY_REQUEST_INTERESTED_CTA);
        if(cta!=null) {
            StoryCTAData data = cta.getStoryCTAData();
            if(data!=null && data.getInterestedCount()!=0)
                loveCount.setText(factory.formatValue(data.getInterestedCount()));
        }

        Story story = storyList.get(currentlyShownIndex);
        subTitleTextView.setText(factory.getTimeAgo(story.getCreatedAt()));
        watchCountView.setText(factory.formatValue(story.getWatchCount()));



        try {
            if (myAuthorId.equalsIgnoreCase(getMentionUserProfileId())) {
                mentionFollowBtn.setVisibility(GONE);
            } else {
                mentionFollowBtn.setVisibility(VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadPlaceholder();
        checkAndUpdateInterest();
        checkMentionUser();
    }

    public void checkAndUpdateInterest() {
        boolean isInterested = isInterested();
        if (isInterested) {
            interestBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.red)));
            interestIconImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.avd_heart_filled));
        } else {
            interestBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.dark_translucent)));
            interestIconImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.avd_heart_outline));
        }
    }

    private void checkMentionUser() {
        StoryCTA cta = getCTA(Constants.MENTION_CTA);
        if (cta != null) {
            StoryCTAData data = cta.getStoryCTAData();
            if (data != null) {
                hasMentioned = true;
                mentionContainer.setVisibility(View.VISIBLE);
                populateMentionUser(data);
                checkAndUpdateFollowing();
            } else {
                mentionContainer.setVisibility(View.GONE);
            }
        } else {
            mentionContainer.setVisibility(View.GONE);
        }
    }

    public boolean isInterested() {
        StoryCTA cta = getCTA(Constants.PLAY_REQUEST_INTERESTED_CTA);
        if (cta != null) {
            StoryCTAData data = cta.getStoryCTAData();
            if (data != null) {
                interestBtn.setVisibility(View.VISIBLE);
                loveCountLayout.setVisibility(View.VISIBLE);
                return data.isInterested();
            } else {
                interestBtn.setVisibility(View.GONE);
                loveCountLayout.setVisibility(View.GONE);
            }
        } else {
            interestBtn.setVisibility(View.GONE);
            loveCountLayout.setVisibility(View.GONE);
        }
        return false;
    }

    private void populateMentionUser(StoryCTAData data) {
        mentionUsername.setText(data.getUsername());
        loadRoundImage(mentionUserAvatar, data.getProfileUrl());
    }

    public void checkAndUpdateFollowing() {
        checkAndUpdateFollowing(isFollowing());
    }

    public void checkAndUpdateFollowing(boolean isFollowing) {
        if (isFollowing) {
            mentionFollowBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_added_user));
        } else {
            mentionFollowBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_add_user_dark));
        }
    }

    private boolean isFollowing() {
        StoryCTA cta = getCTA(Constants.MENTION_CTA);
        if (cta == null) return false;
        StoryCTAData data = cta.getStoryCTAData();
        if (data == null) return false;
        return data.isFollowed();
    }

    public void setFollowing(boolean flag) {
        StoryCTA cta = getCTA(Constants.MENTION_CTA);
        if (cta == null) return;
        StoryCTAData data = cta.getStoryCTAData();
        if (data == null) return;
        data.setFollowed(flag);
    }

    private String getMentionUserProfileId() {
        StoryCTA cta = getCTA(Constants.MENTION_CTA);
        if (cta == null) return "";
        StoryCTAData data = cta.getStoryCTAData();
        if (data == null) return "";
        return data.getProfileId();
    }

    private String getMentionUserName() {
        StoryCTA cta = getCTA(Constants.MENTION_CTA);
        if (cta == null) return "";
        StoryCTAData data = cta.getStoryCTAData();
        if (data == null) return "";
        return data.getUsername();
    }

    private String getMentionUserId() {
        StoryCTA cta = getCTA(Constants.MENTION_CTA);
        if (cta == null) return "";
        StoryCTAData data = cta.getStoryCTAData();
        if (data == null) return "";
        return data.getUserId();
    }

    public StoryCTA getCTA(String ctaType) {
        if (this.storyList.isEmpty()) return null;
        Story story = this.storyList.get(currentlyShownIndex);
        if (story == null) return null;

        ArrayList<StoryCTA> ctas = story.getStoryCTAS();
        if (ctas == null)
            ctas = new ArrayList<>();

        for (StoryCTA cta : ctas) {
            if (cta.getCtaType().equalsIgnoreCase(ctaType)) {
                return cta;
            }
        }

        return null;
    }

    public void addInterestedCTA() {
        setInterestedFlag(true);
    }

    public void setInterestedFlag(boolean flag) {
        StoryCTA cta = getCTA(Constants.PLAY_REQUEST_INTERESTED_CTA);
        if (cta == null) return;

        StoryCTAData data = cta.getStoryCTAData();
        if (data == null) return;

        data.setInterested(flag);
    }

    public StoryCallback getCallback() {
        return callback;
    }

    private void hideOnPause() {
        AnimatorSet decSet2 = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofFloat(interestBtn, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(titleTextView, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(subTitleTextView, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(titleImageView, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(titleProtectorView, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(watchCountLayout, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(loveCountLayout, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(moreButton, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(closeButton, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(interestBtn, "alpha", 1f, 0f));
        animators.add(ObjectAnimator.ofFloat(linearProgressIndicatorLay, "alpha", 1f, 0f));
        if (hasMentioned)
            animators.add(ObjectAnimator.ofFloat(mentionContainer, "alpha", 1f, 0f));
        decSet2.playTogether(animators);
        decSet2.setDuration(300);
        decSet2.start();
    }

    private void showOnResume() {
        AnimatorSet decSet2 = new AnimatorSet();
        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(ObjectAnimator.ofFloat(interestBtn, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(titleTextView, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(subTitleTextView, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(titleImageView, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(titleProtectorView, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(watchCountLayout, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(loveCountLayout, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(moreButton, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(closeButton, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(interestBtn, "alpha", 0f, 1f));
        animators.add(ObjectAnimator.ofFloat(linearProgressIndicatorLay, "alpha", 0f, 1f));
        if (hasMentioned)
            animators.add(ObjectAnimator.ofFloat(mentionContainer, "alpha", 0f, 1f));
        decSet2.playTogether(animators);
        decSet2.setDuration(300);
        decSet2.start();
    }

    private void loadRoundImage(ImageView imageView, String url) {
        if (((Activity) getContext()).isDestroyed()) return;
        Glide.with(getContext())
                .load(url)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()).error(R.drawable.avd_avatar))
                .transition(new DrawableTransitionOptions().crossFade())
                .into(imageView);
    }

    private void loadPlaceholder() {
        try {
            if (((Activity) getContext()).isDestroyed()) return;
            String url = storyList.get(currentlyShownIndex).getPlaceholderThumbnail();
            Picasso.get()
                    .load(url)
                    .transform(new BlurTransformation(getContext(), 5, 1))
                    .into(placeHolderImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        return !pausedState;
    }

    public boolean isExplicitPause() {
        return explicitPause;
    }

    public void showLoader(boolean show) {
        if (show) {
            if (loaderProgressbar != null) {
                loaderProgressbar.setVisibility(VISIBLE);
            }
        } else {
            if (loaderProgressbar != null) {
                loaderProgressbar.setVisibility(GONE);
            }
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }
}
