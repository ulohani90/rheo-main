package com.rheotv.android.ui.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.objects.GameObject;
import com.rheotv.android.databinding.ItemTopGamesLayoutBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.BindingUtils;

import java.util.List;

public class TopGamesGridAdapter extends RecyclerView.Adapter<BaseViewHolder> {


    List<GameObject> mTopGames;

    OnTopGamesCardClick mListener;

    public TopGamesGridAdapter(List<GameObject> topGames) {
        this.mTopGames = topGames;
    }

    public void setListener(OnTopGamesCardClick mListener) {
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemTopGamesLayoutBinding itemTopGamesLayoutBinding = ItemTopGamesLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ItemTopGameCardViewHolder(itemTopGamesLayoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder baseViewHolder, int position) {
        baseViewHolder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return mTopGames.size();
    }

    public class ItemTopGameCardViewHolder extends BaseViewHolder {

        ItemTopGamesLayoutBinding mBinding;

        public ItemTopGameCardViewHolder(ItemTopGamesLayoutBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            GameObject result = mTopGames.get(position);
            BindingUtils.setImageUrlUsingCache(mBinding.gameImage, result.getThumbnail(), true);
            mBinding.gameNameTv.setText(result.getName());
            mBinding.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mListener.onTopGameCardClick(result.getName(), result.getId());
                }
            });

        }
    }

    public interface OnTopGamesCardClick {
        void onTopGameCardClick(String gameName, String gameId);
    }
}
