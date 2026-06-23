// ============================================================
// AboutActivity.java — 关于系统页
// 展示应用名称、版本号、功能说明
// ============================================================
package com.example.smarttravel;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * AboutActivity 关于系统Activity
 * 展示《智慧出行打车系统》的版本信息和课程设计说明
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 初始化工具栏
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
