package com.rheotv.android.player;

import android.content.Context;

import com.google.android.exoplayer2.ExoPlayer;

public class RheoTVPlayer {

    private Context context;
    private ExoPlayer exoPlayer = null;

    private static RheoTVPlayer instance = null;

    private RheoTVPlayer(Context context) {
        this.context = context;
    }

    public RheoTVPlayer getInstance(Context context){
        if(instance==null){
            instance = new RheoTVPlayer(context);
        }
        return instance;
    }


}
