// ============================================================
// RandomDataGenerator.java — 随机数据生成工具类
// 负责司机姓名、车辆型号、车牌号、评分、到达时间
// 车辆图片、订单号的随机生成
// ============================================================
package com.example.smarttravel;

import java.util.Random;

/**
 * RandomDataGenerator 随机数据生成器
 * 用于模拟网约车平台的司机匹配结果
 * 每次调用产生不同的随机组合，提高演示真实性
 */
public class RandomDataGenerator {

    private static final Random random = new Random();

    // ===== 司机姓名池 =====
    private static final String[] DRIVER_NAMES = {
            "王师傅", "李师傅", "张师傅", "刘师傅",
            "陈师傅", "赵师傅", "周师傅", "黄师傅"
    };

    // ===== 车辆型号池 =====
    private static final String[] CAR_MODELS = {
            "比亚迪秦PLUS",  "比亚迪汉EV",  "大众朗逸",
            "丰田卡罗拉",    "日产轩逸",    "本田雅阁",
            "特斯拉Model3",  "小鹏P7",      "广汽埃安S",
            "吉利星瑞"
    };

    // ===== 省份简称（车牌用） =====
    private static final String[] PROVINCES = {"粤", "京", "沪", "苏", "浙", "川", "湘", "鄂", "豫", "鲁"};

    // ===== 城市字母（车牌用） =====
    private static final char[] CITY_LETTERS = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G'
    };

    // ===== 车辆图片资源ID池 =====
    private static final int[] CAR_IMAGE_RES = {
            R.drawable.ic_car_1,
            R.drawable.ic_car_2,
            R.drawable.ic_car_3,
            R.drawable.ic_car_4,
            R.drawable.ic_car_5
    };

    // ============================================================

    /**
     * 随机抽取一位司机姓名
     */
    public static String getRandomDriverName() {
        return DRIVER_NAMES[random.nextInt(DRIVER_NAMES.length)];
    }

    /**
     * 随机抽取一款车辆型号
     */
    public static String getRandomCarModel() {
        return CAR_MODELS[random.nextInt(CAR_MODELS.length)];
    }

    /**
     * 随机生成车牌号
     * 格式：省份简称 + 城市字母 + 5位数字
     * 如：粤A83527、京B45218
     */
    public static String getRandomPlateNo() {
        String province = PROVINCES[random.nextInt(PROVINCES.length)];
        char cityLetter = CITY_LETTERS[random.nextInt(CITY_LETTERS.length)];
        // 生成5位数字
        int number = 10000 + random.nextInt(90000);
        return province + cityLetter + number;
    }

    /**
     * 随机生成评分（4.5 ~ 5.0，保留1位小数）
     */
    public static double getRandomRating() {
        // 4.5 + 0.0~0.5，保留1位小数
        double rating = 4.5 + random.nextDouble() * 0.5;
        return Math.round(rating * 10.0) / 10.0;
    }

    /**
     * 随机生成预计到达时间（2~8分钟）
     */
    public static int getRandomArriveMinutes() {
        return 2 + random.nextInt(7); // 2~8分钟
    }

    /**
     * 随机抽取一张车辆图片资源ID
     */
    public static int getRandomCarImageRes() {
        return CAR_IMAGE_RES[random.nextInt(CAR_IMAGE_RES.length)];
    }

    /**
     * 自动生成订单号
     * 格式：YYYYMMDDHHMMSS + 3位随机数
     * 如：20260601142355128
     * 保证每次生成唯一
     */
    public static String generateOrderNo() {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss",
                java.util.Locale.getDefault()).format(new java.util.Date());
        int suffix = 100 + random.nextInt(900); // 3位随机数
        return timestamp + suffix;
    }

    /**
     * 获取当前时间字符串
     * 格式：yyyy-MM-dd HH:mm
     */
    public static String getCurrentTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",
                java.util.Locale.getDefault()).format(new java.util.Date());
    }

    /**
     * 随机生成车上车点附近坐标（用于地图标记）
     * @return [x, y] 百分比坐标（0~1）
     */
    public static float[] getRandomMapPosition() {
        float x = 0.1f + random.nextFloat() * 0.8f;
        float y = 0.1f + random.nextFloat() * 0.8f;
        return new float[]{x, y};
    }
}
