package com.mobile.smartcalling.util;


import cn.hutool.core.collection.CollUtil;
import com.mobile.smartcalling.entity.NewCallbackData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
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
     * 删除当天Set中某个手机号（用于清除“当天已外呼”限制）
     */
    public String removeTodayPoorQualityDispatchPhone(String phone) {
        String todayKey = getTodayKey();
        Long removed = redisTemplate.opsForSet().remove(todayKey, phone);
        if (removed != null && removed > 0) {
            log.info("手机号：{} 已从今日质差派单外呼集合 {} 删除", phone, todayKey);
        } else {
            log.info("手机号：{} 不在今日质差派单外呼集合 {} 中，无需删除", phone, todayKey);
        }
        return "200";
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


    // 手机号30天存储：key = "phone:13812345678"
    public void setPhoneByThirtyDays(String phone) {
        redisTemplate.opsForValue().set("phone:" + phone, "1", 30, TimeUnit.DAYS);
    }

    // 手机号60天存储：key = "phone:13812345678"
    public void setPhoneBySixtyDays(String phone) {
        redisTemplate.opsForValue().set("phone:" + phone, "1", 60, TimeUnit.DAYS);
    }

    public void setOrderIdAndBroadband(String orderId,String broadband){
        String orderIdKey = "order:"+orderId;
        redisTemplate.opsForValue().set(orderIdKey, broadband, 30, TimeUnit.DAYS);
        log.info("orderId关联宽带账号存入Redis成功，orderIdKey:{}, broadband:{}", orderIdKey, broadband);
    }

    public Object getOrderIdAndBroadband(String orderId){
        String orderIdKey = "order:"+orderId;
        Object broadband = redisTemplate.opsForValue().get(orderIdKey);
        log.info("orderId关联宽带账号查询Redis成功，orderIdKey:{}, broadband:{}",orderIdKey,broadband);
        return broadband;
    }

    /**
     * 批量根据订单号获取关联宽带账号
     * @param orderIds 订单号列表
     * @return 包含 orderId 和 broadband 的 Map
     */
    public Map<String, String> multiGetOrderAndBroadband(List<String> orderIds) {
        if (CollUtil.isEmpty(orderIds)) {
            return new HashMap<>();
        }

        // 1. 构建需要查询的完整 Redis Key 列表
        List<String> redisKeys = orderIds.stream()
                .map(orderId -> "order:" + orderId)
                .collect(Collectors.toList());

        // 2. 一次性批量查询 Redis
        List<Object> values = redisTemplate.opsForValue().multiGet(redisKeys);

        // 3. 将结果组装成 Map，方便后续通过 orderId 快速查找
        Map<String, String> resultMap = new HashMap<>();
        for (int i = 0; i < orderIds.size(); i++) {
            Object value = values.get(i);
            if (value != null) {
                resultMap.put(orderIds.get(i), value.toString());
            }else{
                resultMap.put(orderIds.get(i),"");
            }
        }

        log.info("批量查询Redis成功，请求数量:{}, 命中数量:{}", orderIds.size(), resultMap.size());
        return resultMap;
    }



    // 判断手机号是否存在
    public boolean isPhoneExists(String phone) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("phone:" + phone));
    }


    /**
     * 获取手机号在 Redis 中的剩余过期时间（单位：秒）
     */
    public long getPhoneTtl(String phone) {
        String key = "phone:" + phone;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }


    // 删除指定 phone 缓存
    public  String removePhoneByRedis(String phone){
        // 1. 构建 Redis key
        String redisKey = "phone:" + phone;

        // 2. 判断 Redis 中是否存在该 key
        Boolean hasKey = redisTemplate.hasKey(redisKey);

        // 3. 根据是否存在执行不同逻辑
        if (Boolean.TRUE.equals(hasKey)) {
            // 存在旧数据，删除
            redisTemplate.delete(redisKey);
            log.info("手机号：{} 缓存已经删除",phone);

            return "200";
        } else {

            log.info("手机号：{} 缓存中不存在 无需删除",phone);

            return "200";
        }

    }

    public static void main(String[] args) {

        NewCallbackData cell = new NewCallbackData();

        String  taskName = "装机单竣工回访-杭州";



        String taskNames = Optional.ofNullable(taskName).orElse("");
        String[] taskArray = taskNames.split("-");

        String[] taskArray2 = taskName.split("-", 2);


        System.out.println(taskArray2[0]+"----------------"+taskArray2[1]);
    }

}
