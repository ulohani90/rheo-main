package com.rheotv.android.ui.adapters;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.rheotv.android.data.network.models.objects.PostObject;

import java.util.ArrayList;

public class CarouselListDiffUtils extends DiffUtil.Callback {

    ArrayList<PostObject> oldList;
    ArrayList<PostObject> newList;

    public void CarouselListDiffUtils(ArrayList<PostObject> oldList, ArrayList<PostObject> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition).getId() == oldList.get(oldItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        int result = newList.get(newItemPosition).compareTo(oldList.get(oldItemPosition));
        return result == 0;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        PostObject newModel = newList.get(newItemPosition);
        PostObject oldModel = oldList.get(oldItemPosition);

        Bundle diff = new Bundle();

        if (!newModel.getAuthor().getUser().getUsername().equalsIgnoreCase(newModel.getAuthor().getUser().getUsername())) {
            diff.putString("author_name", newModel.getAuthor().getUser().getUsername());
        }
        if (diff.size() == 0) {
            return null;
        }
        return diff;
    }
}
