// ============================================================
// OrderDetailActivity.java — 订单详情页（升级版·含地图和路线）
// 集成高德地图 MapView，显示：
// 1. 乘客位置（蓝色标记）
// 2. 司机位置（绿色标记，随机偏移）
// 3. 司机→乘客的路线规划（Polyline）
// 4. 司机信息卡片 + 订单信息 + 行程进度
// ============================================================
package com.example.smarttravel;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Random;

/**
 * OrderDetailActivity 订单详情Activity（升级版）
 * 含地图、路线规划、司机信息、订单信息、行程进度
 */
public class OrderDetailActivity extends AppCompatActivity implements
        RouteSearch.OnRouteSearchListener {

    private MaterialToolbar toolbar;
    private MapView mapView;
    private AMap aMap;

    // 司机信息
    private TextView tvDriverName, tvDriverRating, tvDriverCar;
    private TextView tvPlateNo, tvOrderNo;
    private ImageView ivDriverAvatar;

    // 订单信息
    private TextView tvDetailStart, tvDetailEnd;
    private TextView tvOrderCarType, tvOrderPrice, tvOrderStatus;

    private String startPoint, endPoint, carType, price;
    private double startLat, startLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // 获取参数
        String orderNo = getIntent().getStringExtra("order_no");
        price = getIntent().getStringExtra("price");
        String driverName = getIntent().getStringExtra("driver_name");
        String plateNo = getIntent().getStringExtra("plate_no");
        double rating = getIntent().getDoubleExtra("rating", 4.9);
        String carModel = getIntent().getStringExtra("car_model");
        startPoint = getIntent().getStringExtra("start_point");
        endPoint = getIntent().getStringExtra("end_point");
        carType = getIntent().getStringExtra("car_type");
        startLat = getIntent().getDoubleExtra("start_lat", 30.28);
        startLng = getIntent().getDoubleExtra("start_lng", 120.15);

        // 初始化视图
        initViews(orderNo, price, driverName, plateNo, rating, carModel);

        // 初始化地图
        initMap(savedInstanceState);

        // 在地图上标记位置并规划路线
        if (startLat != 0 && startLng != 0) {
            setupMapMarkersAndRoute();
        }
    }

    private void initViews(String orderNo, String price, String driverName,
                           String plateNo, double rating, String carModel) {
        toolbar = findViewById(R.id.toolbar);
        mapView = findViewById(R.id.map_view);

        tvDriverName = findViewById(R.id.tv_driver_name);
        tvDriverRating = findViewById(R.id.tv_driver_rating);
        tvDriverCar = findViewById(R.id.tv_driver_car);
        tvPlateNo = findViewById(R.id.tv_plate_no);
        tvOrderNo = findViewById(R.id.tv_order_no);
        ivDriverAvatar = findViewById(R.id.iv_driver_avatar);

        tvDetailStart = findViewById(R.id.tv_detail_start);
        tvDetailEnd = findViewById(R.id.tv_detail_end);
        tvOrderCarType = findViewById(R.id.tv_order_car_type);
        tvOrderPrice = findViewById(R.id.tv_order_price);
        tvOrderStatus = findViewById(R.id.tv_order_status);

        toolbar.setNavigationOnClickListener(v -> finish());

        // 填充数据
        if (driverName != null) tvDriverName.setText(driverName);
        if (carModel != null) tvDriverCar.setText(carModel);
        if (plateNo != null) tvPlateNo.setText(plateNo);
        if (orderNo != null) tvOrderNo.setText(orderNo);
        tvDriverRating.setText(String.format("%.1f 分", rating));
        tvDetailStart.setText(startPoint != null ? startPoint : "当前位置");
        tvDetailEnd.setText(endPoint != null ? endPoint : "目的地");
        tvOrderCarType.setText(carType != null ? carType : "经济快车");
        tvOrderPrice.setText(price != null ? price : "¥0");
        tvOrderStatus.setText("进行中");

        // 设置司机头像颜色
        if (ivDriverAvatar != null) {
            ivDriverAvatar.setImageResource(R.drawable.ic_driver_avatar);
        }
    }

    private void initMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.getUiSettings().setZoomControlsEnabled(true);
        aMap.getUiSettings().setZoomGesturesEnabled(true);
        aMap.getUiSettings().setScrollGesturesEnabled(true);
    }

    private void setupMapMarkersAndRoute() {
        // 乘客位置（起点）
        LatLng passengerPos = new LatLng(startLat, startLng);

        // 司机位置：在乘客附近随机偏移（200~800米）
        Random random = new Random();
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = 300 + random.nextDouble() * 500; // 300~800米
        double latOffset = distance * Math.cos(angle) / 111319.0;
        double lngOffset = distance * Math.sin(angle) / (111319.0 * Math.cos(startLat * Math.PI / 180.0));
        LatLng driverPos = new LatLng(
                startLat + latOffset,
                startLng + lngOffset
        );

        // 添加乘客标记（蓝色）
        aMap.addMarker(new MarkerOptions()
                .position(passengerPos)
                .title("我的位置")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // 添加司机标记（绿色）
        aMap.addMarker(new MarkerOptions()
                .position(driverPos)
                .title("司机位置")
                .snippet(tvDriverName.getText().toString() + " · " + tvPlateNo.getText().toString())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // 移动相机到乘客位置（显示一定范围的视野）
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerPos, 14));

        // 请求路线规划
        requestRoute(driverPos, passengerPos);
    }

    private void requestRoute(LatLng from, LatLng to) {
        try {
            RouteSearch routeSearch = new RouteSearch(this);
            routeSearch.setRouteSearchListener(this);

            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(
                    new RouteSearch.FromAndTo(
                            new LatLonPoint(from.latitude, from.longitude),
                            new LatLonPoint(to.latitude, to.longitude)
                    ),
                    RouteSearch.DRIVING_SINGLE_DEFAULT,
                    null, null, ""
            );
            routeSearch.calculateDriveRouteAsyn(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
        if (rCode == 1000 && result != null && result.getPaths() != null
                && result.getPaths().size() > 0) {
            DrivePath path = result.getPaths().get(0);
            if (path.getSteps() != null) {
                // 绘制路线
                PolylineOptions polylineOptions = new PolylineOptions()
                        .color(getResources().getColor(R.color.md_primary))
                        .width(12f)
                        .setUseTexture(true);

                for (com.amap.api.services.route.DriveStep step : path.getSteps()) {
                    // 直接解析 LatLonPoint 列表
                    java.util.List<LatLonPoint> polylinePoints = step.getPolyline();
                    if (polylinePoints != null && polylinePoints.size() > 0) {
                        for (LatLonPoint point : polylinePoints) {
                            polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        }
                    }
                }

                aMap.addPolyline(polylineOptions);
            }
        }
    }

    @Override
    public void onBusRouteSearched(com.amap.api.services.route.BusRouteResult busRouteResult, int i) {}

    @Override
    public void onWalkRouteSearched(com.amap.api.services.route.WalkRouteResult walkRouteResult, int i) {}

    @Override
    public void onRideRouteSearched(com.amap.api.services.route.RideRouteResult rideRouteResult, int i) {}

    // ========================================================================
    // MapView 生命周期管理
    // ========================================================================

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
