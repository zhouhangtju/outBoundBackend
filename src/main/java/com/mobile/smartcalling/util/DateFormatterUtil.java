package com.mobile.smartcalling.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Slf4j
public class DateFormatterUtil {

    public static String stringHHmm(Date date) {
        SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm");
        return simpleDateFormatTime.format(date);
    }

    public static String stringDateTime(Date date) {
        SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormatTime.format(date);
    }

    public static String stringDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    public static String string(Date date) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleFormat.format(date);
    }
    public static String stringMinute(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(date);
    }

    public String stringLocalDateTime(LocalDate date) {
        DateTimeFormatter dateTimeFormatterTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return date.format(dateTimeFormatterTime);
    }

    public static String stringLocalDate(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(dateTimeFormatter);
    }

    public static String stringLocal(LocalDate date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return date.format(dateFormatter);
    }

    public static Date before(Date date1,Date date2) {
        return date1.before(date2)?date1 : date2;
    }

    public static Date after(Date date1,Date date2) {
        return date1.after(date2)?date1 : date2;
    }

    /**
     * 根据传入的日期,获取时间区间中所有的日期
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return java.util.List<java.time.LocalDate>
     *
     */
    public static List<LocalDate> getAllDatesInTheDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> localDateList = new ArrayList<>();
        // 开始时间必须小于结束时间
        if (startDate.isAfter(endDate)) {
            return null;
        }
        while (startDate.isBefore(endDate)) {
            localDateList.add(startDate);
            startDate = startDate.plusDays(1);
        }
        localDateList.add(endDate);
        return localDateList;
    }

    public static Date getDateByString1(String ds) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return simpleDateFormat.parse(ds);
        } catch (ParseException e) {
            log.error("getLocalDate error", e);
        }
        return null;
    }

    public static LocalDate getLocalDateByString(String ds) {
        try {
            /*"yyyy-MM-dd HH:mm:ss"*/
            SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormatTime.parse(ds);
            return getLocalDateByDate(date);
        } catch (ParseException e) {
            log.error("", e);
        }
        return null;
    }

    public static LocalDate getLocalDateByYmd(String ds) {
        try {
            /*yyyy-MM-dd*/
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = simpleFormat.parse(ds);
            return getLocalDateByDate(date);
        } catch (ParseException e) {
            log.error("", e);
        }
        return null;
    }

    public static LocalDate getLocalDateByDate(Date date) {
        //返回当前系统默认的时区
        ZoneId zoneId = ZoneId.systemDefault();
        //atZone()方法返回在指定时区,从该Instant生成的ZonedDateTime
        ZonedDateTime zonedDateTime = date.toInstant().atZone(zoneId);
        LocalDate localDate = zonedDateTime.toLocalDate();
        return localDate;
    }

    public static Date getDateByString2(String time) {
        try {
            SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return simpleDateFormatTime.parse(time);
        } catch (ParseException e) {
            log.error("parse date error ", e);
        }
        return null;
    }
}
