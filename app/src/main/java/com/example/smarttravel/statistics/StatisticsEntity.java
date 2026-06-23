// ============================================================
// StatisticsEntity.java — 统计分析数据实体模型
// 包含出行需求、热力分布、方式结构、碳减排、运营效率等实体
// ============================================================
package com.example.smarttravel.statistics;

import java.util.List;

/**
 * StatisticsEntity 统计实体类
 * 内部含多个静态类，对应五大分析模块的数据结构
 */
public class StatisticsEntity {

    /** 出行需求统计 */
    public static class TripDemand {
        public int totalTrips;            // 总出行量
        public double dailyAvgTrips;      // 日均出行量
        public List<double[]> weeklyTrend; // 周趋势 [dayOfWeek, count]
        public List<double[]> monthlyTrend;// 月趋势 [dayOfMonth, count]

        public TripDemand(int totalTrips, double dailyAvgTrips,
                          List<double[]> weeklyTrend, List<double[]> monthlyTrend) {
            this.totalTrips = totalTrips;
            this.dailyAvgTrips = dailyAvgTrips;
            this.weeklyTrend = weeklyTrend;
            this.monthlyTrend = monthlyTrend;
        }
    }

    /** 时段热力分布 */
    public static class TimeHeatDistribution {
        public List<double[]> slots;  // [[count, percentage], ...]
        public String peakAnalysis;   // 高峰分析结果文本

        public TimeHeatDistribution(List<double[]> slots, String peakAnalysis) {
            this.slots = slots;
            this.peakAnalysis = peakAnalysis;
        }
    }

    /** 出行方式结构 */
    public static class ModeShare {
        public List<double[]> shares; // [[count, percentage], ...]
        // index 0=共享单车, 1=共享汽车, 2=拼车
        public String[] labels = {"共享单车", "共享汽车", "拼车"};

        public ModeShare(List<double[]> shares) {
            this.shares = shares;
        }
    }

    /** 碳减排效益 */
    public static class CarbonReduction {
        public double totalReduction;      // 累计减排量 (kgCO₂)
        public double monthlyReduction;     // 月度减排量
        public double perCapitaReduction;   // 人均减排量
        public List<double[]> monthlyTrend; // 月度趋势 [[月份, 减排量], ...]

        public CarbonReduction(double totalReduction, double monthlyReduction,
                               double perCapitaReduction, List<double[]> monthlyTrend) {
            this.totalReduction = totalReduction;
            this.monthlyReduction = monthlyReduction;
            this.perCapitaReduction = perCapitaReduction;
            this.monthlyTrend = monthlyTrend;
        }
    }

    /** 车辆运营效率 */
    public static class VehicleEfficiency {
        public double avgDistance;                       // 平均行驶距离 (km)
        public double avgServiceDuration;                // 平均服务时长 (分钟)
        public double utilizationRate;                   // 车辆利用率 (%)
        public List<String[]> highUtilTop10;             // 高利用率Top10
        public List<String[]> lowUtilTop10;              // 低利用率Top10
        public List<String[]> usageRanking;              // 使用次数排行

        public VehicleEfficiency(double avgDistance, double avgServiceDuration,
                                 double utilizationRate,
                                 List<String[]> highUtilTop10,
                                 List<String[]> lowUtilTop10,
                                 List<String[]> usageRanking) {
            this.avgDistance = avgDistance;
            this.avgServiceDuration = avgServiceDuration;
            this.utilizationRate = utilizationRate;
            this.highUtilTop10 = highUtilTop10;
            this.lowUtilTop10 = lowUtilTop10;
            this.usageRanking = usageRanking;
        }
    }
}
