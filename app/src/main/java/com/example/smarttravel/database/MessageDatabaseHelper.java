// ============================================================
// MessageDatabaseHelper.java — 消息数据库帮助类
// 管理消息表的创建、升级与 CRUD 操作
// ============================================================
package com.example.smarttravel.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smarttravel.model.MessageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * MessageDatabaseHelper 消息本地数据库
 * 存储系统通知、订单消息等
 */
public class MessageDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_travel_messages.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_MESSAGE = "t_message";

    private static final String COL_ID = "_id";
    private static final String COL_TITLE = "title";
    private static final String COL_CONTENT = "content";
    private static final String COL_TIME = "msg_time";
    private static final String COL_STATUS = "msg_status";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_MESSAGE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TITLE + " TEXT, " +
                    COL_CONTENT + " TEXT, " +
                    COL_TIME + " TEXT, " +
                    COL_STATUS + " INTEGER DEFAULT 0)";

    public MessageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 开发阶段：删除旧表重建（生产环境应使用 ALTER TABLE）
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
        onCreate(db);
    }

    /** 插入一条消息 */
    public long insertMessage(MessageInfo msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, msg.getTitle());
        values.put(COL_CONTENT, msg.getContent());
        values.put(COL_TIME, msg.getTime());
        values.put(COL_STATUS, msg.getStatus());
        long result = db.insert(TABLE_MESSAGE, null, values);
        db.close();
        return result;
    }

    /** 查询所有消息（按时间倒序） */
    public List<MessageInfo> getAllMessages() {
        List<MessageInfo> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGE, null, null, null,
                null, null, COL_ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                MessageInfo msg = new MessageInfo(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME))
                );
                msg.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
                msg.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COL_STATUS)));
                list.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    /** 标记消息为已读 */
    public void markAsRead(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, 1);
        db.update(TABLE_MESSAGE, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /** 获取未读消息数量 */
    public int getUnreadCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MESSAGE +
                " WHERE " + COL_STATUS + " = 0", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /** 获取消息总数 */
    public int getMessageCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MESSAGE, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}
