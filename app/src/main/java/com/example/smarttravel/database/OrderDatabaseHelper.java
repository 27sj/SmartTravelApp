// ============================================================
// OrderDatabaseHelper.java — SQLite 数据库帮助类（升级版）
// 新增订单表字段：driver_name / plate_no / rating / car_model / car_image
// 支持完整司机信息的持久化
// ============================================================
package com.example.smarttravel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * OrderDatabaseHelper 本地数据库帮助类
 * 管理订单表的创建、升级与 CRUD 操作（含司机信息）
 */
public class OrderDatabaseHelper extends SQLiteOpenHelper {

    // ===== 数据库常量 =====
    private static final String DATABASE_NAME = "smart_travel.db";
    private static final int DATABASE_VERSION = 4;  // ⬆ 版本升级到4，新增distance_km字段
    private static final String TABLE_ORDER = "t_order";

    // 基础字段
    private static final String COL_ID = "_id";
    private static final String COL_ORDER_NO = "order_no";
    private static final String COL_START = "start_point";
    private static final String COL_END = "end_point";
    private static final String COL_CAR_TYPE = "car_type";
    private static final String COL_PRICE = "price";
    private static final String COL_TIME = "order_time";
    private static final String COL_STATUS = "status";

    // ===== 新增司机信息字段 =====
    private static final String COL_DRIVER_NAME = "driver_name";   // 司机姓名
    private static final String COL_PLATE_NO = "plate_no";         // 车牌号
    private static final String COL_RATING = "rating";             // 评分
    private static final String COL_CAR_MODEL = "car_model";      // 车辆型号
    private static final String COL_CAR_IMAGE = "car_image";      // 车辆图片资源ID

    /** 建表 SQL（含全部字段） */
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_ORDER + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ORDER_NO + " TEXT, " +
                    COL_START + " TEXT, " +
                    COL_END + " TEXT, " +
                    COL_CAR_TYPE + " TEXT, " +
                    COL_PRICE + " TEXT, " +
                    COL_TIME + " TEXT, " +
                    COL_STATUS + " INTEGER DEFAULT 1, " +
                    COL_DRIVER_NAME + " TEXT, " +
                    COL_PLATE_NO + " TEXT, " +
                    COL_RATING + " REAL DEFAULT 4.5, " +
                    COL_CAR_MODEL + " TEXT, " +
                    COL_CAR_IMAGE + " INTEGER DEFAULT 0, " +
                    "distance_km REAL DEFAULT 5.0)";

    public OrderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            // 版本升级到4：新增 distance_km 字段
            try {
                db.execSQL("ALTER TABLE " + TABLE_ORDER + " ADD COLUMN distance_km REAL DEFAULT 5.0");
            } catch (Exception ignored) {}
        }
    }

    // ==================== CRUD 操作 ====================

    /**
     * 插入一条完整订单记录（含司机信息）
     */
    public long insertOrder(OrderInfo order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ORDER_NO, order.getOrderNo());
        values.put(COL_START, order.getStartPoint());
        values.put(COL_END, order.getEndPoint());
        values.put(COL_CAR_TYPE, order.getCarType());
        values.put(COL_PRICE, order.getPrice());
        values.put(COL_TIME, order.getTime());
        values.put(COL_STATUS, order.getStatus());
        values.put(COL_DRIVER_NAME, order.getDriverName());
        values.put(COL_PLATE_NO, order.getPlateNo());
        values.put(COL_RATING, order.getRating());
        values.put(COL_CAR_MODEL, order.getCarModel());
        values.put(COL_CAR_IMAGE, order.getCarImageResId());
        long result = db.insert(TABLE_ORDER, null, values);
        db.close();
        return result;
    }

    /**
     * 查询所有历史订单（按时间倒序）
     */
    public List<OrderInfo> getAllOrders() {
        List<OrderInfo> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDER, null, null, null,
                null, null, COL_ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                OrderInfo order = new OrderInfo(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ORDER_NO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_START)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_END)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CAR_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATUS))
                );
                // 读取司机信息
                order.setDriverName(cursor.getString(cursor.getColumnIndexOrThrow(COL_DRIVER_NAME)));
                order.setPlateNo(cursor.getString(cursor.getColumnIndexOrThrow(COL_PLATE_NO)));
                order.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RATING)));
                order.setCarModel(cursor.getString(cursor.getColumnIndexOrThrow(COL_CAR_MODEL)));
                order.setCarImageResId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAR_IMAGE)));
                list.add(order);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 根据订单号删除订单
     */
    public int deleteOrder(String orderNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ORDER, COL_ORDER_NO + " = ?",
                new String[]{orderNo});
        db.close();
        return result;
    }

    /**
     * 清空所有订单
     */
    public int deleteAllOrders() {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ORDER, null, null);
        db.close();
        return result;
    }

    // ==================== 统计方法 ====================

    /** 获取累计订单总数 */
    public int getOrderCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDER, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /** 获取累计消费总额（从 price 字段统计） */
    public double getTotalSpending() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(CAST(REPLACE(REPLACE(" + COL_PRICE +
                ", '¥', ''), '元', '') AS REAL)) FROM " + TABLE_ORDER, null);
        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }
}
