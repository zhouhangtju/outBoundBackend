package com.mobile.smartcalling.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeConverter {
    // 定义时间段映射
    private static final Map<String, String> TIME_SLOTS = new HashMap<>();
    static {
        // 时间段定义
        TIME_SLOTS.put("上午", "08:00-12:00");
        TIME_SLOTS.put("中午", "12:00-16:00");
        TIME_SLOTS.put("下午", "12:00-16:00");
        TIME_SLOTS.put("晚上", "16:00-20:00");

        // 小时点定义
        TIME_SLOTS.put("8点", "08:00-10:00");
        TIME_SLOTS.put("八点", "08:00-10:00");
        TIME_SLOTS.put("9点", "08:00-10:00");
        TIME_SLOTS.put("九点", "08:00-10:00");
        TIME_SLOTS.put("10点", "10:00-12:00");
        TIME_SLOTS.put("十点", "10:00-12:00");
        TIME_SLOTS.put("11点", "10:00-12:00");
        TIME_SLOTS.put("十一点", "10:00-12:00");
        TIME_SLOTS.put("12点", "12:00-14:00");
        TIME_SLOTS.put("十二点", "12:00-14:00");
        TIME_SLOTS.put("13点", "12:00-14:00");
        TIME_SLOTS.put("十三点", "12:00-14:00");
        TIME_SLOTS.put("14点", "14:00-16:00");
        TIME_SLOTS.put("十四点", "14:00-16:00");
        TIME_SLOTS.put("15点", "14:00-16:00");
        TIME_SLOTS.put("十五点", "14:00-16:00");
        TIME_SLOTS.put("16点", "16:00-18:00");
        TIME_SLOTS.put("十六点", "16:00-18:00");
        TIME_SLOTS.put("17点", "16:00-18:00");
        TIME_SLOTS.put("十七点", "16:00-18:00");
        TIME_SLOTS.put("18点", "18:00-20:00");
        TIME_SLOTS.put("十八点", "18:00-20:00");
        TIME_SLOTS.put("19点", "18:00-20:00");
        TIME_SLOTS.put("十九点", "18:00-20:00");
        TIME_SLOTS.put("20点", "20:00-22:00");
        TIME_SLOTS.put("二十点", "20:00-22:00");
    }

    // 日期关键词列表（按长度降序排列，避免部分匹配）
    private static final List<String> DATE_KEYWORDS = Arrays.asList(
            "大后天", "后天", "明天", "今天",
            "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日",
            "周一", "周二", "周三", "周四", "周五", "周六", "周日", "周天"
    );

    // 时间关键词列表（按长度降序排列）
    private static final List<String> TIME_KEYWORDS = Arrays.asList(
            "上午", "中午", "下午", "晚上",
            "二十点", "19点", "十九点", "18点", "十八点",
            "17点", "十七点", "16点", "十六点", "15点", "十五点",
            "14点", "十四点", "13点", "十三点", "12点", "十二点",
            "11点", "十一点", "10点", "十点", "9点", "九点", "8点", "八点"
    );

    // 当前基准日期：2025-08-13

    public static void main(String[] args) {
        // 测试字符串
        String[] testStrings = {
                "抓取时间关键字",
        };

        // 提取时间并转换
        for (String str : testStrings) {
            List<String> timeExpressions = extractTimeExpressions(str);
            System.out.println("原字符串: " + str);
            System.out.println("提取的时间: " + timeExpressions);

            List<String> convertedTimes = new ArrayList<>();
            for (String timeExpr : timeExpressions) {
                convertedTimes.add(convertTime(timeExpr));
            }

            System.out.println("转换结果: " + convertedTimes + "\n");
        }
    }

    /**
     * 从字符串中提取所有时间表达式
     */
    public static List<String> extractTimeExpressions(String input) {
        List<String> results = new ArrayList<>();
        String remaining = input;

        // 循环提取直到没有更多时间表达式
        while (true) {
            String found = findTimeExpression(remaining);
            if (found == null) {
                break;
            }

            results.add(found);
            // 从剩余字符串中移除已找到的表达式
            int index = remaining.indexOf(found);
            remaining = remaining.substring(index + found.length());
        }

        return results;
    }

    /**
     * 在字符串中查找第一个时间表达式
     */
    private static String findTimeExpression(String input) {
        // 先查找完整的日期+时间表达式
        for (String date : DATE_KEYWORDS) {
            int dateIndex = input.indexOf(date);
            if (dateIndex != -1) {
                // 找到日期后，检查后面是否有时间关键词
                String substringAfterDate = input.substring(dateIndex + date.length());
                for (String time : TIME_KEYWORDS) {
                    if (substringAfterDate.startsWith(time)) {
                        return date + time;
                    }
                }
                // 如果日期后面没有时间，单独的日期也是一个时间表达式
                return date;
            }
        }

        // 再查找单独的时间表达式
        for (String time : TIME_KEYWORDS) {
            if (input.contains(time)) {
                return time;
            }
        }

        return null;
    }

    /**
     * 转换时间描述，处理完整格式、仅日期、仅时间三种情况
     */
    public static String convertTime(String timeDesc) {
        // 检查是否是仅日期
        if (DATE_KEYWORDS.contains(timeDesc)) {
            // 只有日期时，默认使用上午时段
            return convertTime(timeDesc + "上午");
        }

        // 检查是否是仅时间
        if (TIME_KEYWORDS.contains(timeDesc)) {
            // 只有时间时，默认使用明天的日期
            return convertTime("明天" + timeDesc);
        }

        // 处理完整格式：日期+时间
        String datePart = "";
        String timePart = "";

        // 提取日期部分
        for (String dateKeyword : DATE_KEYWORDS) {
            if (timeDesc.startsWith(dateKeyword)) {
                datePart = dateKeyword;
                timePart = timeDesc.substring(dateKeyword.length());
                break;
            }
        }

        // 验证时间部分
        if (!TIME_KEYWORDS.contains(timePart)) {
            return "不支持的时间格式: " + timeDesc;
        }

        // 获取日期
        LocalDate date = getDateByDescription(datePart);
        if (date == null) {
            return "无法识别的日期: " + datePart;
        }

        // 格式化并返回结果
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(dateFormatter) + " " + TIME_SLOTS.get(timePart);
    }

    /**
     * 根据日期描述获取对应的日期
     */
    private static LocalDate getDateByDescription(String description) {
        LocalDate BASE_DATE = LocalDate.now();
        switch (description) {
            case "今天":
                return BASE_DATE;
            case "明天":
                return BASE_DATE.plusDays(1);
            case "后天":
                return BASE_DATE.plusDays(2);
            case "大后天":
                return BASE_DATE.plusDays(3);
            case "周一":
            case "星期一":
                return getNextDayOfWeek(DayOfWeek.MONDAY,BASE_DATE);
            case "周二":
            case "星期二":
                return getNextDayOfWeek(DayOfWeek.TUESDAY,BASE_DATE);
            case "周三":
            case "星期三":
                return getNextDayOfWeek(DayOfWeek.WEDNESDAY,BASE_DATE);
            case "周四":
            case "星期四":
                return getNextDayOfWeek(DayOfWeek.THURSDAY,BASE_DATE);
            case "周五":
            case "星期五":
                return getNextDayOfWeek(DayOfWeek.FRIDAY,BASE_DATE);
            case "周六":
            case "星期六":
                return getNextDayOfWeek(DayOfWeek.SATURDAY,BASE_DATE);
            case "周日":
            case "星期日":
            case "周天":
                return getNextDayOfWeek(DayOfWeek.SUNDAY,BASE_DATE);
            default:
                return null;
        }
    }

    /**
     * 获取下一个指定星期几的日期
     */
    private static LocalDate getNextDayOfWeek(DayOfWeek dayOfWeek,LocalDate BASE_DATE) {
        LocalDate date = BASE_DATE;
        while (!date.getDayOfWeek().equals(dayOfWeek)) {
            date = date.plusDays(1);
        }
        return date.isBefore(BASE_DATE) ? date.plusWeeks(1) : date;
    }
}
