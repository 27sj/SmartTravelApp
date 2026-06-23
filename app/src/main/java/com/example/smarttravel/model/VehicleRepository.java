// ============================================================
// VehicleRepository.java — 车辆数据仓库
// SQLite + Repository模式，自动生成测试车辆数据
// 支持共享单车/共享汽车/拼车三种类型
// ============================================================
package com.example.smarttravel.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * VehicleRepository 车辆数据仓库
 * 管理车辆数据的存储、生成与查询
 * 支持后续扩展真实车辆数据接口
 */
public class VehicleRepository {

    private static final String DB_NAME = "smart_travel_vehicles.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "t_vehicle_detail";

    private static final String C_ID = "_id";
    private static final String C_VEHICLE_ID = "vehicle_id";
    private static final String C_VEHICLE_TYPE = "vehicle_type";
    private static final String C_VEHICLE_NAME = "vehicle_name";
    private static final String C_PLATE = "plate_number";
    private static final String C_STATUS = "vehicle_status";
    private static final String C_BATTERY = "battery_level";
    private static final String C_FUEL = "fuel_level";
    private static final String C_RANGE = "remaining_range";
    private static final String C_COST = "estimated_cost";
    private static final String C_LAT = "latitude";
    private static final String C_LNG = "longitude";
    private static final String C_TIME = "create_time";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE + " (" +
                    C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    C_VEHICLE_ID + " TEXT, " +
                    C_VEHICLE_TYPE + " TEXT, " +
                    C_VEHICLE_NAME + " TEXT, " +
                    C_PLATE + " TEXT, " +
                    C_STATUS + " INTEGER DEFAULT 0, " +
                    C_BATTERY + " REAL DEFAULT 100, " +
                    C_FUEL + " REAL DEFAULT 100, " +
                    C_RANGE + " REAL DEFAULT 50, " +
                    C_COST + " REAL DEFAULT 0, " +
                    C_LAT + " REAL, " +
                    C_LNG + " REAL, " +
                    C_TIME + " TEXT)";

    private final VehicleDBHelper dbHelper;
    private final Random random = new Random();

    public VehicleRepository(Context context) {
        this.dbHelper = new VehicleDBHelper(context);
    }

    // ===== DB 内部帮助类 =====
    private static class VehicleDBHelper extends SQLiteOpenHelper {
        VehicleDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
        }
    }

    // ===== CRUD =====

    /** 插入车辆 */
    public long insert(VehicleDetail v) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toValues(v);
        long id = db.insert(TABLE, null, cv);
        db.close();
        return id;
    }

    /** 批量插入 */
    public void insertAll(List<VehicleDetail> list) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (VehicleDetail v : list) {
                db.insert(TABLE, null, toValues(v));
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /** 查询所有车辆 */
    public List<VehicleDetail> getAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<VehicleDetail> list = new ArrayList<>();
        Cursor c = db.query(TABLE, null, null, null, null, null, null);
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();
        db.close();
        return list;
    }

    /** 按类型查询 */
    public List<VehicleDetail> getByType(String type) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<VehicleDetail> list = new ArrayList<>();
        Cursor c = db.query(TABLE, null, C_VEHICLE_TYPE + "=?",
                new String[]{type}, null, null, null);
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();
        db.close();
        return list;
    }

    /** 清空表 */
    public void clearAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
    }

    // ===== 批量生成测试车辆 =====

    /**
     * 获取周边车辆
     * 如果数据库为空，自动生成一批测试车辆
     * 每次调用重新生成（模拟真实场景）
     */
    public List<VehicleDetail> getNearbyVehicles(double centerLat, double centerLng) {
        // 先清空旧数据
        clearAll();

        // 生成三种类型的混合车辆
        List<VehicleDetail> vehicles = new ArrayList<>();

        // 共享单车：10~15辆
        int bikeCount = 10 + random.nextInt(6);
        for (int i = 0; i < bikeCount; i++) {
            vehicles.add(generateBike(centerLat, centerLng, i));
        }

        // 共享汽车：6~10辆
        int carCount = 6 + random.nextInt(5);
        for (int i = 0; i < carCount; i++) {
            vehicles.add(generateCar(centerLat, centerLng, i));
        }

        // 拼车：4~6辆
        int carpoolCount = 4 + random.nextInt(3);
        for (int i = 0; i < carpoolCount; i++) {
            vehicles.add(generateCarpool(centerLat, centerLng, i));
        }

        // 写入数据库
        insertAll(vehicles);

        return vehicles;
    }

    // ===== 各类型车辆生成 =====

    private String[] bikeNames = {
            "美团单车V3", "哈啰单车A5", "青桔单车X1",
            "美团助力车E2", "哈啰助力车M1", "青桔电单车C3"
    };

    private String[] carNames = {
            "比亚迪秦PLUS", "比亚迪汉EV", "特斯拉Model3",
            "大众朗逸", "丰田卡罗拉", "日产轩逸",
            "小鹏P7", "广汽埃安S", "吉利星瑞"
    };

    private String[] provinces = {"粤", "京", "沪", "苏", "浙", "川", "湘"};
    private char[] cityLetters = {'A', 'B', 'C', 'D', 'E', 'F', 'G'};

    private VehicleDetail generateBike(double centerLat, double centerLng, int idx) {
        VehicleDetail v = new VehicleDetail();
        v.setVehicleId("BIKE-" + String.format(Locale.getDefault(), "%04d", idx + 1));
        v.setVehicleType("bike");
        v.setVehicleName(bikeNames[random.nextInt(bikeNames.length)]);
        v.setPlateNumber("");            // 共享单车无车牌
        v.setVehicleStatus(random.nextInt(100) < 70 ? 0 : 3); // 70%空闲, 30%维护中
        v.setBatteryLevel(30 + random.nextInt(71));  // 30~100%
        v.setFuelLevel(0);
        v.setRemainingRange(5 + random.nextDouble() * 45);  // 5~50km
        v.setBaseFare(1.0);
        v.setPerKmFare(0.5);
        v.setPerMinFare(0.1);

        // 位置偏移
        double[] pos = randomOffset(centerLat, centerLng, 200, 800);
        v.setLatitude(pos[0]);
        v.setLongitude(pos[1]);
        v.setDistance(calcDistance(centerLat, centerLng, pos[0], pos[1]));

        v.setEstimatedCost(v.getBaseFare() + 3.0 * v.getPerKmFare() + 15 * v.getPerMinFare());
        v.setCreateTime(getCurrentTime());
        return v;
    }

    private VehicleDetail generateCar(double centerLat, double centerLng, int idx) {
        VehicleDetail v = new VehicleDetail();
        v.setVehicleId("CAR-" + String.format(Locale.getDefault(), "%04d", idx + 1));
        v.setVehicleType("car");
        v.setVehicleName(carNames[random.nextInt(carNames.length)]);
        v.setPlateNumber(generatePlate());
        v.setVehicleStatus(random.nextInt(100) < 60 ? 0 : 1); // 60%空闲, 40%使用中
        v.setBatteryLevel(40 + random.nextInt(61));    // 40~100%
        v.setFuelLevel(30 + random.nextInt(71));       // 30~100%
        v.setRemainingRange(30 + random.nextDouble() * 320); // 30~350km
        v.setBaseFare(8.0);
        v.setPerKmFare(2.5);
        v.setPerMinFare(0.5);

        double[] pos = randomOffset(centerLat, centerLng, 300, 1000);
        v.setLatitude(pos[0]);
        v.setLongitude(pos[1]);
        v.setDistance(calcDistance(centerLat, centerLng, pos[0], pos[1]));

        double distKm = v.getDistance() / 1000.0;
        v.setEstimatedCost(v.getBaseFare() + distKm * v.getPerKmFare() + 10 * v.getPerMinFare());
        v.setCreateTime(getCurrentTime());
        return v;
    }

    private VehicleDetail generateCarpool(double centerLat, double centerLng, int idx) {
        VehicleDetail v = new VehicleDetail();
        v.setVehicleId("POOL-" + String.format(Locale.getDefault(), "%04d", idx + 1));
        v.setVehicleType("carpool");
        v.setVehicleName(carNames[random.nextInt(carNames.length)]);
        v.setPlateNumber(generatePlate());
        v.setVehicleStatus(random.nextInt(100) < 70 ? 0 : 2); // 70%空闲, 30%即将离线
        v.setBatteryLevel(50 + random.nextInt(51));    // 50~100%
        v.setFuelLevel(40 + random.nextInt(61));       // 40~100%
        v.setRemainingRange(40 + random.nextDouble() * 260); // 40~300km
        v.setBaseFare(5.0);
        v.setPerKmFare(1.5);
        v.setPerMinFare(0.3);

        double[] pos = randomOffset(centerLat, centerLng, 250, 900);
        v.setLatitude(pos[0]);
        v.setLongitude(pos[1]);
        v.setDistance(calcDistance(centerLat, centerLng, pos[0], pos[1]));

        double distKm = v.getDistance() / 1000.0;
        v.setEstimatedCost(v.getBaseFare() + distKm * v.getPerKmFare() + 15 * v.getPerMinFare());
        v.setCreateTime(getCurrentTime());
        return v;
    }

    // ===== 工具方法 =====

    /** 在中心点周围随机偏移 */
    private double[] randomOffset(double lat, double lng, double minMeters, double maxMeters) {
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = minMeters + random.nextDouble() * (maxMeters - minMeters);
        double latOffset = distance * Math.cos(angle) / 111319.0;
        double lngOffset = distance * Math.sin(angle) / (111319.0 * Math.cos(lat * Math.PI / 180.0));
        return new double[]{lat + latOffset, lng + lngOffset};
    }

    /** 计算两点间距离（米） 用高德坐标距离公式 */
    private double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0];
    }

    private String generatePlate() {
        String province = provinces[random.nextInt(provinces.length)];
        char city = cityLetters[random.nextInt(cityLetters.length)];
        int number = 10000 + random.nextInt(90000);
        return province + city + number;
    }

    private String getCurrentTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",
                java.util.Locale.getDefault()).format(new java.util.Date());
    }

    private ContentValues toValues(VehicleDetail v) {
        ContentValues cv = new ContentValues();
        cv.put(C_VEHICLE_ID, v.getVehicleId());
        cv.put(C_VEHICLE_TYPE, v.getVehicleType());
        cv.put(C_VEHICLE_NAME, v.getVehicleName());
        cv.put(C_PLATE, v.getPlateNumber());
        cv.put(C_STATUS, v.getVehicleStatus());
        cv.put(C_BATTERY, v.getBatteryLevel());
        cv.put(C_FUEL, v.getFuelLevel());
        cv.put(C_RANGE, v.getRemainingRange());
        cv.put(C_COST, v.getEstimatedCost());
        cv.put(C_LAT, v.getLatitude());
        cv.put(C_LNG, v.getLongitude());
        cv.put(C_TIME, v.getCreateTime());
        return cv;
    }

    private VehicleDetail fromCursor(Cursor c) {
        VehicleDetail v = new VehicleDetail();
        v.setVehicleId(c.getString(c.getColumnIndexOrThrow(C_VEHICLE_ID)));
        v.setVehicleType(c.getString(c.getColumnIndexOrThrow(C_VEHICLE_TYPE)));
        v.setVehicleName(c.getString(c.getColumnIndexOrThrow(C_VEHICLE_NAME)));
        v.setPlateNumber(c.getString(c.getColumnIndexOrThrow(C_PLATE)));
        v.setVehicleStatus(c.getInt(c.getColumnIndexOrThrow(C_STATUS)));
        v.setBatteryLevel(c.getDouble(c.getColumnIndexOrThrow(C_BATTERY)));
        v.setFuelLevel(c.getDouble(c.getColumnIndexOrThrow(C_FUEL)));
        v.setRemainingRange(c.getDouble(c.getColumnIndexOrThrow(C_RANGE)));
        v.setEstimatedCost(c.getDouble(c.getColumnIndexOrThrow(C_COST)));
        v.setLatitude(c.getDouble(c.getColumnIndexOrThrow(C_LAT)));
        v.setLongitude(c.getDouble(c.getColumnIndexOrThrow(C_LNG)));
        v.setCreateTime(c.getString(c.getColumnIndexOrThrow(C_TIME)));
        return v;
    }
}
