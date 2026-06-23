// ============================================================
// StatisticsDatabaseHelper.java — 统计分析数据库帮助类
// 管理 vehicle 统计表 + 自动生成测试数据 + 核心统计查询
// vehicle 表在 smart_travel_stats.db，订单查询走 smart_travel.db
// ============================================================
package com.example.smarttravel.statistics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * StatisticsDatabaseHelper 统计分析数据库帮助类
 * 维护 vehicle 表、提供出行需求/时段热力/出行方式/碳减排/运营效率等统计查询
 * 数据库不足时自动生成测试数据
 */
public class StatisticsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_travel_stats.db";
    private static final int DATABASE_VERSION = 1;

    // ===== 车辆表 =====
    private static final String TABLE_VEHICLE = "t_vehicle";
    private static final String COL_V_ID = "_id";
    private static final String COL_V_PLATE = "plate_no";
    private static final String COL_V_MODEL = "vehicle_model";
    private static final String COL_V_TYPE = "vehicle_type";
    private static final String COL_V_STATUS = "status";
    private static final String COL_V_SERVICE_START = "service_start_time";
    private static final String COL_V_SERVICE_END = "service_end_time";
    private static final String COL_V_TOTAL_SERVICE_MIN = "total_service_min";

    private static final String CREATE_VEHICLE_TABLE =
            "CREATE TABLE " + TABLE_VEHICLE + " (" +
                    COL_V_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_V_PLATE + " TEXT, " +
                    COL_V_MODEL + " TEXT, " +
                    COL_V_TYPE + " TEXT, " +
                    COL_V_STATUS + " INTEGER DEFAULT 0, " +
                    COL_V_SERVICE_START + " TEXT, " +
                    COL_V_SERVICE_END + " TEXT, " +
                    COL_V_TOTAL_SERVICE_MIN + " INTEGER DEFAULT 0)";

    private final Context context;
    private final Random random = new Random();
    private static final String ORDER_DB = "smart_travel.db";
    private static final String ORDER_TABLE = "t_order";

    public StatisticsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_VEHICLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLE);
        onCreate(db);
    }

    /** 打开 smart_travel.db 用于订单查询，确保必要列存在 */
    private SQLiteDatabase openOrderDb() {
        try {
            SQLiteDatabase db = context.openOrCreateDatabase(ORDER_DB,
                    Context.MODE_PRIVATE, null);
            try {
                db.execSQL("ALTER TABLE " + ORDER_TABLE + " ADD COLUMN distance_km REAL DEFAULT 5.0");
            } catch (Exception ignored) {}
            return db;
        } catch (Exception e) {
            return getWritableDatabase();
        }
    }

    // ================================================================
    // 自动生成测试数据
    // ================================================================

    public void ensureTestData() {
        SQLiteDatabase orderDb = null;
        try {
            orderDb = context.openOrCreateDatabase(ORDER_DB,
                    Context.MODE_PRIVATE, null);
            // 确保 distance_km 列存在
            try {
                orderDb.execSQL("ALTER TABLE " + ORDER_TABLE + " ADD COLUMN distance_km REAL DEFAULT 5.0");
            } catch (Exception ignored) {} // 已存在则忽略
        } catch (Exception e) {
            orderDb = getWritableDatabase();
        }

        Cursor orderCursor = orderDb.rawQuery(
                "SELECT COUNT(*) FROM " + ORDER_TABLE, null);
        int orderCount = 0;
        if (orderCursor.moveToFirst()) orderCount = orderCursor.getInt(0);
        orderCursor.close();

        if (orderCount < 30) {
            generateTestOrders(orderDb);
        }
        orderDb.close();

        SQLiteDatabase statsDb = getWritableDatabase();
        Cursor vehCursor = statsDb.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_VEHICLE, null);
        int vehCount = 0;
        if (vehCursor.moveToFirst()) vehCount = vehCursor.getInt(0);
        vehCursor.close();

        if (vehCount < 20) {
            generateTestVehicles(statsDb);
        }
        statsDb.close();
    }

    private void generateTestOrders(SQLiteDatabase db) {
        String[] startPoints = {
                "大学城地铁站", "科技园区", "商业中心", "火车站", "机场",
                "人民医院", "图书馆", "体育馆", "博物馆", "公园"
        };
        String[] endPoints = {
                "南山科技园", "华侨城", "会展中心", "福田口岸", "宝安中心",
                "龙岗万达", "罗湖东门", "大学城", "软件园", "华强北"
        };
        String[] carTypes = {"共享单车", "共享汽车", "拼车"};
        String[] driverNames = {
                "王师傅", "李师傅", "张师傅", "刘师傅", "陈师傅", "赵师傅"
        };
        String[] carModels = {
                "比亚迪秦PLUS", "比亚迪汉EV", "特斯拉Model3", "大众朗逸",
                "丰田卡罗拉", "小鹏P7", "广汽埃安S", "美团单车", "哈啰单车"
        };

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -60);

        for (int i = 0; i < 200; i++) {
            cal.add(Calendar.MINUTE, random.nextInt(120) + 10);

            String orderNo = String.format(Locale.getDefault(),
                    "ORD%tY%<tm%<td%<tH%<tM%<tS%03d", cal, random.nextInt(999));
            String startPoint = startPoints[random.nextInt(startPoints.length)];
            String endPoint = endPoints[random.nextInt(endPoints.length)];
            String carType = carTypes[random.nextInt(carTypes.length)];
            double priceVal = 2.0 + random.nextDouble() * 58.0;
            String price = String.format(Locale.getDefault(), "¥%.0f", priceVal);
            String time = String.format(Locale.getDefault(),
                    "%tY-%<tm-%<td %<tH:%<tM", cal);
            int status = random.nextInt(100) < 95 ? 1 : 0;

            String driverName = driverNames[random.nextInt(driverNames.length)];
            String plateNo = generatePlateNo();
            double rating = 4.5 + random.nextDouble() * 0.5;
            rating = Math.round(rating * 10.0) / 10.0;
            String carModel = carModels[random.nextInt(carModels.length)];
            double distanceKm = 1.0 + random.nextDouble() * 20.0;

            ContentValues values = new ContentValues();
            values.put("order_no", orderNo);
            values.put("start_point", startPoint);
            values.put("end_point", endPoint);
            values.put("car_type", carType);
            values.put("price", price);
            values.put("order_time", time);
            values.put("status", status);
            values.put("driver_name", driverName);
            values.put("plate_no", plateNo);
            values.put("rating", rating);
            values.put("car_model", carModel);
            values.put("distance_km", distanceKm);
            db.insert(ORDER_TABLE, null, values);
        }
    }

    private void generateTestVehicles(SQLiteDatabase db) {
        String[] types = {"bike", "car", "carpool"};
        String[] bikeModels = {"美团单车V3", "哈啰单车A5", "青桔单车X1"};
        String[] carModels = {
                "比亚迪秦PLUS", "比亚迪汉EV", "特斯拉Model3",
                "大众朗逸", "丰田卡罗拉", "小鹏P7"
        };

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        for (int i = 0; i < 50; i++) {
            String type = types[i < 20 ? 0 : (i < 35 ? 1 : 2)];
            String model = type.equals("bike")
                    ? bikeModels[random.nextInt(bikeModels.length)]
                    : carModels[random.nextInt(carModels.length)];
            String plateNo = generatePlateNo();
            int status = random.nextInt(100) < 70 ? 1 : 0;
            int totalMin = random.nextInt(480) + 60;

            String serviceStart = String.format(Locale.getDefault(),
                    "%tY-%<tm-%<td 06:00", cal);
            String serviceEnd = String.format(Locale.getDefault(),
                    "%tY-%<tm-%<td 22:00", cal);

            ContentValues values = new ContentValues();
            values.put(COL_V_PLATE, plateNo);
            values.put(COL_V_MODEL, model);
            values.put(COL_V_TYPE, type);
            values.put(COL_V_STATUS, status);
            values.put(COL_V_SERVICE_START, serviceStart);
            values.put(COL_V_SERVICE_END, serviceEnd);
            values.put(COL_V_TOTAL_SERVICE_MIN, totalMin);
            db.insert(TABLE_VEHICLE, null, values);

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private String generatePlateNo() {
        String[] provinces = {"粤", "京", "沪", "苏", "浙", "川", "湘"};
        char cityLetter = (char) ('A' + random.nextInt(7));
        int number = 10000 + random.nextInt(90000);
        return provinces[random.nextInt(provinces.length)] + cityLetter + number;
    }

    // ================================================================
    // 一、出行需求统计分析
    // ================================================================

    public int getTotalTrips() {
        SQLiteDatabase db = openOrderDb();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + ORDER_TABLE, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close(); db.close();
        return count;
    }

    public double getTotalSpending() {
        SQLiteDatabase db = openOrderDb();
        Cursor c = db.rawQuery("SELECT SUM(CAST(REPLACE(price,'¥','') AS REAL)) FROM t_order", null);
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        db.close();
        return total;
    }

    public double getDailyAvgTrips() {
        SQLiteDatabase db = openOrderDb();
        Cursor c = db.rawQuery(
                "SELECT (julianday(MAX(order_time)) - julianday(MIN(order_time))) " +
                        "FROM " + ORDER_TABLE, null);
        double days = 1;
        if (c.moveToFirst()) {
            days = c.getDouble(0);
            if (days < 1) days = 1;
        }
        c.close();

        int total = 0;
        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM " + ORDER_TABLE, null);
        if (c2.moveToFirst()) total = c2.getInt(0);
        c2.close();
        db.close();
        return (double) total / days;
    }

    public List<double[]> getWeeklyTrend() {
        SQLiteDatabase db = openOrderDb();
        List<double[]> result = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT DATE(order_time) as d, COUNT(*) as cnt " +
                        "FROM " + ORDER_TABLE + " " +
                        "WHERE order_time >= datetime('now', '-7 days', 'localtime') " +
                        "GROUP BY d ORDER BY d", null);
        while (c.moveToNext()) {
            double day = parseDayOfWeek(c.getString(0));
            double count = c.getInt(1);
            result.add(new double[]{day, count});
        }
        c.close();
        if (result.isEmpty()) {
            for (int i = 0; i < 7; i++) {
                result.add(new double[]{i + 1, 15 + random.nextInt(25)});
            }
        }
        db.close();
        return result;
    }

    public List<double[]> getMonthlyTrend() {
        SQLiteDatabase db = openOrderDb();
        List<double[]> result = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT DATE(order_time) as d, COUNT(*) as cnt " +
                        "FROM " + ORDER_TABLE + " " +
                        "WHERE order_time >= datetime('now', '-30 days', 'localtime') " +
                        "GROUP BY d ORDER BY d", null);
        while (c.moveToNext()) {
            double day = parseDayOfMonth(c.getString(0));
            double count = c.getInt(1);
            result.add(new double[]{day, count});
        }
        c.close();
        if (result.isEmpty()) {
            for (int i = 0; i < 30; i++) {
                result.add(new double[]{i + 1, 12 + random.nextInt(30)});
            }
        }
        db.close();
        return result;
    }

    private double parseDayOfWeek(String dateStr) {
        try {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date date = sdf.parse(dateStr);
            if (date != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(date);
                return cal.get(java.util.Calendar.DAY_OF_WEEK);
            }
        } catch (Exception ignored) {}
        return 1;
    }

    private double parseDayOfMonth(String dateStr) {
        try {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date date = sdf.parse(dateStr);
            if (date != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(date);
                return cal.get(java.util.Calendar.DAY_OF_MONTH);
            }
        } catch (Exception ignored) {}
        return 1;
    }

    // ================================================================
    // 二、时段热力分布分析
    // ================================================================

    public static final String[] TIME_SLOTS = {
            "0:00—6:00", "6:00—9:00", "9:00—12:00",
            "12:00—18:00", "18:00—22:00", "22:00—24:00"
    };

    public List<double[]> getTimeSlotDistribution() {
        SQLiteDatabase db = openOrderDb();
        List<double[]> result = new ArrayList<>();
        String[] conditions = {
            "CAST(strftime('%H', order_time) AS INTEGER) BETWEEN 0 AND 5",
            "CAST(strftime('%H', order_time) AS INTEGER) BETWEEN 6 AND 8",
            "CAST(strftime('%H', order_time) AS INTEGER) BETWEEN 9 AND 11",
            "CAST(strftime('%H', order_time) AS INTEGER) BETWEEN 12 AND 17",
            "CAST(strftime('%H', order_time) AS INTEGER) BETWEEN 18 AND 21",
            "CAST(strftime('%H', order_time) AS INTEGER) >= 22 OR CAST(strftime('%H', order_time) AS INTEGER) = 23"
        };

        Cursor totalC = db.rawQuery("SELECT COUNT(*) FROM " + ORDER_TABLE, null);
        int total = 0;
        if (totalC.moveToFirst()) total = totalC.getInt(0);
        totalC.close();
        if (total == 0) total = 1;

        for (String cond : conditions) {
            Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM " + ORDER_TABLE + " WHERE " + cond, null);
            int count = 0;
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
            double pct = (double) count / total * 100.0;
            result.add(new double[]{count, pct});
        }
        db.close();
        return result;
    }

    public String analyzePeakHours() {
        List<double[]> dist = getTimeSlotDistribution();
        double maxCount = 0;
        int peakIdx = -1;
        int secondPeakIdx = -1;
        double secondMax = 0;

        for (int i = 0; i < dist.size(); i++) {
            double cnt = dist.get(i)[0];
            if (cnt > maxCount) {
                secondMax = maxCount;
                secondPeakIdx = peakIdx;
                maxCount = cnt;
                peakIdx = i;
            } else if (cnt > secondMax) {
                secondMax = cnt;
                secondPeakIdx = i;
            }
        }

        StringBuilder sb = new StringBuilder();
        if (peakIdx >= 0) {
            boolean isMorning = peakIdx == 1;
            boolean isEvening = peakIdx == 4;
            if (isMorning) sb.append("早高峰：").append(TIME_SLOTS[peakIdx]);
            else if (isEvening) sb.append("晚高峰：").append(TIME_SLOTS[peakIdx]);
            else sb.append("高峰时段：").append(TIME_SLOTS[peakIdx]);
            sb.append("（").append(String.format(Locale.getDefault(), "%.1f%%", dist.get(peakIdx)[1])).append("）");
        }
        if (secondPeakIdx >= 0) {
            sb.append("\n次高峰：").append(TIME_SLOTS[secondPeakIdx])
                    .append("（").append(String.format(Locale.getDefault(), "%.1f%%", dist.get(secondPeakIdx)[1])).append("）");
        }
        sb.append("\n平峰时段：").append(TIME_SLOTS[2]).append("、").append(TIME_SLOTS[3]);
        return sb.toString();
    }

    // ================================================================
    // 三、出行方式结构分析
    // ================================================================

    public List<double[]> getModeShare() {
        SQLiteDatabase db = openOrderDb();
        List<double[]> result = new ArrayList<>();
        String[] modes = {"共享单车", "共享汽车", "拼车"};

        Cursor totalC = db.rawQuery("SELECT COUNT(*) FROM " + ORDER_TABLE, null);
        int total = 0;
        if (totalC.moveToFirst()) total = totalC.getInt(0);
        totalC.close();
        if (total == 0) total = 1;

        for (String mode : modes) {
            Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM " + ORDER_TABLE +
                            " WHERE car_type=? COLLATE NOCASE", new String[]{mode});
            int count = 0;
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
            double pct = (double) count / total * 100.0;
            result.add(new double[]{count, pct});
        }
        db.close();
        return result;
    }

    // ================================================================
    // 四、碳减排效益分析
    // ================================================================

    private static final double BIKE_REDUCTION = 0.21;
    private static final double CAR_REDUCTION = 0.11;
    private static final double CARPOOL_REDUCTION = 0.15;

    public double getTotalCarbonReduction() {
        return calculateCarbonReduction(null);
    }

    public double getMonthlyCarbonReduction() {
        return calculateCarbonReduction("monthly");
    }

    public double getPerCapitaReduction() {
        double total = getTotalCarbonReduction();
        SQLiteDatabase db = openOrderDb();
        Cursor c = db.rawQuery(
                "SELECT COUNT(DISTINCT order_no) FROM " + ORDER_TABLE, null);
        int users = 1;
        if (c.moveToFirst()) users = Math.max(c.getInt(0), 1);
        c.close(); db.close();
        return total / users;
    }

    public List<double[]> getCarbonReductionTrend() {
        SQLiteDatabase db = openOrderDb();
        List<double[]> result = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT strftime('%m', order_time) as m, " +
                        "SUM(CASE WHEN car_type='共享单车' THEN 0.21 " +
                        "         WHEN car_type='共享汽车' THEN 0.11 " +
                        "         WHEN car_type='拼车' THEN 0.15 ELSE 0 END * distance_km) as reduction " +
                        "FROM " + ORDER_TABLE + " " +
                        "WHERE distance_km IS NOT NULL " +
                        "GROUP BY m ORDER BY m", null);
        while (c.moveToNext()) {
            double month = c.getInt(0);
            double val = c.getDouble(1);
            result.add(new double[]{month, val});
        }
        c.close();
        if (result.isEmpty()) {
            for (int i = 1; i <= 6; i++) {
                result.add(new double[]{i, 50 + random.nextDouble() * 100});
            }
        }
        db.close();
        return result;
    }

    private double calculateCarbonReduction(String period) {
        SQLiteDatabase db = openOrderDb();
        String where = "";
        if ("monthly".equals(period)) {
            where = "WHERE order_time >= datetime('now', '-30 days', 'localtime')";
        }
        Cursor c = db.rawQuery(
                "SELECT SUM(CASE WHEN car_type='共享单车' THEN 0.21 " +
                        "         WHEN car_type='共享汽车' THEN 0.11 " +
                        "         WHEN car_type='拼车' THEN 0.15 ELSE 0 END * distance_km) " +
                        "FROM " + ORDER_TABLE + " " + where, null);
        double result = 0;
        if (c.moveToFirst()) result = c.getDouble(0);
        c.close();
        db.close();
        return result;
    }

    // ================================================================
    // 五、车辆运营效率分析
    // ================================================================

    public List<String[]> getVehicleUsageRanking(int topN) {
        SQLiteDatabase db = openOrderDb();
        List<String[]> result = new ArrayList<>();
        Cursor c = db.rawQuery(
                "SELECT plate_no, car_model, COUNT(*) as usage_count " +
                        "FROM " + ORDER_TABLE + " o " +
                        "WHERE o.plate_no IS NOT NULL AND o.plate_no != '' " +
                        "GROUP BY o.plate_no ORDER BY usage_count DESC LIMIT ?",
                new String[]{String.valueOf(topN * 2)});
        while (c.moveToNext()) {
            result.add(new String[]{
                    c.getString(0),
                    c.getString(1),
                    String.valueOf(c.getInt(2))
            });
        }
        c.close();

        if (result.isEmpty()) {
            SQLiteDatabase statsDb = getReadableDatabase();
            Cursor c2 = statsDb.rawQuery(
                    "SELECT plate_no, vehicle_model, " + COL_V_TOTAL_SERVICE_MIN +
                            " FROM " + TABLE_VEHICLE +
                            " ORDER BY " + COL_V_TOTAL_SERVICE_MIN + " DESC LIMIT ?",
                    new String[]{String.valueOf(topN * 2)});
            while (c2.moveToNext()) {
                result.add(new String[]{
                        c2.getString(0),
                        c2.getString(1),
                        String.valueOf(c2.getInt(2))
                });
            }
            c2.close();
            statsDb.close();
        }
        db.close();
        return result;
    }

    public List<String[]> getVehicleUtilizationRanking(int topN, boolean highest) {
        SQLiteDatabase db = getReadableDatabase();
        List<String[]> result = new ArrayList<>();
        String order = highest ? "DESC" : "ASC";
        Cursor c = db.rawQuery(
                "SELECT plate_no, vehicle_model, " +
                        "ROUND(CAST(" + COL_V_TOTAL_SERVICE_MIN + " AS REAL) / 960.0 * 100, 1) as util_rate " +
                        "FROM " + TABLE_VEHICLE + " " +
                        "ORDER BY util_rate " + order + " LIMIT ?",
                new String[]{String.valueOf(topN)});
        while (c.moveToNext()) {
            result.add(new String[]{
                    c.getString(0),
                    c.getString(1),
                    c.getString(2) + "%"
            });
        }
        c.close();
        if (result.isEmpty()) {
            for (int i = 0; i < topN; i++) {
                result.add(new String[]{
                        generatePlateNo(),
                        "测试车型",
                        (highest ? 70 + random.nextInt(25) : 10 + random.nextInt(20)) + "%"
                });
            }
        }
        db.close();
        return result;
    }

    public double getAvgVehicleDistance() {
        SQLiteDatabase db = openOrderDb();
        Cursor c = db.rawQuery(
                "SELECT AVG(distance_km) FROM " + ORDER_TABLE +
                        " WHERE distance_km IS NOT NULL", null);
        double avg = 0;
        if (c.moveToFirst()) avg = c.getDouble(0);
        c.close(); db.close();
        return avg;
    }

    public double getAvgServiceDuration() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT AVG(" + COL_V_TOTAL_SERVICE_MIN + ") FROM " + TABLE_VEHICLE, null);
        double avg = 0;
        if (c.moveToFirst()) avg = c.getDouble(0);
        c.close(); db.close();
        return avg;
    }

    public double getOverallUtilizationRate() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT AVG(CAST(" + COL_V_TOTAL_SERVICE_MIN + " AS REAL) / 960.0) * 100 " +
                        "FROM " + TABLE_VEHICLE, null);
        double rate = 0;
        if (c.moveToFirst()) rate = c.getDouble(0);
        c.close(); db.close();
        return rate;
    }
}
