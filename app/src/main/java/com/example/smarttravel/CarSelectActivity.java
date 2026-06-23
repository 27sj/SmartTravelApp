// ============================================================
// CarSelectActivity.java — 车型选择页（升级版·随机等待时间）
// 每次打开页面时，四种车型的预计到达时间在2~8分钟内随机变化
// ============================================================
package com.example.smarttravel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * CarSelectActivity 车型选择Activity
 * 展示四种车型，预计到达时间每次随机生成
 */
public class CarSelectActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvStartPoint, tvEndPoint;
    private RecyclerView recyclerCarTypes;

    private CarTypeAdapter adapter;
    private String startPoint, endPoint;
    private double startLat, startLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_select);

        // 获取参数
        startPoint = getIntent().getStringExtra("start_point");
        endPoint = getIntent().getStringExtra("end_point");
        startLat = getIntent().getDoubleExtra("start_lat", 0.0);
        startLng = getIntent().getDoubleExtra("start_lng", 0.0);
        if (startPoint == null) startPoint = "当前位置";
        if (endPoint == null) endPoint = "目的地";

        // 初始化视图
        toolbar = findViewById(R.id.toolbar);
        tvStartPoint = findViewById(R.id.tv_start_point);
        tvEndPoint = findViewById(R.id.tv_end_point);
        recyclerCarTypes = findViewById(R.id.recycler_car_types);

        toolbar.setNavigationOnClickListener(v -> finish());
        tvStartPoint.setText(startPoint);
        tvEndPoint.setText(endPoint);

        // ===== 生成模拟车型数据（每次随机等待时间） =====
        List<CarType> carTypeList = new ArrayList<>();
        // 每次刷新随机生成不同的等待时间
        carTypeList.add(new CarType("经济快车", R.drawable.ic_car_economy,
                RandomDataGenerator.getRandomArriveMinutes() + "分钟", "¥12"));
        carTypeList.add(new CarType("优享快车", R.drawable.ic_car_premium,
                RandomDataGenerator.getRandomArriveMinutes() + "分钟", "¥22"));
        carTypeList.add(new CarType("出租车", R.drawable.ic_car_taxi,
                RandomDataGenerator.getRandomArriveMinutes() + "分钟", "¥15"));
        carTypeList.add(new CarType("拼车", R.drawable.ic_car_carpool,
                RandomDataGenerator.getRandomArriveMinutes() + "分钟", "¥8"));

        // 配置 RecyclerView
        recyclerCarTypes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarTypeAdapter(carTypeList);
        recyclerCarTypes.setAdapter(adapter);

        // 点击事件
        adapter.setOnItemClickListener(carType -> {
            Intent intent = new Intent(CarSelectActivity.this, OrderConfirmActivity.class);
            intent.putExtra("start_point", startPoint);
            intent.putExtra("end_point", endPoint);
            intent.putExtra("car_type", carType.getName());
            intent.putExtra("price", carType.getPrice());
            intent.putExtra("start_lat", startLat);
            intent.putExtra("start_lng", startLng);
            startActivity(intent);
        });
    }
}
