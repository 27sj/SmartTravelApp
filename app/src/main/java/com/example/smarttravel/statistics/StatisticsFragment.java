// ============================================================
// StatisticsFragment.java — 数据分析中心主Fragment
// 五大分析板块：出行需求/热力分布/方式结构/碳减排/运营效率
// 使用MPAndroidChart渲染图表，数据从数据库动态获取
// ============================================================
package com.example.smarttravel.statistics;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smarttravel.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * StatisticsFragment 数据分析中心
 * 智慧交通共享出行运营监测平台
 */
public class StatisticsFragment extends Fragment {

    private TabLayout tabLayout;
    private View[] sections = new View[5];
    private StatisticsRepository repository;

    // ===== 颜色主题 =====
    private static final int COLOR_PRIMARY = 0xFF1976D2;
    private static final int COLOR_ACCENT = 0xFF03A9F4;
    private static final int COLOR_GREEN = 0xFF4CAF50;
    private static final int COLOR_ORANGE = 0xFFFF9800;
    private static final int COLOR_RED = 0xFFE53935;
    private static final int COLOR_PURPLE = 0xFF7B1FA2;
    private static final int[] CHART_COLORS = {
            0xFF1976D2, 0xFF42A5F5, 0xFF03A9F4, 0xFF4CAF50, 0xFFFF9800,
            0xFFE53935, 0xFF7B1FA2, 0xFF00BCD4, 0xFFFF5722, 0xFF9C27B0
    };

    // ===== Dark theme colors =====
    private static final int DARK_TEXT = Color.parseColor("#FFE8ECF4");
    private static final int DARK_GRID = Color.parseColor("#FF1E2640");
    private static final int DARK_BLUE = Color.parseColor("#FF409EFF");
    private static final int DARK_GREEN = Color.parseColor("#FF52C41A");
    private static final int DARK_ORANGE = Color.parseColor("#FFFAAD14");
    private static final int DARK_RED = Color.parseColor("#FFF44336");

    private static final String[] DAY_LABELS = {"日", "一", "二", "三", "四", "五", "六"};
    private static final String[] TIME_SLOT_LABELS = {
            "0-6时", "6-9时", "9-12时", "12-18时", "18-22时", "22-24时"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new StatisticsRepository(requireContext());

        initViews(view);
        setupTabs(view);
        loadAllData();
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        sections[0] = view.findViewById(R.id.section_demand);
        sections[1] = view.findViewById(R.id.section_heat);
        sections[2] = view.findViewById(R.id.section_mode);
        sections[3] = view.findViewById(R.id.section_carbon);
        sections[4] = view.findViewById(R.id.section_operation);

        // 设置Tab标题
        String[] titles = {"出行需求", "热力分布", "方式结构", "碳减排", "运营效率"};
        for (int i = 0; i < titles.length; i++) {
            tabLayout.addTab(tabLayout.newTab().setText(titles[i]));
        }
    }

    private void setupTabs(View view) {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                for (int i = 0; i < sections.length; i++) {
                    sections[i].setVisibility(i == pos ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadAllData() {
        loadHeaderCards();
        loadDemandData();
        loadHeatData();
        loadModeData();
        loadCarbonData();
        loadOperationData();
    }

    // ================================================================
    // 1. 出行需求分析
    // ================================================================

    private void loadHeaderCards() {
        try {
            TextView tvOrders = getView().findViewById(R.id.tv_total_orders);
            TextView tvSpending = getView().findViewById(R.id.tv_total_spending);
            TextView tvCarbon = getView().findViewById(R.id.tv_total_carbon);

            int totalOrders = repository.getTripDemand().totalTrips;
            double totalSpending = repository.getTotalSpending();
            double totalCarbon = repository.getCarbonReduction().totalReduction;

            tvOrders.setText(String.valueOf(totalOrders));
            tvSpending.setText("¥" + new DecimalFormat("0.0").format(totalSpending));
            tvCarbon.setText(new DecimalFormat("0.0").format(totalCarbon));
        } catch (Exception ignored) {}
    }

    private void loadDemandData() {
        StatisticsEntity.TripDemand demand = repository.getTripDemand();

        TextView tvTotal = getView().findViewById(R.id.tv_total_trips);
        TextView tvDaily = getView().findViewById(R.id.tv_daily_avg);
        tvTotal.setText(formatNumber(demand.totalTrips));
        tvDaily.setText(new DecimalFormat("0.0").format(demand.dailyAvgTrips));

        setupWeeklyChart(demand.weeklyTrend);
        setupMonthlyChart(demand.monthlyTrend);
    }

    private void setupWeeklyChart(List<double[]> data) {
        BarChart chart = getView().findViewById(R.id.chart_weekly);
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.size() && i < 7; i++) {
            entries.add(new BarEntry(i, (float) data.get(i)[1]));
        }

        BarDataSet set = new BarDataSet(entries, "日订单量");
        set.setColor(DARK_BLUE);
        set.setValueTextSize(10f);

        BarData barData = new BarData(set);
        barData.setBarWidth(0.6f);

        chart.setData(barData);
        chart.setFitBars(true);
        chart.setDrawGridBackground(false);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.animateY(800);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setTextColor(DARK_TEXT);
        chart.getAxisLeft().setGridColor(DARK_GRID);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(DAY_LABELS));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(DARK_TEXT);
        xAxis.setGridColor(DARK_GRID);
        chart.invalidate();
    }

    private void setupMonthlyChart(List<double[]> data) {
        LineChart chart = getView().findViewById(R.id.chart_monthly);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.size() && i < 30; i++) {
            entries.add(new Entry((float) data.get(i)[0], (float) data.get(i)[1]));
        }

        LineDataSet set = new LineDataSet(entries, "日订单量");
        set.setColor(COLOR_PRIMARY);
        set.setLineWidth(2f);
        set.setCircleColor(COLOR_PRIMARY);
        set.setCircleRadius(3f);
        set.setValueTextSize(8f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(set);
        chart.setData(lineData);
        chart.setDrawGridBackground(false);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.animateX(1000);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setTextColor(DARK_TEXT);
        chart.getAxisLeft().setGridColor(DARK_GRID);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(8f);
        xAxis.setTextColor(DARK_TEXT);
        xAxis.setGridColor(DARK_GRID);
        xAxis.setLabelCount(6, true);
        chart.invalidate();
    }

    // ================================================================
    // 2. 时段热力分布
    // ================================================================

    private void loadHeatData() {
        StatisticsEntity.TimeHeatDistribution heat = repository.getTimeHeatDistribution();

        TextView tvAnalysis = getView().findViewById(R.id.tv_peak_analysis);
        tvAnalysis.setText(heat.peakAnalysis);

        setupTimeSlotLineChart(heat.slots);
        setupTimeSlotBarChart(heat.slots);
        setupTimeSlotDetail(heat.slots);
    }

    private void setupTimeSlotLineChart(List<double[]> data) {
        LineChart chart = getView().findViewById(R.id.chart_time_slot);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, (float) data.get(i)[1]));
        }

        LineDataSet set = new LineDataSet(entries, "订单占比 (%)");
        set.setColor(COLOR_ACCENT);
        set.setLineWidth(2.5f);
        set.setCircleColor(COLOR_PRIMARY);
        set.setCircleRadius(4f);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setFillColor(COLOR_ACCENT);
        set.setFillAlpha(40);

        LineData lineData = new LineData(set);
        chart.setData(lineData);
        chart.setDrawGridBackground(false);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.animateX(800);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setTextColor(DARK_TEXT);
        chart.getAxisLeft().setGridColor(DARK_GRID);
        chart.getAxisLeft().setValueFormatter(new PercentFormatter());

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(TIME_SLOT_LABELS));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(DARK_TEXT);
        xAxis.setGridColor(DARK_GRID);
        chart.invalidate();
    }

    private void setupTimeSlotBarChart(List<double[]> data) {
        BarChart chart = getView().findViewById(R.id.chart_heat_bar);
        List<BarEntry> entries = new ArrayList<>();
        int[] heatColors = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, (float) data.get(i)[0]));
            // 热力色：从蓝到红渐变
            float intensity = (float) data.get(i)[1] / 100f;
            heatColors[i] = Color.rgb(
                    (int) (50 + 205 * intensity),
                    (int) (150 * (1 - intensity) + 50),
                    (int) (200 * (1 - intensity))
            );
        }

        BarDataSet set = new BarDataSet(entries, "订单量");
        set.setColors(heatColors);
        set.setValueTextSize(10f);

        BarData barData = new BarData(set);
        barData.setBarWidth(0.6f);

        chart.setData(barData);
        chart.setFitBars(true);
        chart.setDrawGridBackground(false);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.animateY(800);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setTextColor(DARK_TEXT);
        chart.getAxisLeft().setGridColor(DARK_GRID);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(TIME_SLOT_LABELS));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(DARK_TEXT);
        xAxis.setGridColor(DARK_GRID);
        chart.invalidate();
    }

    private void setupTimeSlotDetail(List<double[]> data) {
        LinearLayout container = getView().findViewById(R.id.layout_time_detail);
        container.removeAllViews();

        for (int i = 0; i < data.size(); i++) {
            View row = LayoutInflater.from(requireContext())
                    .inflate(android.R.layout.simple_list_item_2, container, false);
            TextView tv1 = row.findViewById(android.R.id.text1);
            TextView tv2 = row.findViewById(android.R.id.text2);

            tv1.setText(TIME_SLOT_LABELS[i]);
            tv1.setTextSize(14f);
            tv1.setTextColor(0xFF212121);

            double count = data.get(i)[0];
            double pct = data.get(i)[1];
            tv2.setText(String.format(Locale.getDefault(),
                    "%d 单 (%.1f%%)", (int) count, pct));
            tv2.setTextSize(12f);
            tv2.setTextColor(0xFF757575);

            container.addView(row);
        }
    }

    // ================================================================
    // 3. 出行方式结构
    // ================================================================

    private void loadModeData() {
        StatisticsEntity.ModeShare mode = repository.getModeShare();

        setupPieChart(mode);
        setupDonutChart(mode);
        setupModeDetail(mode);
    }

    private void setupPieChart(StatisticsEntity.ModeShare mode) {
        PieChart chart = getView().findViewById(R.id.chart_mode_pie);
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < mode.shares.size(); i++) {
            entries.add(new PieEntry((float) mode.shares.get(i)[1], mode.labels[i]));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(new int[]{DARK_BLUE, DARK_ORANGE, DARK_GREEN});
        set.setValueTextSize(13f);
        set.setValueTextColor(Color.WHITE);
        set.setSliceSpace(2f);

        PieData pieData = new PieData(set);
        pieData.setValueFormatter(new PercentFormatter(chart));

        chart.setData(pieData);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.setUsePercentValues(true);
        chart.setCenterText("出行方式占比");
        chart.setCenterTextSize(14f);
        chart.setCenterTextColor(DARK_TEXT);
        chart.setEntryLabelColor(DARK_TEXT);
        chart.setEntryLabelTextSize(11f);
        chart.setDrawEntryLabels(true);
        chart.getLegend().setTextSize(11f);
        chart.getLegend().setTextColor(DARK_TEXT);
        chart.animateY(800);
        chart.setHoleRadius(0f);
        chart.setTransparentCircleRadius(0f);
        chart.invalidate();
    }

    private void setupDonutChart(StatisticsEntity.ModeShare mode) {
        PieChart chart = getView().findViewById(R.id.chart_mode_donut);
        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < mode.shares.size(); i++) {
            entries.add(new PieEntry((float) mode.shares.get(i)[1], mode.labels[i]));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(new int[]{DARK_BLUE, DARK_ORANGE, DARK_GREEN});
        set.setValueTextSize(13f);
        set.setValueTextColor(Color.WHITE);
        set.setSliceSpace(2f);

        PieData pieData = new PieData(set);
        pieData.setValueFormatter(new PercentFormatter(chart));

        chart.setData(pieData);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.setUsePercentValues(true);
        chart.setCenterText("市场占有率");
        chart.setCenterTextSize(14f);
        chart.setCenterTextColor(DARK_TEXT);
        chart.setEntryLabelColor(DARK_TEXT);
        chart.setEntryLabelTextSize(11f);
        chart.getLegend().setTextSize(11f);
        chart.getLegend().setTextColor(DARK_TEXT);
        chart.animateY(800);
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(55f);
        chart.invalidate();
    }

    private void setupModeDetail(StatisticsEntity.ModeShare mode) {
        TextView tv = getView().findViewById(R.id.tv_mode_detail);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mode.shares.size(); i++) {
            String label = mode.labels[i];
            double count = mode.shares.get(i)[0];
            double pct = mode.shares.get(i)[1];
            sb.append(String.format(Locale.getDefault(),
                    "%s：%d 单 (%.1f%%)\n", label, (int) count, pct));
        }
        tv.setText(sb.toString().trim());
    }

    // ================================================================
    // 4. 碳减排效益分析
    // ================================================================

    private void loadCarbonData() {
        StatisticsEntity.CarbonReduction carbon = repository.getCarbonReduction();

        DecimalFormat df = new DecimalFormat("0.0");
        ((TextView) getView().findViewById(R.id.tv_carbon_total))
                .setText(df.format(carbon.totalReduction));
        ((TextView) getView().findViewById(R.id.tv_carbon_monthly))
                .setText(df.format(carbon.monthlyReduction));
        ((TextView) getView().findViewById(R.id.tv_carbon_per_capita))
                .setText(df.format(carbon.perCapitaReduction));

        setupCarbonTrendChart(carbon.monthlyTrend);
    }

    private void setupCarbonTrendChart(List<double[]> data) {
        LineChart chart = getView().findViewById(R.id.chart_carbon_trend);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry((float) data.get(i)[0], (float) data.get(i)[1]));
        }

        LineDataSet set = new LineDataSet(entries, "碳减排量 (kgCO₂)");
        set.setColor(DARK_GREEN);
        set.setLineWidth(2.5f);
        set.setCircleColor(DARK_GREEN);
        set.setCircleRadius(4f);
        set.setValueTextSize(9f);
        set.setDrawFilled(true);
        set.setFillColor(DARK_GREEN);
        set.setFillAlpha(30);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(set);
        chart.setData(lineData);
        chart.setDrawGridBackground(false);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.animateX(1000);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setTextColor(DARK_TEXT);
        chart.getAxisLeft().setGridColor(DARK_GRID);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(DARK_TEXT);
        xAxis.setGridColor(DARK_GRID);
        xAxis.setLabelCount(6, true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "月";
            }
        });
        chart.invalidate();
    }

    // ================================================================
    // 5. 车辆运营效率分析
    // ================================================================

    private void loadOperationData() {
        StatisticsEntity.VehicleEfficiency ve = repository.getVehicleEfficiency();

        DecimalFormat df = new DecimalFormat("0.0");
        ((TextView) getView().findViewById(R.id.tv_avg_distance))
                .setText(df.format(ve.avgDistance));
        ((TextView) getView().findViewById(R.id.tv_avg_duration))
                .setText(String.valueOf((int) ve.avgServiceDuration));
        ((TextView) getView().findViewById(R.id.tv_util_rate))
                .setText(df.format(ve.utilizationRate));

        setupHighUtilChart(ve.highUtilTop10);
        setupLowUtilChart(ve.lowUtilTop10);
        setupUsageRankChart(ve.usageRanking);
    }

    private void setupHighUtilChart(List<String[]> data) {
        BarChart chart = getView().findViewById(R.id.chart_high_util);
        setupRankingBarChart(chart, data, DARK_GREEN, "利用率 (%)");
    }

    private void setupLowUtilChart(List<String[]> data) {
        BarChart chart = getView().findViewById(R.id.chart_low_util);
        setupRankingBarChart(chart, data, DARK_RED, "利用率 (%)");
    }

    private void setupUsageRankChart(List<String[]> data) {
        BarChart chart = getView().findViewById(R.id.chart_usage_rank);
        setupRankingBarChart(chart, data, DARK_BLUE, "使用次数");
    }

    private void setupRankingBarChart(BarChart chart, List<String[]> data, int color, String label) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int count = Math.min(data.size(), 10);

        for (int i = 0; i < count; i++) {
            String[] row = data.get(i);
            // 取数值部分
            String valStr = row[2].replace("%", "");
            float val = Float.parseFloat(valStr);
            entries.add(new BarEntry(i, val));
            labels.add(truncatePlate(row[0]));
        }

        BarDataSet set = new BarDataSet(entries, label);
        set.setColor(color);
        set.setValueTextSize(9f);

        BarData barData = new BarData(set);
        barData.setBarWidth(0.5f);

        chart.setData(barData);
        chart.setFitBars(true);
        chart.setDrawGridBackground(false);
        chart.setGridBackgroundColor(Color.TRANSPARENT);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.animateY(800);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisLeft().setTextColor(DARK_TEXT);
        chart.getAxisLeft().setGridColor(DARK_GRID);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(
                labels.toArray(new String[0])));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(8f);
        xAxis.setTextColor(DARK_TEXT);
        xAxis.setGridColor(DARK_GRID);
        xAxis.setLabelRotationAngle(-30);
        chart.invalidate();
    }

    private String truncatePlate(String plate) {
        if (plate == null) return "未知";
        // 简化车牌显示
        String prefix = plate.substring(0, Math.min(2, plate.length()));
        String suffix = plate.substring(Math.max(0, plate.length() - 3));
        return prefix + "..." + suffix;
    }

    // ================================================================
    // 工具方法
    // ================================================================

    private String formatNumber(int num) {
        if (num >= 10000) {
            return new DecimalFormat("0.0").format(num / 10000.0) + "万";
        }
        return String.valueOf(num);
    }
}
