package com.mobile.smartcalling.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");


    /**
     * 生成当天的 Redis Key
     */
    private String getTodayKey() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return today;
    }


    /**
     * @param phoneNum 电话号码
     */
    public void markPhoneCalledToday(String phoneNum) {
        String todayKey = getTodayKey();
        // 1. 将号码加入当天的 Set 集合
        redisTemplate.opsForSet().add(todayKey, phoneNum);
    }

    /**
     * 判断某个电话号码今日是否已使用过
     * @param phoneNum 电话号码
     * @return true=已使用，false=未使用
     */
    public boolean isPhoneCalledToday(String phoneNum) {
        String todayKey = getTodayKey();
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(todayKey, phoneNum));
    }


    /**
     * 删除昨天存储手机号的Redis Set集合key
     * @return 是否删除成功（true=删除成功/key不存在，false=删除失败）
     */
    public boolean deleteYesterdayPhoneKey() {
        // 1. 获取昨天的日期
        LocalDate yesterday = LocalDate.now().minusDays(1);
        // 2. 拼接昨天的key
        String yesterdayKey =  yesterday.format(DATE_FORMATTER);

        // 3. 删除key（delete方法返回Boolean，拆箱为boolean，key不存在时也返回true）
         Boolean deleted = redisTemplate.delete(yesterdayKey);
        // 处理null（Redis连接异常时可能返回null），避免空指针
        return deleted != null && deleted;
    }

}