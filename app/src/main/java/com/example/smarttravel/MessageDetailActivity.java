// ============================================================
// MessageDetailActivity.java — 消息详情Activity
// 显示单条消息的完整标题、内容、时间
// 进入后自动标记为已读
// ============================================================
package com.example.smarttravel;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarttravel.database.MessageDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * MessageDetailActivity 消息详情页面
 */
public class MessageDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvTitle, tvContent, tvTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        long msgId = getIntent().getLongExtra("msg_id", -1);
        String msgTitle = getIntent().getStringExtra("msg_title");
        String msgContent = getIntent().getStringExtra("msg_content");
        String msgTime = getIntent().getStringExtra("msg_time");

        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvContent = findViewById(R.id.tv_detail_content);
        tvTime = findViewById(R.id.tv_detail_time);

        toolbar.setNavigationOnClickListener(v -> finish());

        tvTitle.setText(msgTitle);
        tvContent.setText(msgContent);
        tvTime.setText(msgTime);

        // 标记已读
        if (msgId > 0) {
            MessageDatabaseHelper dbHelper = new MessageDatabaseHelper(this);
            dbHelper.markAsRead(msgId);
        }
    }
}
