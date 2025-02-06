package com.mobile.smartcalling.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Component
@Slf4j
public class DateBetweenWeekUtil {

    /*
    查询从昨天开始前7天区间的开始时间和结束时间
     */
    public static List<String> getWeekTime(String startTime,String endTime) {
        ArrayList<String> list = new ArrayList<>();
        if(StringUtils.isEmpty(startTime)||StringUtils.isEmpty(endTime)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
            Date now = new Date();
            // 格式化并打印日期

            endTime = sdf.format(DateUtils.addDays(now, -1));
            startTime = sdf.format(DateUtils.addDays(now, -7));
            list.add(startTime);
            list.add(endTime);
            log.info("查询区间：{},{}", startTime,endTime);
        }else {
            list.add(startTime);
            list.add(endTime);
            log.info("查询区间：{},{}", startTime,endTime);
        }

        return list;
    }



    /*
    查询从今天开始前7天区间的开始时间和结束时间
     */
    public static List<String> getRealWeekTime(String startTime,String endTime) {
        ArrayList<String> list = new ArrayList<>();
        if(StringUtils.isEmpty(startTime)||StringUtils.isEmpty(endTime)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置为东八区
            Date now = new Date();
            // 格式化并打印日期

            endTime = sdf.format(DateUtils.addDays(now, 0));
            startTime = sdf.format(DateUtils.addDays(now, -6));
            list.add(startTime);
            list.add(endTime);
            log.info("查询区间：{},{}", startTime,endTime);
        }else {
            list.add(startTime);
            list.add(endTime);
            log.info("查询区间：{},{}", startTime,endTime);
        }

        return list;
    }
}
