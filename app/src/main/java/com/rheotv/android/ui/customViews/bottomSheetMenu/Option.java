package com.rheotv.android.ui.customViews.bottomSheetMenu;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.rheotv.android.R;

public class Option {

    private int id;

    private CharSequence title;

    @Nullable
    private Drawable icon;

    @Nullable
    private String tag;

    public Option(int id, CharSequence title, @Nullable Drawable icon) {
        this.id = id;
        this.title = title;
        this.icon = icon;
    }

    public Option(int id, CharSequence title, @Nullable Drawable icon, @Nullable String tag) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public CharSequence getTitle() {
        return title;
    }

    @Nullable
    public Drawable getIcon() {
        return icon;
    }

    @Nullable
    public String getTag() {
        return tag;
    }

    public boolean showTint() {
        return id != R.id.action_share_on_facebook && id != R.id.action_share_on_instagram && id != R.id.action_share_on_youtube;
    }
}
