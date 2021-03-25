package com.rheotv.android.ui.activities.selectGame;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.general.GameDetails;
import com.rheotv.android.databinding.ListItemGameSelectionBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSelectionAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<GameDetails> list;
    private Map<String, String> selectedGame;
    private GameInteractionListener mListener;

    public GameSelectionAdapter(List<GameDetails> list) {
        this.list = list;
        this.selectedGame = new HashMap<>();
    }

    public void setListener(GameInteractionListener mListener) {
        this.mListener = mListener;
    }

    void submitList(List<GameDetails> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemGameSelectionBinding binding = ListItemGameSelectionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GameViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class GameViewHolder extends BaseViewHolder {
        ListItemGameSelectionBinding binding;

        GameViewHolder(ListItemGameSelectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onBind(int position) {
            GameDetails game = list.get(position);
            BindingUtils.setImageUrlUsingCache(binding.gameThumbnailImageView, game.getThumbnail(), false);
            binding.setGame(game);

            if (selectedGame.containsKey(game.getId())) {
                binding.getRoot().setSelected(true);
            } else {
                binding.getRoot().setSelected(false);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (selectedGame.containsKey(game.getId())) {
                    selectedGame.remove(game.getId());
                } else {
                    selectedGame.put(game.getId(), game.getName());
                }

                binding.getRoot().setSelected(!binding.getRoot().isSelected());
                if (mListener != null)
                    mListener.onGameItemClicked(selectedGame);
            });
        }
    }

    interface GameInteractionListener {
        void onGameItemClicked(Map<String, String> selectedGame);
    }
}
