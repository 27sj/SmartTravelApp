// ============================================================
// HistoryOrderActivity.java — 历史订单列表页
// 查询 SQLite 数据库中的历史订单，使用 RecyclerView 展示
// 长按可删除指定订单
// ============================================================
package com.example.smarttravel;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * HistoryOrderActivity 历史订单Activity
 * 从 SQLite 数据库读取订单记录并展示为列表
 * 支持长按删除单条记录
 */
public class HistoryOrderActivity extends AppCompatActivity {

    // 视图引用
    private MaterialToolbar toolbar;                // 顶部工具栏
    private RecyclerView recyclerHistory;           // 订单列表
    private LinearLayout layoutEmpty;               // 空状态

    // 数据
    private OrderDatabaseHelper dbHelper;           // 数据库帮助类
    private HistoryOrderAdapter adapter;            // 列表适配器
    private List<OrderInfo> orderList;              // 订单数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);

        // 初始化数据库
        dbHelper = new OrderDatabaseHelper(this);

        // 初始化视图
        toolbar = findViewById(R.id.toolbar);
        recyclerHistory = findViewById(R.id.recycler_history);
        layoutEmpty = findViewById(R.id.layout_empty);

        // 工具栏返回按钮
        toolbar.setNavigationOnClickListener(v -> finish());

        // 配置 RecyclerView
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));

        // ===== 加载数据并刷新界面 =====
        refreshOrderList();

        // ===== 删除监听：长按弹出确认对话框 =====
        if (adapter != null) {
            adapter.setOnDeleteClickListener((order, position) -> {
                // 弹出确认删除对话框
                new AlertDialog.Builder(this)
                        .setTitle("删除订单")
                        .setMessage("确定删除此订单？\n" + order.getStartPoint() + " → " + order.getEndPoint())
                        .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                            // 从数据库删除
                            dbHelper.deleteOrder(order.getOrderNo());
                            // 从列表移除
                            orderList.remove(position);
                            adapter.updateData(orderList);
                            // 显示提示
                            Snackbar.make(recyclerHistory, "订单已删除",
                                    Snackbar.LENGTH_SHORT).show();
                            // 判断是否为空状态
                            checkEmptyState();
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
            });
        }
    }

    /**
     * 从数据库获取数据并刷新列表
     */
    private void refreshOrderList() {
        // 查询所有历史订单
        orderList = dbHelper.getAllOrders();

        if (adapter == null) {
            // 首次创建适配器
            adapter = new HistoryOrderAdapter(orderList);
            recyclerHistory.setAdapter(adapter);
        } else {
            // 更新数据
            adapter.updateData(orderList);
        }

        // 检查是否为空状态
        checkEmptyState();
    }

    /**
     * 检查列表是否为空，控制空状态显示
     */
    private void checkEmptyState() {
        if (orderList == null || orderList.isEmpty()) {
            recyclerHistory.setVisibility(android.view.View.GONE);
            layoutEmpty.setVisibility(android.view.View.VISIBLE);
        } else {
            recyclerHistory.setVisibility(android.view.View.VISIBLE);
            layoutEmpty.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到页面时刷新数据（可能从其他页面添加了新订单）
        refreshOrderList();
    }
}
