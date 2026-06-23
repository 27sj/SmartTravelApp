// ============================================================
// ProfileFragment.java — 我的碎片（升级版）
// 用户信息：智慧出行用户 + UID + 累计订单数 + 累计消费
// 功能入口：历史订单/优惠券/设置/关于
// ============================================================
package com.example.smarttravel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;

/**
 * ProfileFragment 我的碎片（升级版）
 * 显示用户信息与统计，提供功能入口
 */
public class ProfileFragment extends Fragment {

    private MaterialCardView cardHistory;
    private MaterialCardView cardCoupon;
    private MaterialCardView cardStatistics;
    private MaterialCardView cardSettings;
    private MaterialCardView cardAbout;

    private TextView tvOrderCount, tvTotalSpending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化视图
        cardHistory = view.findViewById(R.id.card_history);
        cardCoupon = view.findViewById(R.id.card_coupon);
        cardStatistics = view.findViewById(R.id.card_statistics);
        cardSettings = view.findViewById(R.id.card_settings);
        cardAbout = view.findViewById(R.id.card_about);
        tvOrderCount = view.findViewById(R.id.tv_profile_orders);
        tvTotalSpending = view.findViewById(R.id.tv_profile_spending);

        // 加载统计信息
        loadStatistics();

        // 设置点击事件
        cardHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), HistoryOrderActivity.class);
            startActivity(intent);
        });

        cardCoupon.setOnClickListener(v -> {
            com.google.android.material.snackbar.Snackbar.make(view,
                    "您有3张可用优惠券",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        });

        cardStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(),
                    com.example.smarttravel.statistics.StatisticsActivity.class);
            startActivity(intent);
        });

        cardSettings.setOnClickListener(v -> {
            com.google.android.material.snackbar.Snackbar.make(view,
                    "设置功能开发中",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        });

        cardAbout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 返回此页面时刷新统计
        loadStatistics();
    }

    /** 从数据库加载订单统计信息 */
    private void loadStatistics() {
        OrderDatabaseHelper dbHelper = new OrderDatabaseHelper(requireContext());
        int orderCount = dbHelper.getOrderCount();
        double totalSpending = dbHelper.getTotalSpending();

        tvOrderCount.setText(String.valueOf(orderCount));

        DecimalFormat df = new DecimalFormat("0.0");
        tvTotalSpending.setText("¥" + df.format(totalSpending));
    }
}
