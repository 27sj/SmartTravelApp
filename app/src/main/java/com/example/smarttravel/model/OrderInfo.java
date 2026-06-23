// ============================================================
// OrderInfo.java — 订单信息实体模型（升级版）
// 新增司机信息字段：司机姓名/车牌号/评分/车型/图片
// 用于订单确认、详情展示及 SQLite 数据库持久化
// ============================================================
package com.example.smarttravel;

/**
 * OrderInfo 订单信息实体类
 * 包含订单号、起点终点、车型、费用、时间、司机全信息
 */
public class OrderInfo {

    /** 订单号（格式：YYYYMMDDHHMMSS + 3位随机数） */
    private String orderNo;

    /** 起点地址 */
    private String startPoint;

    /** 终点地址 */
    private String endPoint;

    /** 所选车型 */
    private String carType;

    /** 预估费用（如"¥12"） */
    private String price;

    /** 订单创建时间 */
    private String time;

    /** 订单状态：0-进行中，1-已完成 */
    private int status;

    // ===== 随机司机信息字段 =====
    private String driverName;      // 司机姓名
    private String plateNo;         // 车牌号
    private double rating;          // 评分
    private String carModel;        // 车辆型号
    private int carImageResId;      // 车辆图片资源ID

    /**
     * 构造方法 — 基础订单信息
     */
    public OrderInfo(String orderNo, String startPoint, String endPoint,
                     String carType, String price, String time, int status) {
        this.orderNo = orderNo;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.carType = carType;
        this.price = price;
        this.time = time;
        this.status = status;
    }

    // ===== 全字段 Getter / Setter =====

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getStartPoint() { return startPoint; }
    public void setStartPoint(String startPoint) { this.startPoint = startPoint; }

    public String getEndPoint() { return endPoint; }
    public void setEndPoint(String endPoint) { this.endPoint = endPoint; }

    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    // ===== 司机信息 =====

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public int getCarImageResId() { return carImageResId; }
    public void setCarImageResId(int carImageResId) { this.carImageResId = carImageResId; }
}
