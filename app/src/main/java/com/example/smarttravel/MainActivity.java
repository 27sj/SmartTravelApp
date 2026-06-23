// ============================================================
// MainActivity.java — 主Activity
// 承载底部导航栏（首页/订单/消息/我的）和 Fragment 切换
// 使用 Navigation Component 实现页面导航
// ============================================================
package com.example.smarttravel;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * MainActivity 应用主入口
 * 管理底部导航栏及其对应的四个 Fragment 页面
 * 使用 Android Navigation Component 处理碎片切换
 */
public class MainActivity extends AppCompatActivity {

    // 视图引用
    private MaterialToolbar toolbar;                // 顶部工具栏
    private BottomNavigationView bottomNavigation;  // 底部导航栏
    private NavController navController;            // 导航控制器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 加载 activity_main.xml 布局
        setContentView(R.layout.activity_main);

        // 初始化视图
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // 设置工具栏（使用 setSupportActionBar 以支持 ActionBar 特性）
        setSupportActionBar(toolbar);

        // ===== 初始化 Navigation Component =====
        // 获取 NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // 将 BottomNavigationView 与 NavController 绑定
        // 实现点击底部导航项自动切换 Fragment
        if (navController != null) {
            NavigationUI.setupWithNavController(bottomNavigation, navController);
        }

        // ===== 监听页面切换，更新工具栏标题 =====
        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // 根据当前 Fragment 更新工具栏标题
                if (destination.getLabel() != null) {
                    toolbar.setTitle(destination.getLabel().toString());
                }
            });
        }
    }

    /**
     * 处理返回键
     * 如果当前不是首页，则回退到上一个 Fragment
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp();
    }
}
