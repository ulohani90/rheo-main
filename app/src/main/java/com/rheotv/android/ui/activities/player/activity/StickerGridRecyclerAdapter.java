package com.rheotv.android.ui.activities.player.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.rheotv.android.databinding.LayoutGreetStreamerBindingBinding;
import com.rheotv.android.databinding.ListItemStickerBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class StickerGridRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    Context context;

    public StickerGridRecyclerAdapter(Context context) {
        this.context = context;
    }

    private List<Sticker> mList = new ArrayList<>();

    StickerSelectionListener mStickerSelectionListener;

    public static final int ITEM_VIEW_TYPE_STICKER = 0;
    public static final int ITEM_VIEW_TYPE_FOOTER = 1;
    public static final int ITEM_VIEW_TYPE_GREETING = 2;
    public boolean isGreetingEnabled = true;

    public void setStickerSelectionListener(StickerSelectionListener stickerSelectionListener) {
        this.mStickerSelectionListener = stickerSelectionListener;
    }

    public void setGreetingEnabled(boolean enabled) {
        this.isGreetingEnabled = enabled;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_GREETING) {
            LayoutGreetStreamerBindingBinding binding = LayoutGreetStreamerBindingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new GreetingViewHolder(binding);
        } else {
            ListItemStickerBinding listItemStickerBinding = ListItemStickerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            //listItemStickerBinding.
            return new ViewHolder(listItemStickerBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isGreetingEnabled && position < mList.size() && mList.get(position) != null && AppConstants.STICKER_TYPE_GREETING.equalsIgnoreCase(mList.get(position).getType())) {
            return ITEM_VIEW_TYPE_GREETING;
        }
        return ITEM_VIEW_TYPE_STICKER;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void submitList(List<Sticker> list) {
        int initialPosition = mList.size();
        mList.addAll(list);
        notifyItemRangeInserted(initialPosition, list.size());
    }

    class ViewHolder extends BaseViewHolder {
        ListItemStickerBinding mViewBinding;

        ViewHolder(ListItemStickerBinding viewBinding) {
            super(viewBinding.getRoot());
            mViewBinding = viewBinding;
        }

        @Override
        public void onBind(int position) {
            Sticker sticker = mList.get(position);
            Glide.with(itemView.getContext())
                    .load(sticker.getStickerUrl())
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mViewBinding.loader.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mViewBinding.stickerImageView);
            if (sticker.getValue() != 0) {
                mViewBinding.coinIconImageView.setVisibility(View.VISIBLE);
                mViewBinding.stickerValue.setText("" + sticker.getValue());
            } else {
                mViewBinding.coinIconImageView.setVisibility(View.GONE);
                mViewBinding.stickerValue.setText("FREE");
            }
            mViewBinding.stickerImageView.setOnClickListener(v -> {
                if (mStickerSelectionListener != null) {
                    mStickerSelectionListener.onStickerSelected(sticker);
                }
            });
        }
    }

    class GreetingViewHolder extends BaseViewHolder {
        LayoutGreetStreamerBindingBinding mBinding;

        public GreetingViewHolder(LayoutGreetStreamerBindingBinding binding) {
            super(binding.getRoot());
            mBinding = binding;

        }

        @Override
        public void onBind(int position) {
            mBinding.coinValue.setText(mList.get(position).getValue() + "");
            mBinding.greetHeader.setText(mList.get(position).getTitle());
            mBinding.greetMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        mBinding.characterCountText.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.characterCountText.setVisibility(View.GONE);
                    }
                }
            });
            mBinding.greetMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable != null)
                        mBinding.characterCountText.setText((150 - editable.length()) + "");
                }
            });

            mBinding.sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View view) {
                    CommonUtils.hideKeyboardFrom(context, view);
                    String message = "";
                    if (mBinding.greetMessage.getText() != null && mBinding.greetMessage.getText().toString().trim().length() > 0) {
                        message = mBinding.greetMessage.getText().toString().trim();
                    }
                    mStickerSelectionListener.onStickerSelected(mList.get(position), message);
                }
            });
        }
    }

    public interface StickerSelectionListener {
        void onStickerSelected(Sticker sticker);

        void onStickerSelected(Sticker sticker, String message);

        void onBottomSheetClose();
    }
}

