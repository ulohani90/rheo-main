package com.rheotv.android.ui.activities.rank;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.useProfile.responses.AchievementsData;
import com.rheotv.android.data.network.models.useProfile.responses.StreamerLevel;
import com.rheotv.android.data.network.models.useProfile.responses.Target;
import com.rheotv.android.data.network.models.useProfile.responses.TargetData;
import com.rheotv.android.databinding.FragmentRankListBinding;
import com.rheotv.android.ui.adapters.LevelTargetsAdapter;
import com.rheotv.android.ui.adapters.LevelType;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.ui.decorators.LevelsItemDecorator;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.ViewUtils;
import com.rheotv.android.utils.segmentTracker.SegmentConstants;
import com.rheotv.android.utils.segmentTracker.SegmentTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RankListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RankListFragment extends BaseFragment<FragmentRankListBinding, RankFragmentViewModel> implements RankListNavigator {

    private static final String ARG_KEY_DEFINITION = "definition";
    private static final String ARG_KEY_LEVEL_DATA = "level_data";
    private static final String ARG_KEY_PAYMENT_MODEL = "payment_model";
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private RankFragmentViewModel mViewModel;

    private FragmentRankListBinding mBinding;
    private LevelType mLevelType;
    private StreamerLevel level;

    private HashMap<String, Object> baseProperties = new HashMap<>();

    public static RankListFragment newInstance(int paymentModel, StreamerLevel level, String definition, String source) {
        RankListFragment fragment = new RankListFragment();
        Bundle bundle = new Bundle();
        fragment.mLevelType = level.getLevelType();
        bundle.putParcelable(ARG_KEY_LEVEL_DATA, level);
        bundle.putInt(ARG_KEY_PAYMENT_MODEL, paymentModel);
        bundle.putString(ARG_KEY_DEFINITION, definition);
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static RankListFragment newInstance(LevelType levelType, String source) {
        RankListFragment fragment = new RankListFragment();
        Bundle bundle = new Bundle();
        fragment.mLevelType = levelType;
        bundle.putString(AppConstants.SCREEN_SOURCE, source);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_rank_list;
    }

    @Override
    public RankFragmentViewModel getViewModel() {
        mViewModel = new ViewModelProvider(this, mViewModelFactory).get(RankFragmentViewModel.class);
        mViewModel.setNavigator(this);
        mViewModel.currentWatchHourLiveData.observe(this, item -> {
            if (mViewModel.getRewardDefinition() != null) {
                mBinding.setRewardDefinition(mViewModel.getRewardDefinition());
                mBinding.rewardLayout.setVisibility(View.VISIBLE);
            }
            if (item == null) {
                mBinding.currentWatchHour.setVisibility(View.GONE);
            } else {
                mBinding.currentWatchHour.setVisibility(View.VISIBLE);
                mBinding.currentWatchHour.setText(item.trim());
            }
        });
        mViewModel.rheoCoinWatchHourLiveData.observe(this, item -> {
            if (mViewModel.getRewardDefinition() != null) {
                mBinding.setRewardDefinition(mViewModel.getRewardDefinition());
                mBinding.rewardLayout.setVisibility(View.VISIBLE);
            }
            if (item == null)
                mBinding.watchHourTable.setVisibility(View.GONE);
            else {
                mBinding.rewardLayout.setVisibility(View.VISIBLE);
                mBinding.watchHourTable.setVisibility(View.VISIBLE);
                mBinding.earnAsYouGrowBody.setText(item.trim());
            }
        });
        mViewModel.levelAchievementLiveData.observe(this, item -> {
            if (mViewModel.getRewardDefinition() != null) {
                mBinding.setRewardDefinition(mViewModel.getRewardDefinition());
                mBinding.rewardLayout.setVisibility(View.VISIBLE);
            }

            if (item == null)
                mBinding.levelAchievementTable.setVisibility(View.GONE);
            else {
                mBinding.rewardLayout.setVisibility(View.VISIBLE);
                mBinding.levelAchievementTable.setVisibility(View.VISIBLE);
                mBinding.levelAchievementBody.setText(item);
            }
        });
        return mViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        level = null;
        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey(ARG_KEY_PAYMENT_MODEL))
                mViewModel.setPaymentModel(getArguments().getInt(ARG_KEY_PAYMENT_MODEL, mViewModel.getPaymentModel()));
            if (arguments.containsKey(ARG_KEY_LEVEL_DATA))
                level = getArguments().getParcelable(ARG_KEY_LEVEL_DATA);
            if (arguments.containsKey(ARG_KEY_DEFINITION))
                mViewModel.setRewardDefinition(arguments.getString(ARG_KEY_DEFINITION));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();
        if (mViewModel.getPaymentModel() == 2)
            mViewModel.getAchievements(mLevelType);

        mBinding.currentLevelInfo.setVisibility(level != null && "present".equalsIgnoreCase(level.getState()) ? View.VISIBLE : View.GONE);
        boolean isTargetAccomplished = false;
        try {
            if (level.getTargets() != null && !level.getTargets().isEmpty()) {
                if (level.getTargets().get(0).getData() != null) {
                    for (TargetData targetData : level.getTargets().get(0).getData()) {
                        if (!targetData.isCompleted()) {
                            isTargetAccomplished = false;
                            break;
                        }
                        isTargetAccomplished = true;
                    }
                } else {
                    for (Target target : level.getTargets()) {
                        List<TargetData> targetDataList = new ArrayList<>();
                        TargetData targetData = new TargetData();
                        targetData.setAchievedValue(-1);
                        targetData.setTargetValue(-1);
                        targetData.setCompleted(true);
                        targetDataList.add(targetData);
                        target.setData(targetDataList);
                    }
                    isTargetAccomplished = true;
                }
            }else{
                isTargetAccomplished = true;
            }
            if (level.getCriteria() != null && !level.getCriteria().isEmpty()) {
                if (level.getCriteria().get(0).getData() != null) {
                    for (TargetData criteriaData : level.getCriteria().get(0).getData()) {
                        if (!criteriaData.isCompleted()) {
                            isTargetAccomplished = false;
                            break;
                        }
                        isTargetAccomplished = true;
                    }
                } else {
                    for (Target criteria : level.getCriteria()) {
                        List<TargetData> criteriaDataList = new ArrayList<>();
                        TargetData criteriaData = new TargetData();
                        criteriaData.setAchievedValue(-1);
                        criteriaData.setTargetValue(-1);
                        criteriaData.setCompleted(true);
                        criteriaDataList.add(criteriaData);
                        criteria.setData(criteriaDataList);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        mBinding.levelAccomplishmentLayout.setVisibility(isTargetAccomplished ? View.VISIBLE : View.GONE);
        mBinding.levelRequirementLayout.setAlpha(isTargetAccomplished ? 0.5f : 1f);
        if (getArguments() != null && getArguments().containsKey(AppConstants.SCREEN_SOURCE))
            baseProperties.put(AppConstants.SCREEN_SOURCE, getArguments().getString(AppConstants.SCREEN_SOURCE));
        baseProperties.put(AppConstants.SCREEN_NAME, SegmentConstants.SCREEN_NAME_RANK_INFO);
        SegmentTracker.getInstance(getContext()).recordScreenName(SegmentConstants.SCREEN_NAME_RANK_INFO, baseProperties);

        if (level.getCriteria() != null && !level.getCriteria().isEmpty()) {
            mBinding.criteriaRv.setVisibility(View.VISIBLE);
            mBinding.levelRequirementLayout.setVisibility(View.VISIBLE);
            LevelTargetsAdapter criteriaAdapter = new LevelTargetsAdapter(level.getCriteria());
            mBinding.criteriaRv.setAdapter(criteriaAdapter);
        } else {
            mBinding.criteriaRv.setVisibility(View.GONE);
        }

        if (level.getTargets() != null && !level.getTargets().isEmpty()) {
            mBinding.levelRequirementLayout.setVisibility(View.VISIBLE);
            mBinding.targetsRv.setVisibility(View.VISIBLE);
            LevelTargetsAdapter targetsAdapter = new LevelTargetsAdapter(level.getTargets());
            mBinding.targetsRv.setAdapter(targetsAdapter);
        } else {
            mBinding.targetsRv.setVisibility(View.GONE);
        }

        if (mViewModel.getPaymentModel() == 2) {
            while (!mActionQueue.isEmpty()) {
                mActionQueue.poll().run();
            }
        }
    }

    private void populateBonusTable(List<AchievementsData> list) {
        if (mLevelType instanceof LevelType.Gold) {
            mBinding.bonusPlanTitle.setText(R.string.reward_monthly_bonus_title);
            mBinding.columnWatchHour.setText(R.string.reward_monthly_watch_hours);
        } else {
            mBinding.bonusPlanTitle.setText(R.string.reward_bonus_title);
            mBinding.columnWatchHour.setText(R.string.reward_watch_hours);
        }
        for (AchievementsData achievementsData : list) {
            mBinding.bonusTable.addView(getTableRow(achievementsData, list.indexOf(achievementsData) == list.size() - 1));
        }
    }

    private View getTableRow(AchievementsData achievementsData, boolean isLastElement) {
        LinearLayout linearLayout = new LinearLayout(mBinding.getRoot().getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setWeightSum(2.0f);
        TableRow tableRow = new TableRow(mBinding.getRoot().getContext());
        int padding = ViewUtils.dpToPx(1);
        tableRow.setPadding(padding, 0, padding, padding);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tableRow.addView(getBonusCell("" + achievementsData.getTarget(), 0, isLastElement));
        tableRow.addView(getBonusCell("+" + achievementsData.getAmount(), 1, isLastElement));
        linearLayout.setLayoutParams(layoutParams);
        return tableRow;
    }

    private TextView getBonusCell(String text, int column, boolean isLastElement) {
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1f);
        int padding = ViewUtils.dpToPx(5);
        TextView textView = new TextView(mBinding.getRoot().getContext());
        textView.setId(View.generateViewId());
        if (isLastElement) {
            if (column == 0) {
                textView.setBackgroundResource(R.drawable.table_bottom_left_bg);
            } else {
                textView.setBackgroundResource(R.drawable.table_bottom_right_bg);
            }
        } else {
            textView.setBackgroundColor(Color.parseColor("#1d2e44"));
        }
        if (column == 1) {
            layoutParams.setMarginStart(ViewUtils.dpToPx(1));
        }
        textView.setPadding(padding, padding, padding, padding);
        textView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        textView.setTextColor(Color.parseColor("#fbfbfb"));
        textView.setTextSize(12f);
        textView.setText(text);
        textView.setLayoutParams(layoutParams);
        return textView;
    }


    private Queue<Runnable> mActionQueue = new LinkedList<>();

    @Override
    public void setRewardData(List<AchievementsData> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (mBinding == null) {
            mActionQueue.add(() -> {
                mBinding.rewardLayout.setVisibility(View.VISIBLE);
                mBinding.bonusTable.setVisibility(View.VISIBLE);
                populateBonusTable(list);
            });
        } else {
            mBinding.rewardLayout.setVisibility(View.VISIBLE);
            mBinding.bonusTable.setVisibility(View.VISIBLE);
            populateBonusTable(list);
        }
    }
}
