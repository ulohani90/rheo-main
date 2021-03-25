package com.rheotv.android.ui.activities.player.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.rheotv.android.R;
import com.rheotv.android.app.RheoTvApp;
import com.rheotv.android.data.network.models.stickers.Sticker;
import com.rheotv.android.databinding.BottomSheetStickerBinding;
import com.rheotv.android.ui.activities.inAppBilling.BillingActivity;
import com.rheotv.android.ui.base.BaseBottomSheetDialogFragment;
import com.rheotv.android.ui.customViews.simpleSnackbar.SimpleSnackbar;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.recyclerdecorators.GridSpaceItemDecoration;
import com.rheotv.android.utils.RewardManager;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import static com.rheotv.android.utils.segmentTracker.SegmentConstants.EVENT_CHAT_STICKER_SENT;

public class StickerBottomSheet extends BaseBottomSheetDialogFragment<BottomSheetStickerBinding, StickerBottomSheetViewModel> implements StickerGridRecyclerAdapter.StickerSelectionListener {
    public static final String TAG = "SendGiftBottomSheet";

    @Inject
    StickerBottomSheetViewModel mViewModel;


    StickerGridRecyclerAdapter mAdapter;

    private HashMap<String, Object> baseProperties = new HashMap<>();

    private boolean isStickersLoading = false;
    private StickerGridRecyclerAdapter.StickerSelectionListener mStickerSelectionListener;
    private String authorName;
    private boolean isGreetingEnable = true;

    public static StickerBottomSheet newInstance(String postId, String authorName, boolean isGreetingEnabled, StickerGridRecyclerAdapter.StickerSelectionListener stickerSelectionListener) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.ARG_POST, postId);
        StickerBottomSheet fragment = new StickerBottomSheet();
        fragment.authorName = authorName;
        fragment.isGreetingEnable = isGreetingEnabled;
        fragment.mStickerSelectionListener = stickerSelectionListener;
        fragment.setArguments(bundle);
        return fragment;
    }

    public static StickerBottomSheet newInstance(String postId, String authorName, StickerGridRecyclerAdapter.StickerSelectionListener stickerSelectionListener) {
        return newInstance(postId, authorName, true, stickerSelectionListener);
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.bottom_sheet_sticker;
    }

    @Override
    public StickerBottomSheetViewModel getViewModel() {
        if (getArguments() != null) {
            if (getArguments().containsKey(AppConstants.ARG_POST)) {
                mViewModel.postId = getArguments().getString(AppConstants.ARG_POST);
            }
        }
        mViewModel.getStickerList().observe(this, list -> {
            getViewDataBinding().loader.setVisibility(View.GONE);
            mAdapter.submitList(list);
            isStickersLoading = false;
        });
        return mViewModel;
    }

    int spanCount;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new StickerGridRecyclerAdapter(getActivity());
        adjustWindow(view);
        updateViewHeight();

        //setupRv();
        getViewDataBinding().totalCoins.setText(RewardManager.getInstance().getTotalCoins());
        mAdapter.setStickerSelectionListener(this);
        mAdapter.setGreetingEnabled(isGreetingEnable);
        spanCount = 3;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 4;
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mAdapter.getItemViewType(position)) {
                    case StickerGridRecyclerAdapter.ITEM_VIEW_TYPE_GREETING:
                        return spanCount;
                    default:
                        return 1;
                }

            }
        });
        getViewDataBinding().stickerList.setLayoutManager(layoutManager);
        getViewDataBinding().stickerList.addItemDecoration(new GridSpaceItemDecoration(ViewUtils.dpToPx(20), spanCount));
        getViewDataBinding().stickerList.setAdapter(mAdapter);


        mViewModel.loadStickers();
        getViewDataBinding().stickerList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isStickersLoading && mViewModel.getNextStickerUrl() != null && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    isStickersLoading = true;
                    mViewModel.loadStickers();
                }
            }
        });
        baseProperties.put("post_id", mViewModel.postId);
    }


    private void updateViewHeight() {
        if (getContext() == null) return;
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ViewGroup.LayoutParams params = getViewDataBinding().container.getLayoutParams();
            params.height = (int) (ViewUtils.getScreenHeightInPx(getContext()) * 2 / 5);
            getViewDataBinding().container.setLayoutParams(params);
        } else {
            ViewGroup.LayoutParams params = getViewDataBinding().container.getLayoutParams();
            params.height = (int) (ViewUtils.getScreenHeightInPx(getContext()) * 0.55);
            getViewDataBinding().container.setLayoutParams(params);
        }
    }


//    public void adjustWindow(View view) {
//        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
//                FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
//
//                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    CoordinatorLayout.LayoutParams params;
//                    if (bottomSheet != null) {
//                        params = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
//                        params.setMargins(ViewUtils.dpToPx(80), 0, ViewUtils.dpToPx(80), 0);
//                        bottomSheet.setLayoutParams(params);
//                        if (dialog.getWindow() != null) {
//                            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//                        }
//
//                    }
//                }
//
//                BottomSheetBehavior behavior;
//                if (bottomSheet != null) {
//                    behavior = BottomSheetBehavior.from(bottomSheet);
//                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                }
//            }
//        });
//    }

    private AtomicInteger verticalScrollOffset = new AtomicInteger(0);

    public void setupRv() {
        getViewDataBinding().stickerList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int bottom, int i4, int i5, int i6, int oldBottom) {
                int y = oldBottom - bottom;
                if (y > 0) {
                    // if y is positive the keyboard is up else it's down
                    getViewDataBinding().stickerList.post(new Runnable() {
                        @Override
                        public void run() {
                            if (y > 0 || verticalScrollOffset.get() >= y) {
                                getViewDataBinding().stickerList.scrollBy(0, y);
                            } else {
                                getViewDataBinding().stickerList.scrollBy(0, verticalScrollOffset.get());
                            }
                        }
                    });
                }
            }
        });

        getViewDataBinding().stickerList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            AtomicInteger state = new AtomicInteger(RecyclerView.SCROLL_STATE_IDLE);

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                state.compareAndSet(RecyclerView.SCROLL_STATE_IDLE, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        if (!state.compareAndSet(RecyclerView.SCROLL_STATE_SETTLING, newState)) {
                            state.compareAndSet(RecyclerView.SCROLL_STATE_DRAGGING, newState);
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        state.compareAndSet(RecyclerView.SCROLL_STATE_IDLE, newState);
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        state.compareAndSet(RecyclerView.SCROLL_STATE_DRAGGING, newState);
                        break;

                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (state.get() != RecyclerView.SCROLL_STATE_IDLE) {
                    verticalScrollOffset.getAndAdd(dy);
                }
            }
        });
    }

    @Override
    public void onStickerSelected(Sticker sticker) {
        if (RewardManager.getInstance().getTotalCoin() >= sticker.getValue() && CommonUtils.isUserLoggedin()) {
            Map<String, Object> map = new HashMap<>(baseProperties);
            map.put("sticker_id", sticker.getId());
            map.put("sticker_type", sticker.getType());
            map.put("sticker_title", sticker.getTitle());
            map.put("sticker_value", sticker.getValue());
            map.put("is_first", CommonUtils.isFirstStickerSent());
            map.put("is_greetings", AppConstants.STICKER_TYPE_GREETING.equalsIgnoreCase(sticker.getType()));
            map.put("author", authorName);

            SegmentTracker.getInstance(getActivity()).trackEvent(EVENT_CHAT_STICKER_SENT, map);
        }

        if (!AppConstants.STICKER_TYPE_GREETING.equalsIgnoreCase(sticker.getType()))
            CommonUtils.setFirstStickerSent();
        if (mStickerSelectionListener != null)
            mStickerSelectionListener.onStickerSelected(sticker);

        dismiss();
    }

    @Override
    public void onStickerSelected(Sticker sticker, String message) {
        if (RewardManager.getInstance().getTotalCoin() >= sticker.getValue() && CommonUtils.isUserLoggedin()) {
            Map<String, Object> map = new HashMap<>(baseProperties);
            map.put("sticker_id", sticker.getId());
            map.put("sticker_type", sticker.getType());
            map.put("sticker_title", sticker.getTitle());
            map.put("sticker_value", sticker.getValue());
            map.put("is_first", CommonUtils.isFirstStickerSent());
            map.put("is_greetings", AppConstants.STICKER_TYPE_GREETING.equalsIgnoreCase(sticker.getType()));
            map.put("author", authorName);
            SegmentTracker.getInstance(getActivity()).trackEvent(EVENT_CHAT_STICKER_SENT, map);
        }
        if (!AppConstants.STICKER_TYPE_GREETING.equalsIgnoreCase(sticker.getType()))
            CommonUtils.setFirstStickerSent();
        if (mStickerSelectionListener != null) {
            mStickerSelectionListener.onStickerSelected(sticker, message);
        }
        dismiss();

    }

    public void setStickerSelectionListener(StickerGridRecyclerAdapter.StickerSelectionListener stickerSelectionListener) {
        this.mStickerSelectionListener = stickerSelectionListener;
    }

    @Override
    public void onDestroy() {
        if (mStickerSelectionListener != null)
            mStickerSelectionListener.onBottomSheetClose();
        super.onDestroy();
    }

    @Override
    public void onBottomSheetClose() {

    }
}
