// ============================================================
// DriverInfo.java — 司机信息实体模型
// 用于订单匹配成功后展示司机相关信息
// ============================================================
package com.example.smarttravel;

/**
 * DriverInfo 司机信息实体类
 * 包含司机姓名、车牌号、评分、车辆型号等信息
 */
public class DriverInfo {

    /** 司机姓名 */
    private String name;

    /** 车牌号 */
    private String plateNo;

    /** 评分（如 4.9） */
    private double rating;

    /** 车辆型号 */
    private String carModel;

    /**
     * 构造方法
     * @param name     司机姓名
     * @param plateNo  车牌号
     * @param rating   评分
     * @param carModel 车辆型号
     */
    public DriverInfo(String name, String plateNo, double rating, String carModel) {
        this.name = name;
        this.plateNo = plateNo;
        this.rating = rating;
        this.carModel = carModel;
    }

    // ===== Getter 方法 =====
    public String getName() { return name; }
    public String getPlateNo() { return plateNo; }
    public double getRating() { return rating; }
    public String getCarModel() { return carModel; }
}
