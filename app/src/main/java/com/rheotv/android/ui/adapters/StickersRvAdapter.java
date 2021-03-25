package com.rheotv.android.ui.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.rheotv.android.ui.customViews.SquareRelativeLayout;
import com.rheotv.android.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

import static com.rheotv.android.ui.activities.player.activity.StickerGridRecyclerAdapter.ITEM_VIEW_TYPE_FOOTER;
import static com.rheotv.android.ui.activities.player.activity.StickerGridRecyclerAdapter.ITEM_VIEW_TYPE_GREETING;
import static com.rheotv.android.ui.activities.player.activity.StickerGridRecyclerAdapter.ITEM_VIEW_TYPE_STICKER;

public class StickersRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Sticker> stickers;

    boolean showLoading;

    int stickerSize;

    OnStickersClickListener mListener;




    public StickersRvAdapter(int stickerSize) {
        stickers = new ArrayList<>();
        this.stickerSize = stickerSize;
    }

    public void setmListener(OnStickersClickListener mListener) {
        this.mListener = mListener;
    }

    public void setStickers(List<Sticker> stickers) {
        int startPosition = this.stickers.size();
        this.stickers.addAll(stickers);
        notifyItemRangeInserted(startPosition, stickers.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == stickers.size()) {
            return ITEM_VIEW_TYPE_FOOTER;
        }
        if (stickers.get(position).getType().equalsIgnoreCase(AppConstants.STICKER_TYPE_GREETING)) {
            return ITEM_VIEW_TYPE_GREETING;
        }
        return ITEM_VIEW_TYPE_STICKER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_STICKER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_item_layout, parent, false);
            /*FrameLayout parentLayout = view.findViewById(R.id.parent);
            ViewGroup.LayoutParams lp = parentLayout.getLayoutParams();
            lp.height = stickerSize;*/
            StickerViewHolder holder = new StickerViewHolder(view);
            return holder;
        } else if (viewType == ITEM_VIEW_TYPE_GREETING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_greet_streamer_binding, parent, false);
            return new GreetingViewHolder(view);
        } else {
            FooterLoadingViewHolder holder = new FooterLoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_loading_layout, parent, false));
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE_STICKER) {
            StickerViewHolder viewHolder = (StickerViewHolder) holder;
            Glide.with(viewHolder.stickerImage.getContext()).load(stickers.get(position).getStickerUrl()).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    viewHolder.loader.setVisibility(View.GONE);
                    return false;
                }
            }).into(viewHolder.stickerImage);

            viewHolder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onStickerClicked(stickers.get(position).getStickerUrl(), stickers.get(position).getId());
                    }
                }
            });

        } else if (getItemViewType(position) == ITEM_VIEW_TYPE_GREETING) {
            GreetingViewHolder viewHolder = (GreetingViewHolder) holder;
            viewHolder.coinValue.setText(stickers.get(position).getValue() + "");
            viewHolder.greetHeader.setText(stickers.get(position).getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return stickers != null ? showLoading ? stickers.size() + 1 : stickers.size() : 0;
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        notifyItemChanged(stickers.size());
    }

    public void clearData() {
        this.stickers.clear();
    }

    public class StickerViewHolder extends RecyclerView.ViewHolder {

        ImageView stickerImage;
        ProgressBar loader;
        SquareRelativeLayout parent;

        public StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = (SquareRelativeLayout) itemView.findViewById(R.id.parent);
            stickerImage = (ImageView) itemView.findViewById(R.id.sticker_image);
            loader = (ProgressBar) itemView.findViewById(R.id.loader);
        }
    }

    public class GreetingViewHolder extends RecyclerView.ViewHolder {

        TextView greetHeader;
        TextView coinValue;
        EditText editText;

        public GreetingViewHolder(@NonNull View itemView) {
            super(itemView);
            greetHeader = (TextView) itemView.findViewById(R.id.greet_header);
            editText = (EditText) itemView.findViewById(R.id.greet_message);
            coinValue = (TextView) itemView.findViewById(R.id.coin_value);
        }
    }

    public class FooterLoadingViewHolder extends RecyclerView.ViewHolder {

        public FooterLoadingViewHolder(View itemView) {
            super(itemView);
        }
    }


    public interface OnStickersClickListener {
        void onStickerClicked(String stickerUrl, String stickerId);
    }
}
