package com.rheotv.android.utils;

import android.content.Context;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.lb.video_trimmer_library.BaseVideoTrimmerView;
import com.lb.video_trimmer_library.view.RangeSeekBarView;
import com.lb.video_trimmer_library.view.TimeLineView;
import com.rheotv.android.R;

public class VideoTrimmerView extends BaseVideoTrimmerView {
    private View rootView;

    public VideoTrimmerView(Context context) {
        super(context, null);
    }

    public VideoTrimmerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTrimmerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public VideoTrimmerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleResources) {
//        super(context, attrs, defStyleAttr, defStyleResources);
//    }

    private String stringForTime(int timeMs) {
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = totalSeconds / 60 % 60;
        long hours = totalSeconds / 3600;
        java.util.Formatter timeFormatter = new java.util.Formatter();
        if (hours > 0)
            return timeFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        else
            return timeFormatter.format("%02d:%02d", minutes, seconds).toString();
    }

    @Override
    public void initRootView() {
        rootView = LayoutInflater.from(getContext()).inflate(R.layout.video_trimmer, this, true);
        rootView.findViewById(R.id.fab).setOnClickListener(view -> initiateTrimming());
    }

    @NonNull
    @Override
    public TimeLineView getTimeLineView() {
        return rootView.findViewById(R.id.timeLineView);
    }

    @NonNull
    @Override
    public View getTimeInfoContainer() {
        return rootView.findViewById(R.id.timeTextContainer);
    }

    @NonNull
    @Override
    public View getPlayView() {
        return rootView.findViewById(R.id.playIndicatorView);
    }

    @NonNull
    @Override
    public VideoView getVideoView() {
        return rootView.findViewById(R.id.videoView);
    }

    @NonNull
    @Override
    public View getVideoViewContainer() {
        return rootView.findViewById(R.id.videoViewContainer);
    }

    @NonNull
    @Override
    public RangeSeekBarView getRangeSeekBarView() {
        return rootView.findViewById(R.id.rangeSeekBarView);
    }

    @Override
    public void onGotVideoFileSize(long videoFileSize) {
        TextView textView = rootView.findViewById(R.id.videoFileSizeTextView);
        String size = Formatter.formatShortFileSize(getContext(), videoFileSize) + "  ";
        textView.setText(size);
    }

    @Override
    public void onRangeUpdated(int startTimeInMs, int endTimeInMs) {
        TextView textView = rootView.findViewById(R.id.trimTimeRangeTextView);
        String seconds = stringForTime(startTimeInMs)  + " - " + stringForTime(endTimeInMs);
        textView.setText(seconds);
    }

    @Override
    public void onVideoPlaybackReachingTime(int timeInMs) {
        TextView textView = rootView.findViewById(R.id.playbackTimeTextView);
        String time = "  " + stringForTime(timeInMs);
        textView.setText(time);
    }
}
