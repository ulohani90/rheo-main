package com.rheotv.android.ui.activities.selectGame;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;

import com.rheotv.android.data.DataManager;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.ui.base.BaseViewModel;
import com.rheotv.android.utils.Status;
import com.rheotv.android.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rheotv.android.app.RheoTvApp.getNonUiContext;

public class GameSelectionViewModel extends BaseViewModel {
    MutableLiveData<List<GameDetails>> gameResults = new MutableLiveData<>();
    public ObservableField<Status> submitting = new ObservableField<>();
    public ObservableField<Status> loadingGame = new ObservableField<>();

    private Map<String, String> selectedGame = new HashMap<>();

    void setSelectedGame(Map<String, String> selectedGame) {
        this.selectedGame = selectedGame;
    }

    public GameSelectionViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    void loadGameDetails() {
        loadingGame.set(Status.LOADING);
        getDataManager()
                .getGameDetails()
                .enqueue(new Callback<List<GameDetails>>() {
                    @Override
                    public void onResponse(Call<List<GameDetails>> call, Response<List<GameDetails>> response) {
                        if (response.isSuccessful()) {
                            gameResults.setValue(response.body());
                            loadingGame.set(Status.SUCCESS);
                        } else {
                            loadingGame.set(Status.ERROR);
                            Toast.makeText(getNonUiContext(), "Couldn't load games. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<GameDetails>> call, Throwable t) {
                        Log.e(getClass().getSimpleName(), "failed to get games");
                        loadingGame.set(Status.ERROR);
                        Toast.makeText(getNonUiContext(), "Couldn't load games. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadSelectedGame() {
//        Log.i(getClass().getName(), "uploadSelectedGame : " + new Gson().toJson(new ArrayList<>(selectedGame.keySet())));
        submitting.set(Status.LOADING);
        getDataManager().setUserGames(new ArrayList<>(selectedGame.keySet())).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    submitting.set(Status.SUCCESS);
                } else {
                    submitting.set(Status.ERROR);
                    Toast.makeText(getNonUiContext(), "Unable to set game preference. Please try Again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                submitting.set(Status.ERROR);
                Toast.makeText(getNonUiContext(), "Unable to set game preference. Please try Again.", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void onContinueClick(View view) {
        if (selectedGame.isEmpty()) {
            Toast.makeText(view.getContext(), "Please select a game", Toast.LENGTH_LONG).show();
            return;
        }

        uploadSelectedGame();
    }
}
