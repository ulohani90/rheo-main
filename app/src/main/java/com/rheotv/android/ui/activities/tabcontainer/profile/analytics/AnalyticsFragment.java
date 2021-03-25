package com.rheotv.android.ui.activities.tabcontainer.profile.analytics;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.GraphDataObject;
import com.rheotv.android.databinding.AnalyticsFragmentBinding;
import com.rheotv.android.ui.base.BaseFragment;
import com.rheotv.android.utils.AppConstants;
import com.rheotv.android.utils.SharedPrefsUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class AnalyticsFragment extends BaseFragment<AnalyticsFragmentBinding, AnalyticsFragmentViewModel>
        implements AnalyticsFragmentNavigator {

    Context context;
    AnalyticsFragmentBinding universalFragmentBinding;

    LinearLayoutManager mLayoutManager;
    @Inject
    ViewModelProvider.Factory mViewModelFactory;
    private AnalyticsFragmentViewModel universalFragmentViewModel;
    public SharedPrefsUtils sharedPrefsUtils = new SharedPrefsUtils();

    boolean isMonthlyViewShown;

    AnalyticsGraphsAdapter mAdapter;

    private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM");

    public static AnalyticsFragment newInstance(String creatorUserName) {
        Bundle args = new Bundle();
        args.putString(AppConstants.AUTHOR_NAME, creatorUserName);
        AnalyticsFragment fragment = new AnalyticsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.rheotv.android.BR.viewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.analytics_fragment;
    }

    @Override
    public AnalyticsFragmentViewModel getViewModel() {
        universalFragmentViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AnalyticsFragmentViewModel.class);
        return universalFragmentViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        universalFragmentViewModel.setNavigator(this);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        universalFragmentBinding = getViewDataBinding();
        setUp();
        //setUpMap(universalFragmentBinding.chart, false);
        //setUpMap(universalFragmentBinding.chartWatchTime, true);
        setUpRecyclerView();
        universalFragmentViewModel.getStreamerData("me", "monthly");
        universalFragmentBinding.progressBar.setVisibility(View.VISIBLE);

        subscribeToLiveData();

    }

    private void setUpRecyclerView() {
        mAdapter = new AnalyticsGraphsAdapter();
        universalFragmentBinding.graphRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        universalFragmentBinding.graphRv.setAdapter(mAdapter);
    }

    private void setUpMap(LineChart chart, boolean shouldFormatValue) {
        Entry entry = new Entry();
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        MyMarkerView mv = new MyMarkerView(context, R.layout.chart_marker_layout, shouldFormatValue);
        // Set the marker to the chart
        mv.setChartView(chart);
        chart.setMarker(mv);

        // set an alternative background color
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setViewPortOffsets(60f, 0f, 60f, 60f);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //xAxis.setTypeface(tfLight);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(getResources().getColor(R.color.white_text_color));
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(getResources().getColor(R.color.map_axis_line_color));
        // xAxis.setTextColor(Color.rgb(255, 255, 255));
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new ValueFormatter() {

            @Override
            public String getFormattedValue(float timeInMillis) {

                return mFormat.format(new Date((long) timeInMillis));
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        //leftAxis.setTypeface(tfLight);
        //leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawAxisLine(true);
        leftAxis.setAxisLineColor(getResources().getColor(R.color.map_axis_line_color));
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        //leftAxis.setAxisMinimum(0f);
        //leftAxis.setAxisMaximum(170f);
        leftAxis.setTextColor(getResources().getColor(R.color.white_text_color));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    private void setUp() {
        isMonthlyViewShown = true;
        universalFragmentBinding.monthly.setSelected(true);
        universalFragmentBinding.weekly.setSelected(false);
        universalFragmentBinding.monthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMonthlyViewShown) {
                    universalFragmentBinding.detailsLayout.setVisibility(View.GONE);
                    universalFragmentViewModel.getStreamerData("me", "monthly");
                    universalFragmentBinding.progressBar.setVisibility(View.VISIBLE);
                    universalFragmentBinding.graphRv.setVisibility(View.GONE);
                    universalFragmentBinding.monthly.setSelected(true);
                    universalFragmentBinding.weekly.setSelected(false);
                    isMonthlyViewShown = true;
                }
            }
        });
        universalFragmentBinding.weekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMonthlyViewShown) {
                    universalFragmentBinding.detailsLayout.setVisibility(View.GONE);
                    universalFragmentBinding.graphRv.setVisibility(View.GONE);
                    universalFragmentViewModel.getStreamerData("me", "weekly");
                    universalFragmentBinding.progressBar.setVisibility(View.VISIBLE);
                    universalFragmentBinding.weekly.setSelected(true);
                    universalFragmentBinding.monthly.setSelected(false);
                    isMonthlyViewShown = false;
                }
            }
        });
    }

    private void subscribeToLiveData() {
        universalFragmentViewModel.getProfileData().observe(this, streamerData -> {
            universalFragmentBinding.detailsLayout.setVisibility(View.VISIBLE);
            universalFragmentBinding.totalViews.setText("Total Views - " + streamerData.getTotalViews() + "");
            universalFragmentBinding.totalFollowers.setText("Total Followers - " + streamerData.getTotalFollowers() + "");
            universalFragmentBinding.totalHoursStreamed.setText("Total Hours Streamed - " + streamerData.getTotalHoursStreamed() + "");
            universalFragmentBinding.totalWatchTime.setText("Total Watch Hours - " + streamerData.getTotalWatchTime());
            universalFragmentBinding.progressBar.setVisibility(View.GONE);
            mAdapter.setGraphObjects(streamerData.getGraphObjects());
            universalFragmentBinding.graphRv.setVisibility(View.VISIBLE);
            //setData(streamerData.getDailyViews(), universalFragmentBinding.chart, "DataSet1");
            //setData(streamerData.getDailyWatchTime(), universalFragmentBinding.chartWatchTime, "DataSet2");
        });
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public void throwError() {
        universalFragmentBinding.progressBar.setVisibility(View.GONE);
    }

    private void setData(List<GraphDataObject> objs, LineChart chart, String label) {
        Log.i(getClass().getName(), "setData objs: " + objs.size());
        // set data
        ArrayList<Entry> values = new ArrayList<>();
        for (GraphDataObject obj : objs) {
            if (label.equals("DataSet1"))
                values.add(new Entry(obj.getDate(), obj.getViews()));
            else
                values.add(new Entry(obj.getDate(), obj.getDuration()));
            Log.i(getClass().getName(), "setData objs: " + obj.getDate() + " and " + obj.getDuration());
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, label);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setValueTextColor(getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);

        // create a data object with the data sets
        LineData data = new LineData(set1);
        data.setValueTextColor(getResources().getColor(R.color.white_text_color));
        data.setValueTextSize(9f);
        chart.setData(data);
        chart.invalidate();
    }

    public class MyMarkerView extends MarkerView {

        private final TextView dateTV;

        private final TextView valueTV;

        private boolean shouldFormat;

        public MyMarkerView(Context context, int layoutResource, boolean shouldFormat) {
            super(context, layoutResource);

            dateTV = findViewById(R.id.dateTV);
            valueTV = findViewById(R.id.valueTV);
            this.shouldFormat = shouldFormat;
        }

        // runs every time the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (shouldFormat) {
                if (e instanceof CandleEntry) {
                    CandleEntry ce = (CandleEntry) e;
                    dateTV.setText(mFormat.format(new Date((long) ce.getX())));
                    valueTV.setText(String.valueOf(ce.getHigh()));
                } else {
                    dateTV.setText(mFormat.format(new Date((long) e.getX())));
                    valueTV.setText(String.valueOf(e.getY()));
                }
            } else {
                if (e instanceof CandleEntry) {
                    CandleEntry ce = (CandleEntry) e;
                    dateTV.setText(mFormat.format(new Date((long) ce.getX())));
                    valueTV.setText(Utils.formatNumber(ce.getHigh(), 0, true));
                } else {
                    dateTV.setText(mFormat.format(new Date((long) e.getX())));
                    valueTV.setText(Utils.formatNumber(e.getY(), 0, true));
                }
            }

            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}