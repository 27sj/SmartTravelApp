// ============================================================
// VehicleDetail.java — 车辆信息实体模型
// 涵盖共享单车/共享汽车/拼车三种出行方式的全部字段
// ============================================================
package com.example.smarttravel.model;

/**
 * VehicleDetail 车辆详情实体
 * 对应2.6节数据结构设计
 */
public class VehicleDetail {

    private String vehicleId;          // 车辆编号
    private String vehicleType;        // bike / car / carpool
    private String vehicleName;        // 车辆名称（如"美团单车V3"）
    private String plateNumber;        // 车牌号码（共享单车可为空）
    private int vehicleStatus;         // 0=空闲, 1=使用中, 2=即将离线, 3=维护中
    private double batteryLevel;       // 剩余电量(%) — 单车/电助力
    private double fuelLevel;          // 剩余油量(%) — 汽车
    private double remainingRange;     // 续航能力(km)
    private double estimatedCost;      // 预计使用费用(元)
    private double latitude;           // 纬度
    private double longitude;          // 经度
    private double distance;           // 与用户距离(m)
    private String createTime;         // 创建/入库时间

    // 费用组成明细（用于详情展示）
    private double baseFare;           // 起步费用
    private double perKmFare;          // 里程费用
    private double perMinFare;         // 时间费用

    // ===== 状态文本 =====
    private static final String[] STATUS_LABELS = {"空闲", "使用中", "即将离线", "维护中"};

    public VehicleDetail() {}

    // ===== 全参构造 =====
    public VehicleDetail(String vehicleId, String vehicleType, String vehicleName,
                         String plateNumber, int vehicleStatus,
                         double batteryLevel, double fuelLevel, double remainingRange,
                         double estimatedCost, double latitude, double longitude,
                         double distance, String createTime) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.vehicleName = vehicleName;
        this.plateNumber = plateNumber;
        this.vehicleStatus = vehicleStatus;
        this.batteryLevel = batteryLevel;
        this.fuelLevel = fuelLevel;
        this.remainingRange = remainingRange;
        this.estimatedCost = estimatedCost;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.createTime = createTime;
    }

    // ===== Getter / Setter =====

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public int getVehicleStatus() { return vehicleStatus; }
    public void setVehicleStatus(int vehicleStatus) { this.vehicleStatus = vehicleStatus; }

    public double getBatteryLevel() { return batteryLevel; }
    public void setBatteryLevel(double batteryLevel) { this.batteryLevel = batteryLevel; }

    public double getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(double fuelLevel) { this.fuelLevel = fuelLevel; }

    public double getRemainingRange() { return remainingRange; }
    public void setRemainingRange(double remainingRange) { this.remainingRange = remainingRange; }

    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public double getPerKmFare() { return perKmFare; }
    public void setPerKmFare(double perKmFare) { this.perKmFare = perKmFare; }

    public double getPerMinFare() { return perMinFare; }
    public void setPerMinFare(double perMinFare) { this.perMinFare = perMinFare; }

    // ===== 辅助方法 =====

    /** 获取车辆类型中文标签 */
    public String getTypeLabel() {
        switch (vehicleType != null ? vehicleType : "") {
            case "bike": return "共享单车";
            case "car": return "共享汽车";
            case "carpool": return "拼车";
            default: return "未知";
        }
    }

    /** 获取车辆状态中文标签 */
    public String getStatusLabel() {
        if (vehicleStatus >= 0 && vehicleStatus < STATUS_LABELS.length) {
            return STATUS_LABELS[vehicleStatus];
        }
        return "未知";
    }

    /** 获取能量信息文本 */
    public String getEnergyLabel() {
        if ("bike".equals(vehicleType)) {
            return "电量 " + (int) batteryLevel + "%";
        } else {
            return "电量 " + (int) batteryLevel + "% / 油量 " + (int) fuelLevel + "%";
        }
    }

    /** 获取Marker标题（简短） */
    public String getMarkerTitle() {
        return getTypeLabel() + " · " + vehicleName;
    }

    /** 获取Marker摘要 */
    public String getMarkerSnippet() {
        return "续航 " + (int) remainingRange + "km · 距离 " + (int) distance + "m";
    }
}
