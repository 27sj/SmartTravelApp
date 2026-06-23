// ============================================================
// CarType.java — 车型实体模型
// 用于 RecyclerView 展示车型选择列表
// ============================================================
package com.example.smarttravel;

/**
 * CarType 车型实体类
 * 封装车型选择的展示数据：车型名称、图标资源ID、预计到达时间、预估价格
 */
public class CarType {

    /** 车型名称（如：经济快车、优享快车） */
    private String name;

    /** 车型图标的 drawable 资源 ID */
    private int iconResId;

    /** 预计到达时间（如"3分钟"） */
    private String arriveTime;

    /** 预估价格（如"¥12"） */
    private String price;

    /**
     * 构造方法
     * @param name       车型名称
     * @param iconResId  图标资源ID
     * @param arriveTime 预计到达时间
     * @param price      预估价格
     */
    public CarType(String name, int iconResId, String arriveTime, String price) {
        this.name = name;
        this.iconResId = iconResId;
        this.arriveTime = arriveTime;
        this.price = price;
    }

    // ===== Getter 方法 =====
    public String getName() { return name; }
    public int getIconResId() { return iconResId; }
    public String getArriveTime() { return arriveTime; }
    public String getPrice() { return price; }
}
