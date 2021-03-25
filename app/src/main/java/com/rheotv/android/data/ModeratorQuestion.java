package com.rheotv.android.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModeratorQuestion {

    @SerializedName("id")
    String id;

    @SerializedName("question")
    String question;

    @SerializedName("options")
    List<ModeratorQuestionOption> options;

    @SerializedName("polarity")
    String polarity;

    @SerializedName("pertains")
    String pertains;

    @SerializedName("choice_type")
    String choiceType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<ModeratorQuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<ModeratorQuestionOption> options) {
        this.options = options;
    }

    public String getPolarity() {
        return polarity;
    }

    public void setPolarity(String polarity) {
        this.polarity = polarity;
    }

    public String getPertains() {
        return pertains;
    }

    public void setPertains(String pertains) {
        this.pertains = pertains;
    }

    public String getChoiceType() {
        return choiceType;
    }

    public void setChoiceType(String choiceType) {
        this.choiceType = choiceType;
    }
}
