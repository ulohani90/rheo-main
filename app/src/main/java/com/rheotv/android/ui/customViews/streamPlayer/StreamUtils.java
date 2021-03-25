package com.rheotv.android.ui.customViews.streamPlayer;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.rheotv.android.BuildConfig;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.postlisting.responses.VideoUrlObj;
import com.rheotv.android.model.VideoQuality;
import com.rheotv.android.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class StreamUtils {

    public static MediaSource buildMediaSource(Uri uri) {
        String url = uri.toString();
        DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT);
        HttpDataSource.RequestProperties properties = factory.getDefaultRequestProperties();
        properties.set("app_version", Integer.toString(BuildConfig.VERSION_CODE));

        if (url.toLowerCase().contains(".m3u8"))
            return new HlsMediaSource.Factory(factory).createMediaSource(uri);
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(RheoTvApp.EXOPLAYER_AGENT)).setExtractorsFactory(new DefaultExtractorsFactory()
                .setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_IGNORE_AAC_STREAM)).createMediaSource(uri);
    }

    public static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE)
            return false;
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    public static String buildUrlFromCurrentFormat(String urlWithoutParameters, QualityFormat qualityFormat, boolean isLive) {
        String[] parts = urlWithoutParameters.split("\\/");
        String oldFormat = parts[parts.length - 2];
        if (qualityFormat == QualityFormat.auto && isLive)
            qualityFormat = QualityFormat.medium;
        if (oldFormat.equalsIgnoreCase("auto") || oldFormat.equalsIgnoreCase("medium") || oldFormat.equalsIgnoreCase("low") || oldFormat.equalsIgnoreCase("high")) {
            return urlWithoutParameters.replace(oldFormat, qualityFormat.name());
        }
        return urlWithoutParameters;
    }

    public static String getUrlForCurrentFormat(List<VideoUrlObj> urls, String qualityFormat) {
        for (VideoUrlObj obj : urls) {
            if (obj.getName().equalsIgnoreCase(qualityFormat)) {
                return obj.getUrl();
            }
        }
        return urls.get(0).getUrl();
    }

    public static boolean checkSpecialFormat(Context context) {
        return NetworkUtils.getNetworkType(context).equalsIgnoreCase(NetworkUtils.NETWORK_TYPE_MOBILE) && NetworkUtils.getNetworkGeneration(context).equalsIgnoreCase(NetworkUtils.NETWORK_QUALITY_HIGH);
    }

    public static List<VideoQuality> getFormats(List<VideoUrlObj> videoUrls) {
        List<VideoQuality> formats = new ArrayList<>();
        for (VideoUrlObj videoUrlObj : videoUrls) {
            if (VideoQuality.Auto.INSTANCE.toString().equalsIgnoreCase(videoUrlObj.getName())) {
                formats.add(VideoQuality.Auto.INSTANCE);
            } else if (VideoQuality.Medium.INSTANCE.toString().equalsIgnoreCase(videoUrlObj.getName())) {
                formats.add(VideoQuality.Medium.INSTANCE);
            } else if (VideoQuality.Low.INSTANCE.toString().equalsIgnoreCase(videoUrlObj.getName())) {
                formats.add(VideoQuality.Low.INSTANCE);
            } else if (VideoQuality.High.INSTANCE.toString().equalsIgnoreCase(videoUrlObj.getName())) {
                formats.add(VideoQuality.High.INSTANCE);
            } else if (VideoQuality.Audio.INSTANCE.toString().equalsIgnoreCase(videoUrlObj.getName())) {
                formats.add(VideoQuality.Audio.INSTANCE);
            }
        }
        return formats;
    }

    public static boolean containsQuality(List<VideoUrlObj> streamUrl, String quality) {
        if (streamUrl == null) return false;
        for (VideoUrlObj videoUrlObj : streamUrl) {
            if (videoUrlObj.getName().equalsIgnoreCase(quality)) {
                return true;
            }
        }
        return false;
    }
}
