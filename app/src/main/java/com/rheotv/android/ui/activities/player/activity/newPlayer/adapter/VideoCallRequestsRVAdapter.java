package com.rheotv.android.ui.activities.player.activity.newPlayer.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rheotv.android.R;
import com.rheotv.android.data.network.models.postlisting.responses.VideoCallUsersListObject;
import com.rheotv.android.databinding.CallRequestItemLayoutBinding;
import com.rheotv.android.databinding.FooterLoadingLayoutBinding;
import com.rheotv.android.ui.activities.player.activity.newPlayer.viewmodel.VideoCallAction;
import com.rheotv.android.ui.base.BaseViewHolder;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.BindingUtils;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VideoCallRequestsRVAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    List<VideoCallUsersListObject> users = new ArrayList<>();

    List<Integer> userIds = new ArrayList<>();

    boolean showLoading;

    boolean isAuthor;

    int VIEW_TYPE_LOADING = 1;
    int VIEW_TYPE_USER = 2;

    int clickedPosition = -1;

    private OnActionClickListener mListener;

    public VideoCallRequestsRVAdapter() {

    }

    public void setAuthor(Boolean author) {
        isAuthor = author;
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
        if (showLoading) {
            notifyItemInserted(users.size());
        } else {
            notifyItemRemoved(users.size());
        }
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        this.mListener = listener;
    }

    public void addUser(List<VideoCallUsersListObject> users) {
        int startIndex = this.users.size();
        int addedSize = 0;
        for (VideoCallUsersListObject obj : users) {
            if (!userIds.contains(obj.getUserProfile().getUser().getId())) {
                this.users.add(obj);
                addedSize += 1;
                userIds.add(obj.getUserProfile().getUser().getId());
            }
        }
        notifyItemRangeInserted(startIndex, addedSize);
    }

    public void appendUser(VideoCallUsersListObject user) {
        if (userIds.contains(user.getUserProfile().getUser().getId())) {
            return;
        }
        int position = users.size();
        this.users.add(user);
        notifyItemInserted(position);
    }

    public void appendUserAtPosition(VideoCallUsersListObject user, int position) {
        if (userIds.contains(user.getUserProfile().getUser().getId())) {
            return;
        }
        if (users.size() == position - 1) {
            users.add(user);
            notifyItemInserted(users.size() - 1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == users.size()) {
            return VIEW_TYPE_LOADING;
        }
        return VIEW_TYPE_USER;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOADING) {
            FooterLoadingLayoutBinding footerLoadingLayoutBinding = FooterLoadingLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new FooterLoadingViewHolder(footerLoadingLayoutBinding);
        }
        CallRequestItemLayoutBinding binding = CallRequestItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CallRequestItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return users != null ? showLoading ? users.size() + 1 : users.size() : 0;
    }

    public void refreshData() {
        users.clear();
        userIds.clear();
        notifyDataSetChanged();
    }

    public void updateState(int position, String videoCallState) {
        users.get(position).setState(videoCallState);
        notifyItemChanged(position);
    }

    public void updateUserState(@Nullable VideoCallUsersListObject obj) {
        int pos = -1;
        for (VideoCallUsersListObject user : users) {
            pos += 1;
            if (user.getUserProfile().getUser().getId().equals(obj.getUserProfile().getUser().getId())) {
                if (Integer.parseInt(obj.getState()) <= Integer.parseInt(user.getState())) {
                    users.get(pos).setState(obj.getState());
                    notifyItemChanged(pos);
                }
                break;
            }
        }
    }

    class CallRequestItemHolder extends BaseViewHolder {
        CallRequestItemLayoutBinding mBinding;

        public CallRequestItemHolder(CallRequestItemLayoutBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            VideoCallUsersListObject user = users.get(position);
            BindingUtils.setImageUrlCircular(mBinding.profilePic, user.getUserProfile().getProfilePic(), 32, 32);
            mBinding.username.setText(user.getUserProfile().getUser().getUsername());


            if (isAuthor) {
                if (user.getState().equalsIgnoreCase(AppConstants
                        .VIDEO_CALL_STATE_INITIATED) || user.getState().equalsIgnoreCase(AppConstants
                        .VIDEO_CALL_STATE_IN_PROGRESS) || user.getState().equalsIgnoreCase(AppConstants
                        .VIDEO_CALL_STATE_ENDED) || user.getState().equalsIgnoreCase(AppConstants
                        .VIDEO_CALL_STATE_REQUESTED)) {
                    if (clickedPosition == position) {
                        mBinding.downArrow.setVisibility(View.VISIBLE);
                        mBinding.downArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_white);
                    } else {
                        mBinding.downArrow.setVisibility(View.VISIBLE);
                        mBinding.downArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_white);
                    }
                } else {
                    mBinding.downArrow.setVisibility(View.GONE);
                }

                if (clickedPosition == position) {
                    if (user.getState().equalsIgnoreCase(AppConstants.VIDEO_CALL_STATE_REQUESTED)) {
                        mBinding.btnGroup.setVisibility(View.VISIBLE);
                        mBinding.refundBtn.setVisibility(View.GONE);
                    } else if (user.getState().equalsIgnoreCase(AppConstants
                            .VIDEO_CALL_STATE_INITIATED) || user.getState().equalsIgnoreCase(AppConstants
                            .VIDEO_CALL_STATE_IN_PROGRESS) || user.getState().equalsIgnoreCase(AppConstants
                            .VIDEO_CALL_STATE_ENDED)) {
                        mBinding.btnGroup.setVisibility(View.GONE);
                        mBinding.refundBtn.setVisibility(View.VISIBLE);

                    } else {

                        mBinding.btnGroup.setVisibility(View.GONE);
                        mBinding.refundBtn.setVisibility(View.GONE);
                    }
                } else {
                    mBinding.btnGroup.setVisibility(View.GONE);
                    mBinding.refundBtn.setVisibility(View.GONE);
                }
                mBinding.parent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /*if (clickedPosition != -1) {
                            int lastClickedPos = clickedPosition;
                            if (clickedPosition == position) {
                                clickedPosition = -1;
                            } else {
                                clickedPosition = position;
                            }
                            notifyItemChanged(lastClickedPos);
                        } else {
                            clickedPosition = position;
                        }*/
                        if (clickedPosition == position) {
                            clickedPosition = -1;
                            mBinding.btnGroup.setVisibility(View.GONE);
                            mBinding.refundBtn.setVisibility(View.GONE);
                            if (user.getState().equalsIgnoreCase(AppConstants
                                    .VIDEO_CALL_STATE_INITIATED) || user.getState().equalsIgnoreCase(AppConstants
                                    .VIDEO_CALL_STATE_IN_PROGRESS) || user.getState().equalsIgnoreCase(AppConstants
                                    .VIDEO_CALL_STATE_ENDED) || user.getState().equalsIgnoreCase(AppConstants
                                    .VIDEO_CALL_STATE_REQUESTED)) {
                                mBinding.downArrow.setVisibility(View.VISIBLE);
                                mBinding.downArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_white);
                            } else {
                                mBinding.downArrow.setVisibility(View.GONE);
                            }
                        } else {
                            if (clickedPosition != -1) {
                                int lastClickedPos = clickedPosition;
                                clickedPosition = position;
                                notifyItemChanged(lastClickedPos);
                            } else {
                                clickedPosition = position;
                            }
                            notifyItemChanged(clickedPosition);
                        }
                    }
                });
                switch (user.getState()) {
                    case AppConstants
                            .VIDEO_CALL_STATE_REQUESTED:
                        mBinding.status.setVisibility(View.GONE);
                        break;
                    case AppConstants
                            .VIDEO_CALL_STATE_INITIATED:
                        mBinding.status.setVisibility(View.VISIBLE);
                        mBinding.status.setTextColor(Color.GREEN);
                        mBinding.status.setText("Accepted");
                        break;
                    case AppConstants
                            .VIDEO_CALL_STATE_IN_PROGRESS:
                        mBinding.status.setTextColor(Color.parseColor("#FFA500"));
                        mBinding.status.setVisibility(View.VISIBLE);
                        mBinding.status.setText("In Progress");
                        break;
                    case AppConstants
                            .VIDEO_CALL_STATE_ENDED:
                        mBinding.status.setTextColor(Color.WHITE);
                        mBinding.status.setVisibility(View.VISIBLE);
                        mBinding.status.setText("Completed");
                        break;
                    case AppConstants
                            .VIDEO_CALL_STATE_DENIED:
                        mBinding.status.setTextColor(Color.RED);
                        mBinding.status.setVisibility(View.VISIBLE);
                        mBinding.status.setText("Denied");

                        break;
                    case AppConstants
                            .VIDEO_CALL_STATE_REFUNDED:
                        mBinding.status.setTextColor(mBinding.status.getContext().getResources().getColor(R.color.color_accent));
                        mBinding.status.setVisibility(View.VISIBLE);
                        mBinding.status.setText("Refunded");
                        break;
                    default:
                        mBinding.status.setVisibility(View.GONE);
                        break;
                }

                mBinding.acceptBtn.setOnClickListener(view -> {
                    if (mListener != null) {
                        mListener.onActionClicked(position, null, user.getUserProfile().getUser().getId(), user.getUserProfile().getProfilePic(), VideoCallAction.Start.INSTANCE, user.getUserProfile().getUser().getUsername());
                    }
                });
                mBinding.denyBtn.setOnClickListener(view -> {
                    if (mListener != null) {
                        mListener.onActionClicked(position, null, user.getUserProfile().getUser().getId(), user.getUserProfile().getProfilePic(), VideoCallAction.Deny.INSTANCE, user.getUserProfile().getUser().getUsername());
                    }
                });
                mBinding.refundBtn.setOnClickListener(view -> {
                    if (mListener != null) {
                        mListener.onActionClicked(position, null, user.getUserProfile().getUser().getId(), user.getUserProfile().getProfilePic(), VideoCallAction.Refund.INSTANCE, user.getUserProfile().getUser().getUsername());
                    }
                });
            } else {
                mBinding.downArrow.setVisibility(View.GONE);
                mBinding.refundBtn.setVisibility(View.GONE);
                mBinding.status.setVisibility(View.GONE);
                mBinding.btnGroup.setVisibility(View.GONE);
            }
        }
    }

    public class FooterLoadingViewHolder extends BaseViewHolder {

        public FooterLoadingViewHolder(FooterLoadingLayoutBinding binding) {
            super(binding.getRoot());
        }

        @Override
        public void onBind(int position) {

        }
    }

    public interface OnActionClickListener {
        void onActionClicked(int position, String channelId, int userId, String userIcon, VideoCallAction action, String userName);
    }
}
