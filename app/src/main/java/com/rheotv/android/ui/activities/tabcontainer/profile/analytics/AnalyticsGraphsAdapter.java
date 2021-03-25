package com.rheotv.android.ui.activities.tabcontainer.profile.analytics;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.rheotv.android.R;
import com.rheotv.android.data.network.models.GraphDataObject;
import com.rheotv.android.data.network.models.useProfile.responses.AnalyticsGraphObject;
import com.rheotv.android.databinding.ItemGraphLayoutBinding;
import com.rheotv.android.ui.base.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AnalyticsGraphsAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    List<AnalyticsGraphObject> graphObjects = new ArrayList<>();

    public AnalyticsGraphsAdapter() {

    }

    public AnalyticsGraphsAdapter(List<AnalyticsGraphObject> graphObjects) {
        this.graphObjects = graphObjects;
    }

    private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM");

    public void setGraphObjects(List<AnalyticsGraphObject> graphObjects) {
        if (graphObjects == null || graphObjects.isEmpty()) return;
        this.graphObjects.clear();
        this.graphObjects.addAll(graphObjects);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGraphLayoutBinding binding = ItemGraphLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GraphViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return graphObjects.size();
    }

    public class GraphViewHolder extends BaseViewHolder {
        ItemGraphLayoutBinding mBinding;

        public GraphViewHolder(ItemGraphLayoutBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        public void onBind(int position) {
            AnalyticsGraphObject obj = graphObjects.get(position);
            mBinding.chartWatchTimeTitle.setText(obj.getTitle());
            mBinding.chartWatchTime.clear();
            mBinding.chartWatchTime.setNoDataText("");
            mBinding.loading.setVisibility(View.VISIBLE);
            setUpMap(mBinding.chartWatchTime, obj.isShouldFormatDate());

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBinding.chartWatchTime.setNoDataText("No data available");
                    setData(obj.getGraphDataObjects(), mBinding.chartWatchTime, "DateSet" + position);
                    mBinding.loading.setVisibility(View.GONE);
                }
            }, 1000);

        }
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

        MyMarkerView mv = new MyMarkerView(chart.getContext(), R.layout.chart_marker_layout, shouldFormatValue);
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
        xAxis.setTextColor(chart.getContext().getResources().getColor(R.color.white_text_color));
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(chart.getContext().getResources().getColor(R.color.map_axis_line_color));
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
        leftAxis.setAxisLineColor(chart.getContext().getResources().getColor(R.color.map_axis_line_color));
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        //leftAxis.setAxisMinimum(0f);
        //leftAxis.setAxisMaximum(170f);
        leftAxis.setTextColor(chart.getResources().getColor(R.color.white_text_color));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setData(List<GraphDataObject> objs, LineChart chart, String label) {
//        Log.i(getClass().getName(), "setData objs: " + objs.size());
        // set data
        ArrayList<Entry> values = new ArrayList<>();
        for (GraphDataObject obj : objs) {
            values.add(new Entry(obj.getDate(), obj.getValue()));
            /*if (label.equals("DataSet1"))
                values.add(new Entry(obj.getDate(), obj.getViews()));
            else
                values.add(new Entry(obj.getDate(), obj.getDuration()));*/
//            Log.i(getClass().getName(), "setData objs: " + obj.getDate() + " and " + obj.getDuration());
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, label);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(chart.getContext().getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setValueTextColor(chart.getContext().getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(chart.getContext().getResources().getColor(R.color.bottom_bar_selected_item_color));
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);

        // create a data object with the data sets
        LineData data = new LineData(set1);
        data.setValueTextColor(chart.getContext().getResources().getColor(R.color.white_text_color));
        data.setValueTextSize(9f);
        chart.animateXY(1000, 1000);
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
