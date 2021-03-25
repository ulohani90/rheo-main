package com.rheotv.android.ui.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.data.network.models.objects.FeedObject;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.databinding.ItemCarouselViewBinding;
import com.rheotv.android.databinding.ItemPostEmptyBinding;
import com.rheotv.android.databinding.ItemPostTopGamesLayoutBinding;
import com.rheotv.android.databinding.ItemPostViewBinding;
import com.rheotv.android.databinding.ItemTopStreamersCardBinding;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;

import java.util.List;

public class PostListAdapterNew extends RecyclerView.Adapter<BaseViewHolder> {

    private List<FeedObject> mPostList;

    int heightSingleItemCarousel;

    int heightMultiItemCarousel;

    int superPrimeStreamerCardWidth;

    int superPrimeStreamerCardHeight;

    public PostListAdapterNew(Context context) {
        calculateHeightForCarouselItems(context);
    }


    public void calculateHeightForCarouselItems(Context context) {
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        int width = outMetrics.widthPixels - (2 * (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, outMetrics));
        heightSingleItemCarousel = ((width * 9) / 16) + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, outMetrics);
        heightMultiItemCarousel = ((((width * 9) / 10) * 9) / 16) + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, outMetrics);
        superPrimeStreamerCardWidth = outMetrics.widthPixels - (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, outMetrics));
        superPrimeStreamerCardHeight = (superPrimeStreamerCardWidth * 360) / 540;
    }

    public void setmPostList(List<FeedObject> mPostList) {
        this.mPostList = mPostList;
    }

    @Override
    public int getItemViewType(int position) {
        if (mPostList != null && !mPostList.isEmpty()) {
            if (position == mPostList.size()) {
                return AppConstants.VIEW_TYPE_LOADING_FOOTER;
            } else if (mPostList.get(position).getType() == 0)
                return AppConstants.VIEW_TYPE_NORMAL;
            else if (mPostList.get(position).getType() == 2) {
                return AppConstants.VIEW_TYPE_CAROUSEL;
            } else if (mPostList.get(position).getType() == 7) {
                return AppConstants.VIEW_TOP_STREAMERS;
            } else if (mPostList.get(position).getType() == 10) {
                return AppConstants.VIEW_TYPE_SUPER_PRIME_STREAMER;
            } else if (mPostList.get(position).getType() == 11) {
                return AppConstants.VIEW_TYPE_TOP_GAMES;
            } else {
                return AppConstants.VIEW_TYPE_NORMAL;
            }
        } else {
            return AppConstants.VIEW_TYPE_EMPTY;
        }
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       /* switch (viewType) {
            case AppConstants.VIEW_TYPE_NORMAL:
                ItemPostViewBinding blogViewBinding = ItemPostViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new BlogViewHolder(blogViewBinding);
            case AppConstants.VIEW_TYPE_CAROUSEL:
                ItemCarouselViewBinding carouselViewBinding = ItemCarouselViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);

                return new CarouselViewHolder(parent.getContext(), carouselViewBinding);

            case AppConstants.VIEW_TYPE_EMPTY:
                ItemPostEmptyBinding emptyViewBinding = ItemPostEmptyBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new EmptyViewHolder(emptyViewBinding);
            case AppConstants.VIEW_TOP_STREAMERS:
                ItemTopStreamersCardBinding itemTopStreamersCardBinding = ItemTopStreamersCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new TopStreamersViewHolder(itemTopStreamersCardBinding);

            case AppConstants.VIEW_TYPE_LOADING_FOOTER:
                FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
            case AppConstants.VIEW_TYPE_SUPER_PRIME_STREAMER:
                ItemCarouselViewBinding primeStreamersCarouselViewBinding = ItemCarouselViewBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);

                return new SuperPrimeStreamersViewHolder(parent.getContext(), primeStreamersCarouselViewBinding);
            case AppConstants.VIEW_TYPE_TOP_GAMES:
                ItemPostTopGamesLayoutBinding itemPostTopGamesLayoutBinding = ItemPostTopGamesLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new TopGamesViewHolder(itemPostTopGamesLayoutBinding);
            default:
                ItemPostEmptyBinding emptyBinding = ItemPostEmptyBinding.inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);
                return new EmptyViewHolder(emptyBinding);
        }*/
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
