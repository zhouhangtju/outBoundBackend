package com.mobile.smartcalling.common;

import cn.hutool.core.util.EnumUtil;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PhoneCallBackStatusEnum {

    WAITING(0, "等待呼叫"),
    SUCCESS(1, "呼叫成功"),
    INTERCEPTED_BY_OPERATOR(2, "运营商拦截"),
    REJECTED(3, "拒接"),
    NO_ANSWER(4, "无应答/无人接听"),
    INVALID_NUMBER(5, "空号"),
    POWER_OFF(6, "关机"),
    SUSPENDED(7, "停机"),
    BUSY(8, "占线/用户正忙"),
    INCOMING_RESTRICTED(9, "呼入限制"),
    ARREARS(10, "欠费"),
    BLACKLISTED(11, "黑名单"),
    USER_BLOCKED(12, "用户屏蔽");

    private final int code;
    private final String description;

    PhoneCallBackStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取对应的枚举值
     */
    public static PhoneCallBackStatusEnum fromCode(int code) {
        for (PhoneCallBackStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    public static List<Integer> getPhoneCodes(){
//        List<Object> codes = EnumUtil.getFieldValues(PhoneCallBackStatusEnum.class, "code");

        List<Integer> codes = EnumUtil.getFieldValues(PhoneCallBackStatusEnum.class, "code")
                .stream()
                .map(obj -> (Integer) obj) // 安全强转，因为你知道 code 是 int/Integer
                .collect(Collectors.toList());

        return codes;
    }


    // 1,3,11,12，如果得到的是这4个状态码，场景XX 天内不会重复呼同一个外呼号码
    public static List<Integer> notOutsidecodes(){
        List<Integer> codes = Arrays.asList(1, 3, 11, 12);
        return codes;
    }




}
