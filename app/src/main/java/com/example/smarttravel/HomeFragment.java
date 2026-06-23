// ============================================================
// HomeFragment.java — 首页碎片（高德地图·车辆信息展示增强版）
// 功能：
// 1. 显示高德地图
// 2. 自动定位当前位置，显示"XX市XX区XX路"格式
// 3. 搜索目的地（POI搜索 + InputTips）
// 4. 点击地图选择终点
// 5. BottomSheet 显示起点/终点/费用
// 6. 随机生成周边车辆（共享单车/共享汽车/拼车，三种Marker）
// 7. 车辆Marker点击→BottomSheetDialog弹出车辆详情
// ============================================================
package com.example.smarttravel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.animation.ValueAnimator;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.example.smarttravel.model.VehicleDetail;
import com.example.smarttravel.model.VehicleRepository;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment 首页碎片 — 地图车辆信息展示
 * 实时定位 + 三种车辆Marker + 车辆详情弹窗 + POI搜索 + 点击选点
 */
public class HomeFragment extends Fragment implements
        LocationSource,
        AMapLocationListener,
        AMap.OnMapClickListener,
        AMap.OnMarkerClickListener,
        PoiSearch.OnPoiSearchListener,
        Inputtips.InputtipsListener,
        GeocodeSearch.OnGeocodeSearchListener {

    // ===== 视图 =====
    private MapView mapView;
    private TextInputEditText etDestination;
    private TextView tvCurrentLocation;
    private TextView tvDestinationDisplay;
    private TextView tvStartPoint;
    private TextView tvEstimatedFee;
    private MaterialButton btnCallTaxi;
    private View bottomSheet;

    // ===== 高德地图 =====
    private AMap aMap;
    private OnLocationChangedListener locationChangedListener;
    private AMapLocationClient locationClient;

    // ===== 状态 =====
    private LatLng currentLatLng;           // 当前位置
    private LatLng destinationLatLng;       // 终点位置
    private Marker destinationMarker;       // 终点标记
    private String destinationAddress = ""; // 终点地址名称
    private boolean isFirstLocate = true;   // 是否首次定位
    private boolean vehiclesGenerated = false; // 是否已生成车辆

    // ===== 附近车辆 =====
    private List<Marker> vehicleMarkers = new ArrayList<>();
    private List<VehicleDetail> vehicleDataList = new ArrayList<>();
    private VehicleRepository vehicleRepository;

    // ===== 动画相关 =====
    private List<ValueAnimator> markerAnimators = new ArrayList<>();

    // ===== 搜索相关 =====
    private PoiSearch poiSearch;
    private GeocodeSearch geocodeSearch;
    private static final int REQUEST_LOCATION_PERMISSION = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view, savedInstanceState);
        initMap();
        initBottomSheet();
        initSearchInput();
        checkAndRequestPermission();

        vehicleRepository = new VehicleRepository(requireContext());

        try {
            geocodeSearch = new GeocodeSearch(requireContext());
            geocodeSearch.setOnGeocodeSearchListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "地理编码服务初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews(View view, Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        tvCurrentLocation = view.findViewById(R.id.tv_home_location);
        tvStartPoint = view.findViewById(R.id.tv_home_location);
        tvDestinationDisplay = view.findViewById(R.id.et_home_destination);
        etDestination = view.findViewById(R.id.et_home_destination);
        tvEstimatedFee = view.findViewById(R.id.tv_est_fee);
        btnCallTaxi = view.findViewById(R.id.btn_go_now);
        bottomSheet = view.findViewById(R.id.bottom_sheet);

        // 初始显示
        tvCurrentLocation.setText("定位中...");
        tvStartPoint.setText("定位中...");

        btnCallTaxi.setOnClickListener(v -> onCallTaxiClicked());
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        aMap.setLocationSource(this);

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        myLocationStyle.interval(2000);
        myLocationStyle.strokeWidth(1.0f);
        myLocationStyle.strokeColor(0x50000000);
        myLocationStyle.radiusFillColor(0x10000000);
        aMap.setMyLocationStyle(myLocationStyle);

        aMap.setMyLocationEnabled(true);
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);

        aMap.getUiSettings().setZoomControlsEnabled(true);
        aMap.getUiSettings().setZoomGesturesEnabled(true);
        aMap.getUiSettings().setScrollGesturesEnabled(true);
        aMap.getUiSettings().setRotateGesturesEnabled(true);

        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    private void initBottomSheet() {
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setPeekHeight(160);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        behavior.setHideable(false);
    }

    private void initSearchInput() {
        etDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 2 && currentLatLng != null) {
                    searchTips(keyword);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etDestination.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etDestination.getText().toString().trim();
                if (!keyword.isEmpty() && currentLatLng != null) {
                    searchPoi(keyword);
                }
                return true;
            }
            return false;
        });
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION);
        } else {
            startLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation();
            } else {
                tvCurrentLocation.setText("定位失败，请手动选择位置");
                tvStartPoint.setText("定位失败，请手动选择位置");
                Toast.makeText(getContext(), "定位权限被拒绝", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ========================================================================
    // 定位相关
    // ========================================================================

    private void startLocation() {
        try {
            locationClient = new AMapLocationClient(requireContext());
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(false);
            option.setInterval(3000);
            option.setNeedAddress(true);
            locationClient.setLocationOption(option);
            locationClient.setLocationListener(this);
            locationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        locationChangedListener = listener;
    }

    @Override
    public void deactivate() {
        locationChangedListener = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
            // 定位成功
            double lat = aMapLocation.getLatitude();
            double lng = aMapLocation.getLongitude();
            currentLatLng = new LatLng(lat, lng);

            if (locationChangedListener != null) {
                locationChangedListener.onLocationChanged(aMapLocation);
            }

            // 格式化地址：XX市XX区XX路
            String city = aMapLocation.getCity();
            String district = aMapLocation.getDistrict();
            String street = aMapLocation.getStreet();
            String streetNum = aMapLocation.getStreetNum();

            StringBuilder addressSb = new StringBuilder();
            if (city != null && !city.isEmpty()) {
                addressSb.append(city);
            }
            if (district != null && !district.isEmpty()) {
                addressSb.append(district);
            }
            if (street != null && !street.isEmpty()) {
                addressSb.append(street);
                if (streetNum != null && !streetNum.isEmpty()) {
                    addressSb.append(streetNum);
                }
            }

            String displayAddress = addressSb.toString();
            if (displayAddress.isEmpty()) {
                String fullAddr = aMapLocation.getAddress();
                if (fullAddr != null && !fullAddr.isEmpty()) {
                    displayAddress = fullAddr;
                } else {
                    displayAddress = "当前位置";
                }
            }

            // 首次定位：移动到当前位置
            if (isFirstLocate) {
                isFirstLocate = false;
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));

                // 更新显示
                tvCurrentLocation.setText(displayAddress);
                tvStartPoint.setText(displayAddress);

                // 生成周边车辆（三种类型）
                generateNearbyVehicles();
            } else {
                tvCurrentLocation.setText(displayAddress);
                tvStartPoint.setText(displayAddress);
            }
        } else {
            // 定位失败
            if (aMapLocation != null) {
                Log.e("HomeFragment", "定位失败: " + aMapLocation.getErrorInfo());
            }
            if (isFirstLocate) {
                tvCurrentLocation.setText("定位失败，请手动选择位置");
                tvStartPoint.setText("定位失败，请手动选择位置");
                isFirstLocate = false;
            }
        }
    }

    // ========================================================================
    // 生成周边车辆（共享单车/共享汽车/拼车）
    // 三种类型采用不同颜色Marker
    // 位置：500m～1000m范围内随机分布
    // ========================================================================

    private void generateNearbyVehicles() {
        if (currentLatLng == null || vehiclesGenerated) return;
        vehiclesGenerated = true;

        // 清除旧Marker
        clearVehicleMarkers();

        // 从Repository获取车辆数据（自动生成）
        vehicleDataList = vehicleRepository.getNearbyVehicles(
                currentLatLng.latitude, currentLatLng.longitude);

        for (VehicleDetail v : vehicleDataList) {
            int iconRes;
            switch (v.getVehicleType()) {
                case "bike":
                    iconRes = R.drawable.ic_marker_bike;   // 绿色
                    break;
                case "car":
                    iconRes = R.drawable.ic_marker_car;    // 蓝色
                    break;
                case "carpool":
                    iconRes = R.drawable.ic_marker_carpool; // 橙色
                    break;
                default:
                    iconRes = R.drawable.ic_marker_car;
            }

            LatLng pos = new LatLng(v.getLatitude(), v.getLongitude());

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(pos)
                    .title(v.getMarkerTitle())
                    .snippet(v.getMarkerSnippet())
                    .icon(BitmapDescriptorFactory.fromResource(iconRes))
                    .anchor(0.5f, 0.5f);

            Marker marker = aMap.addMarker(markerOptions);
            // 把车辆信息关联到Marker上（通过object存储）
            marker.setObject(v);
            vehicleMarkers.add(marker);
            // 启动标记浮动动画
            startMarkerFloatAnimation(marker);
        }

        Log.i("HomeFragment", "已生成" + vehicleDataList.size() + "辆周边车辆"
                + "（单车:" + countByType("bike")
                + " 汽车:" + countByType("car")
                + " 拼车:" + countByType("carpool") + "）");
    }

    private int countByType(String type) {
        int count = 0;
        for (VehicleDetail v : vehicleDataList) {
            if (type.equals(v.getVehicleType())) count++;
        }
        return count;
    }

    // ========================================================================
    // Marker浮动动画 — ValueAnimator循环修改anchor实现上下浮动效果
    // ========================================================================

    private void startMarkerFloatAnimation(Marker marker) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(2000);
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            float yOffset = value * -10f; // -10 pixels up and down
            float anchorY = 0.5f + yOffset / 100f;
            if (anchorY < 0f) anchorY = 0f;
            if (anchorY > 1f) anchorY = 1f;
            marker.setAnchor(0.5f, anchorY);
        });
        animator.start();
        markerAnimators.add(animator);
    }

    private void clearVehicleMarkers() {
        for (Marker m : vehicleMarkers) {
            m.remove();
        }
        vehicleMarkers.clear();
        vehicleDataList.clear();
    }

    // ========================================================================
    // Marker点击 → 弹出车辆详情BottomSheetDialog
    // ========================================================================

    @Override
    public boolean onMarkerClick(Marker marker) {
        // 目的地Marker — 显示信息窗
        if (marker.equals(destinationMarker)) {
            marker.showInfoWindow();
            return true;
        }

        // 车辆Marker — 从object取VehicleDetail数据，弹出详情弹窗
        Object obj = marker.getObject();
        if (obj instanceof VehicleDetail) {
            VehicleDetail vehicle = (VehicleDetail) obj;
            // 点击时轻微缩放相机，增强交互反馈
            aMap.animateCamera(CameraUpdateFactory.zoomTo(aMap.getCameraPosition().zoom + 0.5f), 300, null);
            showVehicleDetailDialog(vehicle);
            return true;
        }

        marker.showInfoWindow();
        return false;
    }

    @SuppressLint("DefaultLocale")
    private void showVehicleDetailDialog(VehicleDetail vehicle) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        @SuppressLint("InflateParams")
        View sheetView = getLayoutInflater()
                .inflate(R.layout.bottom_sheet_vehicle_detail, null);
        dialog.setContentView(sheetView);

        // === 类型标签 ===
        TextView tvTypeBadge = sheetView.findViewById(R.id.tv_veh_type_badge);
        tvTypeBadge.setText(vehicle.getTypeLabel());
        // 根据类型设置标签背景色
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(12);
        switch (vehicle.getVehicleType()) {
            case "bike":
                badgeBg.setColor(0xFF4CAF50);
                break;
            case "car":
                badgeBg.setColor(0xFF1976D2);
                break;
            case "carpool":
                badgeBg.setColor(0xFFFF9800);
                break;
        }
        tvTypeBadge.setBackground(badgeBg);

        // === 车辆名称 ===
        ((TextView) sheetView.findViewById(R.id.tv_veh_name))
                .setText(vehicle.getVehicleName());

        // === 基础信息 ===
        ((TextView) sheetView.findViewById(R.id.tv_veh_id))
                .setText(vehicle.getVehicleId());
        ((TextView) sheetView.findViewById(R.id.tv_veh_plate))
                .setText(vehicle.getPlateNumber().isEmpty() ? "无" : vehicle.getPlateNumber());
        ((TextView) sheetView.findViewById(R.id.tv_veh_type))
                .setText(vehicle.getTypeLabel());

        // === 车辆状态 ===
        TextView tvStatus = sheetView.findViewById(R.id.tv_veh_status);
        tvStatus.setText(vehicle.getStatusLabel());
        int statusColor;
        switch (vehicle.getVehicleStatus()) {
            case 0: statusColor = 0xFF4CAF50; break;  // 空闲-绿
            case 1: statusColor = 0xFFFF9800; break;   // 使用中-橙
            case 2: statusColor = 0xFFF44336; break;   // 即将离线-红
            case 3: statusColor = 0xFF9E9E9E; break;   // 维护中-灰
            default: statusColor = 0xFF757575;
        }
        tvStatus.setTextColor(statusColor);

        // === 能源信息 ===
        ((TextView) sheetView.findViewById(R.id.tv_veh_battery))
                .setText(String.format("%d%%", (int) vehicle.getBatteryLevel()));

        // === 续航 ===
        ((TextView) sheetView.findViewById(R.id.tv_veh_range))
                .setText(String.format("%d km", (int) vehicle.getRemainingRange()));

        // === 费用明细 ===
        DecimalFormat df = new DecimalFormat("0.0");
        ((TextView) sheetView.findViewById(R.id.tv_veh_base_fare))
                .setText("¥" + df.format(vehicle.getBaseFare()));
        ((TextView) sheetView.findViewById(R.id.tv_veh_per_km))
                .setText("¥" + df.format(vehicle.getPerKmFare()) + "/km");
        ((TextView) sheetView.findViewById(R.id.tv_veh_per_min))
                .setText("¥" + df.format(vehicle.getPerMinFare()) + "/min");

        // === 距离 ===
        double dist = vehicle.getDistance();
        String distText;
        if (dist < 1000) {
            distText = String.format("%.0f m", dist);
        } else {
            distText = String.format("%.1f km", dist / 1000);
        }
        ((TextView) sheetView.findViewById(R.id.tv_veh_distance)).setText(distText);

        // === 预计总费用 ===
        ((TextView) sheetView.findViewById(R.id.tv_veh_estimated_cost))
                .setText("¥" + df.format(vehicle.getEstimatedCost()));

        // === 选择此车辆按钮 ===
        MaterialButton btnSelect = sheetView.findViewById(R.id.btn_select_vehicle);
        btnSelect.setOnClickListener(v -> {
            dialog.dismiss();
            // 跳转到订单创建页面，携带车辆信息
            Intent intent = new Intent(getActivity(), CarSelectActivity.class);
            intent.putExtra("vehicle_id", vehicle.getVehicleId());
            intent.putExtra("vehicle_type", vehicle.getVehicleType());
            intent.putExtra("vehicle_name", vehicle.getVehicleName());
            intent.putExtra("plate_number", vehicle.getPlateNumber());
            intent.putExtra("latitude", vehicle.getLatitude());
            intent.putExtra("longitude", vehicle.getLongitude());
            intent.putExtra("estimated_cost", vehicle.getEstimatedCost());
            startActivity(intent);
        });

        dialog.show();
    }

    // ========================================================================
    // 地图点击 → 选择终点
    // ========================================================================

    @Override
    public void onMapClick(LatLng latLng) {
        destinationLatLng = latLng;

        if (destinationMarker != null) {
            destinationMarker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("终点")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true);
        destinationMarker = aMap.addMarker(markerOptions);

        reverseGeocode(latLng);
    }

    private void reverseGeocode(LatLng latLng) {
        if (geocodeSearch != null) {
            LatLonPoint point = new LatLonPoint(latLng.latitude, latLng.longitude);
            RegeocodeQuery query = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);
            geocodeSearch.getFromLocationAsyn(query);
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 1000 && result != null && result.getRegeocodeAddress() != null) {
            destinationAddress = result.getRegeocodeAddress().getFormatAddress();
            tvDestinationDisplay.setText(destinationAddress);
            tvDestinationDisplay.setTextColor(
                    getResources().getColor(R.color.color_text_primary));
            calculateEstimatedFee();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {}

    // ========================================================================
    // POI 搜索
    // ========================================================================

    private void searchTips(String keyword) {
        InputtipsQuery inputQuery = new InputtipsQuery(keyword, "");
        if (currentLatLng != null) {
            inputQuery.setLocation(
                    new LatLonPoint(currentLatLng.latitude, currentLatLng.longitude));
        }
        inputQuery.setCityLimit(true);
        try {
            Inputtips inputTips = new Inputtips(requireContext(), inputQuery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetInputtips(List<Tip> tips, int rCode) {
        if (rCode == 1000 && tips != null && tips.size() > 0) {
            Tip firstTip = tips.get(0);
            String name = firstTip.getName();
            LatLonPoint point = firstTip.getPoint();

            if (name != null && point != null) {
                destinationAddress = name;
                destinationLatLng = new LatLng(point.getLatitude(), point.getLongitude());

                tvDestinationDisplay.setText(name);
                tvDestinationDisplay.setTextColor(
                        getResources().getColor(R.color.color_text_primary));

                if (destinationMarker != null) {
                    destinationMarker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(destinationLatLng)
                        .title(name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                destinationMarker = aMap.addMarker(markerOptions);

                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 16));
                calculateEstimatedFee();
            }
        }
    }

    private void searchPoi(String keyword) {
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "");
        query.setPageSize(5);
        query.setPageNum(0);
        if (currentLatLng != null) {
            query.setLocation(
                    new LatLonPoint(currentLatLng.latitude, currentLatLng.longitude));
        }
        try {
            poiSearch = new PoiSearch(requireContext(), query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 1000 && result != null && result.getPois() != null
                && result.getPois().size() > 0) {
            PoiItem firstPoi = result.getPois().get(0);
            LatLonPoint point = firstPoi.getLatLonPoint();
            destinationLatLng = new LatLng(point.getLatitude(), point.getLongitude());
            destinationAddress = firstPoi.getTitle();

            tvDestinationDisplay.setText(destinationAddress);
            tvDestinationDisplay.setTextColor(
                    getResources().getColor(R.color.color_text_primary));

            if (destinationMarker != null) {
                destinationMarker.remove();
            }
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(destinationLatLng)
                    .title(destinationAddress)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            destinationMarker = aMap.addMarker(markerOptions);

            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 16));
            calculateEstimatedFee();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {}

    // ========================================================================
    // 费用计算
    // ========================================================================

    private void calculateEstimatedFee() {
        if (currentLatLng == null || destinationLatLng == null) {
            tvEstimatedFee.setText("--");
            return;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                currentLatLng.latitude, currentLatLng.longitude,
                destinationLatLng.latitude, destinationLatLng.longitude,
                results);

        double distanceKm = results[0] / 1000.0;
        double fee;
        if (distanceKm <= 3.0) {
            fee = 8.0;
        } else {
            fee = 8.0 + (distanceKm - 3.0) * 2.5;
        }

        DecimalFormat df = new DecimalFormat("0.0");
        tvEstimatedFee.setText("¥" + df.format(fee));
    }

    // ========================================================================
    // 叫车按钮
    // ========================================================================

    private void onCallTaxiClicked() {
        if (destinationLatLng == null || destinationAddress.isEmpty()) {
            Toast.makeText(getContext(), "请先选择目的地", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), CarSelectActivity.class);
        intent.putExtra("start_point", tvStartPoint.getText().toString());
        intent.putExtra("end_point", destinationAddress);
        intent.putExtra("start_lat", currentLatLng != null ? currentLatLng.latitude : 0.0);
        intent.putExtra("start_lng", currentLatLng != null ? currentLatLng.longitude : 0.0);
        intent.putExtra("end_lat", destinationLatLng.latitude);
        intent.putExtra("end_lng", destinationLatLng.longitude);
        startActivity(intent);
    }

    // ========================================================================
    // MapView 生命周期管理
    // ========================================================================

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消所有标记动画
        for (ValueAnimator anim : markerAnimators) {
            if (anim != null && anim.isRunning()) {
                anim.cancel();
            }
        }
        markerAnimators.clear();
        clearVehicleMarkers();
        mapView.onDestroy();

        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
            locationClient = null;
        }

        if (aMap != null) {
            aMap.setLocationSource(null);
            aMap.clear();
            aMap = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
