package com.rheotv.android.ui.activities.player.activity.newPlayer;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.objects.PostObject;
import com.rheotv.android.utils.EventBusModel;
import com.rheotv.android.ui.activities.player.activity.CustomFragmentStateAdapter;
import com.rheotv.android.ui.activities.player.activity.StreamPlayerContainerViewModel;
import com.rheotv.android.ui.activities.player.activity.ViewPagerMediator;
import com.rheotv.android.utils.CommonUtils;
import com.rheotv.android.utils.RecyclerViewUtils;
import com.rheotv.android.utils.ViewUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;

public class ViewPagerMediatorV2 {
    private int startPosition;
    private ViewPager2 viewPager;
    private CustomFragmentStateAdapter adapter;
    private ViewPagerMediatorV2.FragmentOnPageChangeCallback onPageChangeCallback;
    private ViewPagerMediator.ViewPagerOnPageSelectedListener pageChangeListener;

    public ViewPagerMediatorV2(@NonNull ViewPager2 viewPager,
                               @NonNull CustomFragmentStateAdapter adapter,
                               int startPosition, ViewPagerMediator.ViewPagerOnPageSelectedListener pageChangeListener
    ) {
        this.viewPager = viewPager;
        //RecyclerViewUtils.enforceSingleScrollDirection(RecyclerViewUtils.getRecyclerView(viewPager));
        this.adapter = adapter;
        this.startPosition = startPosition;
        this.pageChangeListener = pageChangeListener;
    }

    /**
     * Unlink the ViewPager callback
     */
    public void attach() {
        onPageChangeCallback = new ViewPagerMediatorV2.FragmentOnPageChangeCallback(adapter);

        viewPager.setAdapter(adapter);
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
        viewPager.setCurrentItem(startPosition, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ViewPagerMediator.PageChange playerState) {
        if (playerState == ViewPagerMediator.PageChange.NEXT) {
            Log.i(getClass().getSimpleName(), "stream_jump_next");
            int positionToBeDeleted = viewPager.getCurrentItem();
            viewPager.setCurrentItem(positionToBeDeleted + 1, true);
            adapter.removeItem(positionToBeDeleted);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStreamEvent(EventBusModel playerState) {
        if (playerState instanceof EventBusModel.Next) {
            String id = ((EventBusModel.Next) playerState).getId();
            int pos = adapter.getPositionForId(id);
            if (pos == -1) return;
            viewPager.setCurrentItem(pos, true);
            adapter.removeItem(pos);
            Log.i(getClass().getSimpleName(), "stream_jump_next: " + id + " and " + pos);
        }
    }

    public void updateList(PostObject post) {
        if (post == null || adapter == null) return;
        if (post.isPublished()) {
            int positionToAdd = viewPager.getCurrentItem() < adapter.getItemCount() ? viewPager.getCurrentItem() : 0;
            if (StreamPlayerContainerViewModel.cardPosition != -1) {
                if (StreamPlayerContainerViewModel.cardPosition < adapter.getItemCount()) {
                    positionToAdd = StreamPlayerContainerViewModel.cardPosition;
                } else {
                    positionToAdd = adapter.getItemCount() - 1;
                }
            }
            adapter.addListItem(post, positionToAdd);
        } else {
            adapter.removeListItem(post);
        }

    }

    public void clearList() {
        if (onPageChangeCallback != null)
            onPageChangeCallback.resetPageChangeCallback();
        if (adapter != null)
            adapter.clearList();
    }

    public void addForcedPost(PostObject postObject) {
        if (onPageChangeCallback != null)
            onPageChangeCallback.resetPageChangeCallback();
        if (adapter != null)
            adapter.addItemAt(postObject, 0);
    }

    public enum PageChange {
        NEXT,
        PREVIOUS
    }

    /**
     * Unlink the ViewPager callback
     */
    public void detach() {
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
        onPageChangeCallback = null;
    }

    public void updateAdapter(List<PostObject> postObjects) {
        if (adapter.getItemCount() == 0) {
            adapter.addListItem(postObjects);
            viewPager.setCurrentItem(startPosition, false);
        } else {
            adapter.addListItem(postObjects);
        }
    }

    public boolean showAction(String Id, int notificationId) {
        int position = getPositionForId(Id);
        int currentIndex = getCurrentId();
        if (Id.hashCode() == currentIndex) {
            cancelNotification(notificationId);
            showPlayRequests(position);
            return true;
        } else if (hasId(Id)) {
            View view = getAnchorView();
            Context context = view.getContext();
            Snackbar.make(view, context.getString(R.string.custom_room_accepted_message), Snackbar.LENGTH_INDEFINITE)
                    .setAction(context.getString(R.string.view), v -> {
                        viewPager.setCurrentItem(position, true);
                        addEvent(position);
                    })
                    .setAnchorView(view)
                    .show();
            return true;
        }

        return false;
    }

    private void showPlayRequests(int position) {
        String message = "Custom Room update";
        Fragment fragment = adapter.createFragment(position);
        if (fragment instanceof StreamPlayerFragmentV2)
            ((StreamPlayerFragmentV2) fragment).showCustomRoomAcceptedEnable(message);
    }

    private void addEvent(int position) {
        Fragment fragment = adapter.createFragment(position);
        if (fragment instanceof StreamPlayerFragmentV2)
            ((StreamPlayerFragmentV2) fragment).addCustomRoomEvent();
    }

    private View getAnchorView() {
        FrameLayout layout = new FrameLayout(viewPager.getContext());
        int height = ViewUtils.getNavBarHeight(viewPager.getContext());
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height, Gravity.BOTTOM);
        param.bottomMargin = height + CommonUtils.toPix(32);
        ((ViewGroup) viewPager.getParent()).addView(layout, param);
        return layout;
    }

    private void cancelNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) viewPager.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public int getCurrentId() {
        return adapter.getIdAt(viewPager.getCurrentItem()).intValue();
    }

    public boolean hasId(String Id) {
        return adapter.containsId(Id);
    }

    public int getPositionForId(String Id) {
        return adapter.getPositionForId(Id);
    }

    private class FragmentOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        private WeakReference<CustomFragmentStateAdapter> adapterRef;
        private int index = -1;
        private int position;
        private boolean isInitialPageSelected = false;

        public FragmentOnPageChangeCallback(CustomFragmentStateAdapter adapter) {
            super();
            this.adapterRef = new WeakReference<>(adapter);
        }

        public void resetPageChangeCallback() {
            index = -1;
            isInitialPageSelected = false;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            this.position = position;
            if (index != position) {
                if (adapter.getItemCount() > 0 && (pageState == ViewPager2.SCROLL_STATE_IDLE || !isInitialPageSelected)) {
                    isInitialPageSelected = true;
                    CustomFragmentStateAdapter adapter = adapterRef.get();
                    if (index > 0)
                        EventBus.getDefault().post(new EventBusModel.UpdateBackPress(false));

                    Log.i(getClass().getSimpleName(), "pager_change: position " + position + " and " + index);

                   /* if (index != -1 && adapter.getItem(index) instanceof ViewPagerOnPageSelectedListener) {
                        //((ViewPagerOnPageSelectedListener) adapter.getItem(index)).onPageUnselected();
                    }

                    if (position < adapter.getItemCount() && adapter.getItem(position) instanceof ViewPagerOnPageSelectedListener) {
                        //((ViewPagerOnPageSelectedListener) adapter.getItem(position)).onPageSelected();
                    }*/

                    index = position;
                    if (pageChangeListener != null) {
                        pageChangeListener.onPageSelected();
                    }
                }
            }
        }

        private int pageState = ViewPager2.SCROLL_STATE_DRAGGING;

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            pageState = state;
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                onPageSelected(position);
            }
        }
    }

    /**
     * A callback interface that must be implemented to set selected and un-selected page
     */
    public interface ViewPagerOnPageSelectedListener {
        void onPageSelected();

        void onPageUnselected();
    }

}

