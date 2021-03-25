package com.rheotv.android.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.utils.BindingUtils;

import java.util.ArrayList;
import java.util.List;

public class GameSpinner extends BaseAdapter {
    private ArrayList<GameDetails> gameDetailsList = new ArrayList<>();
    private Context context;
    private boolean darkTextStyle = true;

    public GameSpinner(Context context) {
        this.context = context;
        this.gameDetailsList.add(0, new GameDetails(null, "Select Game", null, false, false));
    }

    public void submitList(List<GameDetails> gameDetailsList) {
        if (gameDetailsList == null || gameDetailsList.isEmpty()) return;
        this.gameDetailsList.clear();
        this.gameDetailsList.add(0, new GameDetails(null, "Select Game", null, false, false));
        this.gameDetailsList.addAll(gameDetailsList);
        notifyDataSetChanged();
    }

    public GameSpinner(List<GameDetails> gameDetailsList, Context context) {
        this.gameDetailsList.addAll(gameDetailsList);
        this.context = context;
    }

    public void enableLightText() {
        darkTextStyle = false;
    }

    @Override
    public int getCount() {
        return gameDetailsList.size();
    }

    @Override
    public Object getItem(int i) {
        return gameDetailsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.gamedetails, null);
        }

        GameDetails gameDetails = gameDetailsList.get(position);
        TextView gameTV = convertView.findViewById(R.id.gamedetailsTV);
        if (!darkTextStyle)
            gameTV.setTextColor(ContextCompat.getColor(context, R.color.white));
        gameTV.setText(gameDetails.getName());

        ImageView gameIV = convertView.findViewById(R.id.gamedetailsIV);
        if (gameDetails.getThumbnail() != null) {
            BindingUtils.setImageUrlUsingCache(gameIV, gameDetails.getThumbnail(), true);
            gameIV.setVisibility(View.VISIBLE);
        } else
            gameIV.setVisibility(View.GONE);

        return convertView;
    }
}