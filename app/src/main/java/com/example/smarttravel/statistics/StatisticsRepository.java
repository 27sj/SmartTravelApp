// ============================================================
// StatisticsRepository.java — 统计分析数据仓库
// 桥接数据库查询和UI展示层，返回StatisticsEntity封装数据
// ============================================================
package com.example.smarttravel.statistics;

import android.content.Context;

import java.util.List;

/**
 * StatisticsRepository 统计分析数据仓库
 * 封装所有统计数据的获取逻辑
 */
public class StatisticsRepository {

    private final StatisticsDatabaseHelper dbHelper;

    public StatisticsRepository(Context context) {
        this.dbHelper = new StatisticsDatabaseHelper(context);
        // 确保测试数据就绪
        dbHelper.ensureTestData();
    }

    /** 获取出行需求统计 */
        public double getTotalSpending() {
        return dbHelper.getTotalSpending();
    }

    public StatisticsEntity.TripDemand getTripDemand() {
        int total = dbHelper.getTotalTrips();
        double dailyAvg = dbHelper.getDailyAvgTrips();
        List<double[]> weekly = dbHelper.getWeeklyTrend();
        List<double[]> monthly = dbHelper.getMonthlyTrend();
        return new StatisticsEntity.TripDemand(total, dailyAvg, weekly, monthly);
    }

    /** 获取时段热力分布 */
    public StatisticsEntity.TimeHeatDistribution getTimeHeatDistribution() {
        List<double[]> slots = dbHelper.getTimeSlotDistribution();
        String peakAnalysis = dbHelper.analyzePeakHours();
        return new StatisticsEntity.TimeHeatDistribution(slots, peakAnalysis);
    }

    /** 获取出行方式结构 */
    public StatisticsEntity.ModeShare getModeShare() {
        List<double[]> shares = dbHelper.getModeShare();
        return new StatisticsEntity.ModeShare(shares);
    }

    /** 获取碳减排效益 */
    public StatisticsEntity.CarbonReduction getCarbonReduction() {
        double total = dbHelper.getTotalCarbonReduction();
        double monthly = dbHelper.getMonthlyCarbonReduction();
        double perCapita = dbHelper.getPerCapitaReduction();
        List<double[]> trend = dbHelper.getCarbonReductionTrend();
        return new StatisticsEntity.CarbonReduction(total, monthly, perCapita, trend);
    }

    /** 获取车辆运营效率 */
    public StatisticsEntity.VehicleEfficiency getVehicleEfficiency() {
        double avgDist = dbHelper.getAvgVehicleDistance();
        double avgDuration = dbHelper.getAvgServiceDuration();
        double utilRate = dbHelper.getOverallUtilizationRate();
        List<String[]> highUtil = dbHelper.getVehicleUtilizationRanking(10, true);
        List<String[]> lowUtil = dbHelper.getVehicleUtilizationRanking(10, false);
        List<String[]> usageRank = dbHelper.getVehicleUsageRanking(10);
        return new StatisticsEntity.VehicleEfficiency(
                avgDist, avgDuration, utilRate, highUtil, lowUtil, usageRank);
    }
}
