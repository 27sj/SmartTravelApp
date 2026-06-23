// ============================================================
// AMapApplication.java — 高德地图 SDK 初始化入口
// 需在 AndroidManifest.xml 中声明 android:name=".AMapApplication"
// ============================================================
package com.example.smarttravel;

import android.app.Application;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;

/**
 * AMapApplication 自定义 Application
 * 用于初始化高德地图 SDK（必须在主进程启动时调用）
 */
public class AMapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // ===== 高德地图 SDK 初始化 =====
        // 设置 3D 地图渲染引擎
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        // 设置搜索服务隐私协议
        ServiceSettings.updatePrivacyShow(this, true, true);
        ServiceSettings.updatePrivacyAgree(this, true);
    }
}
