package com.rheotv.android.utils;

import com.rheotv.android.data.ModeratorQuestion;

import java.util.ArrayList;
import java.util.List;

public class ModeratorQuestions {


    public static ModeratorQuestions mInstance;

    public ModeratorQuestions() {
        clipsQuestions = new ArrayList<>();
    }

    List<ModeratorQuestion> clipsQuestions;

    public static ModeratorQuestions getInstance() {
        if (mInstance == null) {
            mInstance = new ModeratorQuestions();
        }
        return mInstance;
    }

    public List<ModeratorQuestion> getClipsQuestions() {
        return clipsQuestions;
    }

    public void setClipsQuestions(List<ModeratorQuestion> clipsQuestions) {
        this.clipsQuestions = clipsQuestions;
    }
}
