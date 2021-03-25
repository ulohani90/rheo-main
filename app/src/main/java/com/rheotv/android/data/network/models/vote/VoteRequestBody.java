package com.rheotv.android.data.network.models.vote;

import com.google.gson.annotations.SerializedName;

public class VoteRequestBody {

    @SerializedName("participant_id")
    private String participantId;

    public VoteRequestBody(String participantId) {
        this.participantId = participantId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

}
