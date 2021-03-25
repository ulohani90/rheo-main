package com.rheotv.android.ui.activities.player.activity;

import com.rheotv.android.data.network.models.play.RequestPlayResponse;
import com.rheotv.android.data.network.models.play.ResultsItem;

public interface RequestPlayNavigator {

    void addPlayers(RequestPlayResponse response);

    void handleErrorResponse(String message);

    void handleErrorResponse();

    void handleSubmitCustomRoomDetailsSuccess(String customRoomUsername, String customRoomPassword);

    void handleSubmitCustomRoomDetailsError(String error);

    void handleActionSuccessResponse(String requestId, String action);

    void updateWaitingNumber(String waitingNumber);

}
