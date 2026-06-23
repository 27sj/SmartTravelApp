// ============================================================
// OrderConfirmActivity.java — 订单确认页（升级版·随机司机匹配）
// 点击确认呼叫后显示"正在匹配附近司机..."动画
// 2秒后随机生成司机/车辆/车牌/评分，保存到SQLite并跳转详情
// ============================================================
package com.example.smarttravel;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import com.example.smarttravel.database.MessageDatabaseHelper;
import com.example.smarttravel.model.MessageInfo;

/**
 * OrderConfirmActivity 订单确认Activity
 * 展示订单概要，模拟随机匹配司机流程
 */
public class OrderConfirmActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvStart, tvEnd;
    private TextView tvCarType, tvPrice;
    private MaterialButton btnConfirm;
    private MaterialCardView layoutMatching;
    private TextView tvMatchingText;        // 匹配中的文字提示

    private String startPoint, endPoint, carType, price;
    private double startLat, startLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirm);

        // 获取传递参数
        startPoint = getIntent().getStringExtra("start_point");
        endPoint = getIntent().getStringExtra("end_point");
        carType = getIntent().getStringExtra("car_type");
        price = getIntent().getStringExtra("price");
        startLat = getIntent().getDoubleExtra("start_lat", 0.0);
        startLng = getIntent().getDoubleExtra("start_lng", 0.0);

        // 初始化视图
        toolbar = findViewById(R.id.toolbar);
        tvStart = findViewById(R.id.tv_confirm_start);
        tvEnd = findViewById(R.id.tv_confirm_end);
        tvCarType = findViewById(R.id.tv_confirm_car_type);
        tvPrice = findViewById(R.id.tv_confirm_price);
        btnConfirm = findViewById(R.id.btn_confirm_order);
        layoutMatching = findViewById(R.id.layout_matching);
        tvMatchingText = findViewById(R.id.tv_matching_text);

        // 工具栏返回按钮
        toolbar.setNavigationOnClickListener(v -> finish());

        // 填充数据
        tvStart.setText(startPoint);
        tvEnd.setText(endPoint);
        tvCarType.setText(carType);
        tvPrice.setText(price);

        // ===== 确认呼叫按钮 =====
        btnConfirm.setOnClickListener(v -> {
            // 显示匹配遮罩 && 更新文案
            layoutMatching.setVisibility(View.VISIBLE);
            tvMatchingText.setText("正在匹配附近司机...");
            btnConfirm.setEnabled(false);

            // === 第一阶段：0.5秒后更新进度 ===
            new Handler().postDelayed(() -> {
                tvMatchingText.setText("已找到附近司机，分配中...");
            }, 500);

            // === 第二阶段：1.2秒后更新进度 ===
            new Handler().postDelayed(() -> {
                tvMatchingText.setText("司机正在赶来...");
            }, 1200);

            // === 第三阶段：2秒后匹配完成，生成随机数据 ===
            new Handler().postDelayed(() -> {
                layoutMatching.setVisibility(View.GONE);

                // ===== 随机生成司机信息 =====
                String driverName = RandomDataGenerator.getRandomDriverName();
                String plateNo = RandomDataGenerator.getRandomPlateNo();
                double rating = RandomDataGenerator.getRandomRating();
                String carModel = RandomDataGenerator.getRandomCarModel();
                int carImageResId = RandomDataGenerator.getRandomCarImageRes();
                String orderNo = RandomDataGenerator.generateOrderNo();
                String currentTime = RandomDataGenerator.getCurrentTime();

                // ===== 保存到 SQLite =====
                OrderInfo order = new OrderInfo(orderNo, startPoint, endPoint,
                        carType, price, currentTime, 0); // 0=进行中
                order.setDriverName(driverName);
                order.setPlateNo(plateNo);
                order.setRating(rating);
                order.setCarModel(carModel);
                order.setCarImageResId(carImageResId);

                OrderDatabaseHelper dbHelper = new OrderDatabaseHelper(OrderConfirmActivity.this);
                long result = dbHelper.insertOrder(order);
                android.util.Log.i("OrderConfirm", "订单保存成功，ID=" + result);

                // ===== 插入消息通知 =====
                MessageDatabaseHelper msgDb = new MessageDatabaseHelper(OrderConfirmActivity.this);

                // 消息1：接单通知
                MessageInfo msg1 = new MessageInfo("接单通知",
                        "您的订单已被" + driverName + "接单。", currentTime);
                msgDb.insertMessage(msg1);

                // 消息2：司机即将到达
                MessageInfo msg2 = new MessageInfo("司机到达提醒",
                        driverName + "即将到达上车点，请做好准备。", currentTime);
                msgDb.insertMessage(msg2);

                // 消息3：行程开始
                MessageInfo msg3 = new MessageInfo("行程开始",
                        "您的行程已开始。", currentTime);
                msgDb.insertMessage(msg3);

                // 消息4：订单完成
                MessageInfo msg4 = new MessageInfo("订单完成",
                        "订单已完成，感谢使用。", currentTime);
                msgDb.insertMessage(msg4);

                android.util.Log.i("OrderConfirm", "消息通知已插入");

                // ===== 跳转到订单详情页 =====
                Intent intent = new Intent(OrderConfirmActivity.this, OrderDetailActivity.class);
                intent.putExtra("order_no", orderNo);
                intent.putExtra("price", price);
                intent.putExtra("driver_name", driverName);
                intent.putExtra("plate_no", plateNo);
                intent.putExtra("rating", rating);
                intent.putExtra("car_model", carModel);
                intent.putExtra("car_image_res", carImageResId);
                intent.putExtra("start_point", startPoint);
                intent.putExtra("end_point", endPoint);
                intent.putExtra("car_type", carType);
                intent.putExtra("start_lat", startLat);
                intent.putExtra("start_lng", startLng);
                startActivity(intent);

                finish();
            }, 2000);
        });
    }
}
